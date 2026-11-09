import tempfile
import unittest
from pathlib import Path
from zipfile import ZipFile
from release_gate import inspect, git_blob_sha


class ReleaseGateTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.root = Path(self.temp.name)
        resources = self.root / "src/main/resources/Resource"
        resources.mkdir(parents=True)
        self.sample = resources / "Asset.png"
        self.sample.write_bytes(b"\x89PNG\r\nsource-test")
        (self.root / "docs").mkdir()
        self.jar = self.root / "game.jar"
        with ZipFile(self.jar, "w") as bundle:
            bundle.write(self.sample, "Resource/Asset.png")
        self.manifest("UNVERIFIED", "-")

    def tearDown(self):
        self.temp.cleanup()

    def manifest(self, status, evidence, sha=None):
        sha = sha or git_blob_sha(self.sample.read_bytes())
        (self.root / "docs/asset-provenance.tsv").write_text(
            "resource_path\tgit_blob_sha\tstatus\tevidence_ref\n"
            + f"Resource/Asset.png\t{sha}\t{status}\t{evidence}\n",
            encoding="utf-8")

    def test_unverified_rights_do_not_hide_verified_engineering_integrity(self):
        r = inspect(self.root, self.jar)
        self.assertTrue(r["source_integrity"])
        self.assertTrue(r["package_integrity"])
        self.assertFalse(r["public_release_ready"])
        self.assertTrue(any("redistribution permission" in s for s in r["blockers"]))

    def test_blob_tampering_fails_closed(self):
        self.sample.write_bytes(b"modified file")
        r = inspect(self.root, self.jar)
        self.assertFalse(r["source_integrity"])
        self.assertTrue(any("fingerprint mismatch" in e for e in r["errors"]))

    def test_jar_media_mismatch_blocks_gate(self):
        with ZipFile(self.jar, "w") as bundle:
            bundle.writestr("Resource/Asset.png", b"unexpected")
        r = inspect(self.root, self.jar)
        self.assertFalse(r["package_integrity"])

    def test_fully_evidenced_fixture_passes_release_mechanics(self):
        self.manifest("APPROVED", "docs/license-evidence.txt")
        (self.root / "LICENSE").write_text("Test-only copyright grant")
        (self.root / "docs/RELEASE_APPROVAL.md").write_text("Test-only reviewer approval")
        r = inspect(self.root, self.jar)
        self.assertTrue(r["public_release_ready"])
        self.assertFalse(r["blockers"])

    def test_untracked_jar_resource_is_a_package_failure(self):
        with ZipFile(self.jar, "w") as bundle:
            bundle.writestr("Resource/Asset.png", self.sample.read_bytes())
            bundle.writestr("Resource/Unexpected.png", b"not in manifest")
        report = inspect(self.root, self.jar)
        self.assertTrue(report["source_integrity"])
        self.assertFalse(report["package_integrity"])
        self.assertTrue(any("JAR resource inventory" in e for e in report["errors"]))

    def test_duplicate_jar_resource_is_rejected(self):
        import warnings
        with warnings.catch_warnings():
            warnings.simplefilter("ignore", UserWarning)
            with ZipFile(self.jar, "w") as bundle:
                bundle.writestr("Resource/Asset.png", self.sample.read_bytes())
                bundle.writestr("Resource/Asset.png", self.sample.read_bytes())
        report = inspect(self.root, self.jar)
        self.assertFalse(report["package_integrity"])
        self.assertTrue(any("duplicate resource entries" in e for e in report["errors"]))

    def test_symlinked_asset_is_never_followed(self):
        from unittest import SkipTest
        original = self.sample.read_bytes()
        target = self.root / "private-source.png"
        target.write_bytes(original)
        self.sample.unlink()
        try:
            self.sample.symlink_to(target)
        except (OSError, NotImplementedError):
            raise SkipTest("symlinks unsupported on this platform")
        report = inspect(self.root, self.jar)
        self.assertFalse(report["source_integrity"])
        self.assertTrue(any("symlinked source assets" in e for e in report["errors"]))

    def test_extra_untracked_resource_is_rejected(self):
        (self.root / "src/main/resources/Resource/Extra.png").write_bytes(b"new")
        r = inspect(self.root, self.jar)
        self.assertFalse(r["source_integrity"])
        self.assertTrue(any("exactly cover" in e for e in r["errors"]))

    def test_path_traversal_is_rejected(self):
        (self.root / "docs/asset-provenance.tsv").write_text(
            "resource_path\tgit_blob_sha\tstatus\tevidence_ref\n"
            + "../outside.txt\t" + "0" * 40 + "\tAPPROVED\tbad\n",
            encoding="utf-8")
        r = inspect(self.root, self.jar)
        self.assertFalse(r["source_integrity"])
        self.assertTrue(any("path invalid" in e for e in r["errors"]))


if __name__ == "__main__":
    unittest.main()
