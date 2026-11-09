import json
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch

from package_desktop import (
    host_target, project_version, image_paths, verify_image,
    sha256, tree_sha256, package,
)


class PackageDesktopTest(unittest.TestCase):
    def test_targets_are_host_specific_not_cross_compiled(self):
        self.assertEqual(("linux", "deb"), host_target("Linux"))
        self.assertEqual(("macos", "dmg"), host_target("Darwin"))
        self.assertEqual(("windows", "exe"), host_target("Windows"))
        with self.assertRaises(ValueError):
            host_target("Plan9")

    def test_image_launcher_layouts(self):
        base = Path("/images")
        self.assertEqual(base / "AirRaid/bin/AirRaid",
                         image_paths(base, "Linux")[1])
        self.assertEqual(base / "AirRaid/AirRaid.exe",
                         image_paths(base, "Windows")[1])
        self.assertEqual(base / "AirRaid.app/Contents/MacOS/AirRaid",
                         image_paths(base, "Darwin")[1])
        self.assertEqual(base / "AirRaid.app/Contents/runtime/Contents/Home",
                         image_paths(base, "Darwin")[2])

    def test_source_pom_version_is_stable(self):
        pom = Path(__file__).resolve().parents[1] / "pom.xml"
        self.assertEqual("0.8.0", project_version(pom))

    def test_sha256_tree_changes_with_contents(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            p = root / "test.bin"
            p.write_bytes(b"abc")
            first = tree_sha256(root)
            self.assertEqual(1, first["files"])
            self.assertEqual(3, first["bytes"])
            self.assertEqual(sha256(p), "ba7816bf8f01cfea414140de5dae2223b00361"
                         "a396177a9cb410ff61f20015ad")
            p.write_bytes(b"abcd")
            self.assertNotEqual(first["tree_sha256"], tree_sha256(root)["tree_sha256"])

    def test_missing_runtime_or_modified_jar_blocks_image(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            source = root / "airraid-0.8.0.jar"
            source.write_bytes(b"archive")
            image, launcher, java_home = image_paths(root, "Linux")
            (image / "bin").mkdir(parents=True)
            launcher.write_bytes(b"exe")
            (image / "lib/app").mkdir(parents=True)
            (image / "lib/app/airraid-0.8.0.jar").write_bytes(b"archive")
            with self.assertRaises(ValueError):
                verify_image(image, launcher, java_home, source)
            (java_home / "lib").mkdir(parents=True)
            (java_home / "lib/modules").write_bytes(b"runtime")
            self.assertEqual(3, verify_image(image, launcher, java_home, source)["files"])
            (image / "lib/app/airraid-0.8.0.jar").write_bytes(b"tampered")
            with self.assertRaises(ValueError):
                verify_image(image, launcher, java_home, source)

    def fixture(self, root):
        root = Path(root)
        (root / "target").mkdir(parents=True)
        (root / "pom.xml").write_text(
            '<project xmlns="http://maven.apache.org/POM/4.0.0">'
            '<version>0.8.0</version></project>', encoding="utf-8")
        (root / "target/airraid-0.8.0.jar").write_bytes(b"safe-input")
        return root

    @patch("package_desktop.platform.system", return_value="Linux")
    @patch("package_desktop.shutil.which", return_value="/test/jpackage")
    @patch("package_desktop.inspect_licenses", return_value={
        "source_integrity": True,
        "package_integrity": True,
        "public_release_ready": False,
        "blockers": ["media permission unverified"],
    })
    def test_internal_app_image_creates_evidence_only(self, _approval, _which, _system):
        with tempfile.TemporaryDirectory() as tmp:
            root = self.fixture(Path(tmp) / "repo")
            output = root / "target/qa"

            def fake_package(cmd, cwd):
                self.assertEqual("app-image", cmd[cmd.index("--type") + 1])
                dest = Path(cmd[cmd.index("--dest") + 1])
                image, launcher, runtime = image_paths(dest, "Linux")
                launcher.parent.mkdir(parents=True)
                launcher.write_bytes(b"stub")
                (runtime / "lib").mkdir(parents=True)
                (runtime / "lib/modules").write_bytes(b"embedded-runtime")
                (image / "lib/app").mkdir(parents=True)
                (image / "lib/app/airraid-0.8.0.jar").write_bytes(b"safe-input")

            with patch("package_desktop.command_run", side_effect=fake_package):
                manifest, result = package(root, output, "development",
                                           installer=False, skip_launcher=True)
            self.assertTrue(manifest.is_file())
            self.assertTrue(result["java_runtime_bundled"])
            self.assertFalse(result["public_distribution_allowed"])
            self.assertFalse(result["native_launcher_selftest"])
            self.assertIsNone(result["installer"])
            self.assertEqual(1, result["asset_release_blockers"])
            self.assertEqual(result["host_os"], "linux")
            self.assertEqual(json.loads(manifest.read_text())["input_jar_sha256"],
                             sha256(root / "target/airraid-0.8.0.jar"))

    @patch("package_desktop.platform.system", return_value="Linux")
    @patch("package_desktop.inspect_licenses", return_value={
        "source_integrity": True,
        "package_integrity": True,
        "public_release_ready": False,
        "blockers": ["media permission unverified"],
    })
    def test_public_release_fails_closed_before_running_tool(self, _approval, _system):
        with tempfile.TemporaryDirectory() as tmp:
            root = self.fixture(Path(tmp) / "repo")
            with self.assertRaisesRegex(ValueError, "Public release blocked"):
                package(root, root / "target/qa", "release", installer=True,
                        skip_launcher=True)


if __name__ == "__main__":
    unittest.main()
