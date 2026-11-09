#!/usr/bin/env python3
"""Inspectable local release-asset gate. No network or third-party Python modules.

Development mode verifies that the source and built JAR contain exactly the
inventoried legacy media, and reports unresolved legal approval without failing
the engineering build. Release mode fails closed until separate human-verified
permissions and a source-code license have been recorded.
"""
import argparse
import csv
import hashlib
import json
from pathlib import Path
import re
import sys
import zipfile


def git_blob_sha(data: bytes) -> str:
    return hashlib.sha1(b"blob " + str(len(data)).encode("ascii") + b"\0" + data).hexdigest()


def inspect(root: Path, jar: Path | None = None) -> dict:
    manifest = root / "docs" / "asset-provenance.tsv"
    errors = []
    blockers = []
    rows = []
    if not manifest.is_file():
        return {
            "source_integrity": False,
            "package_integrity": False,
            "public_release_ready": False,
            "errors": ["asset manifest is missing"],
            "blockers": ["asset provenance absent"],
            "assets": [],
        }
    with manifest.open("r", encoding="utf-8", newline="") as stream:
        reader = csv.DictReader(stream, delimiter="\t")
        columns = {"resource_path", "git_blob_sha", "status", "evidence_ref"}
        if not columns.issubset(reader.fieldnames or []):
            errors.append("asset manifest columns invalid")
        else:
            rows = list(reader)

    resource_dir = root / "src/main/resources/Resource"
    actual_files = [p for p in resource_dir.rglob("*") if p.is_file() or p.is_symlink()]
    actual_paths = {
        p.relative_to(root / "src/main/resources").as_posix()
        for p in actual_files
    }
    if any(p.is_symlink() for p in actual_files):
        errors.append("symlinked source assets are not permitted")
    listed = []
    verified_source = []
    verified_zip = []
    try:
        archive = zipfile.ZipFile(jar) if jar is not None else None
    except (OSError, zipfile.BadZipFile):
        archive = None
        errors.append("packaged JAR cannot be inspected")
    if archive is not None:
        names = archive.namelist()
        packaged_assets = [name for name in names
                           if name.startswith("Resource/") and not name.endswith("/")]
        if len(packaged_assets) != len(set(packaged_assets)):
            errors.append("duplicate resource entries in JAR")
        if set(packaged_assets) != actual_paths:
            errors.append("JAR resource inventory differs from source")

    for row in rows:
        rel = row["resource_path"]
        listed.append(rel)
        if (not rel.startswith("Resource/") or ".." in Path(rel).parts
                or "\\" in rel or rel.endswith("/") or rel.startswith("/")):
            errors.append("asset manifest path invalid")
            continue
        source = root / "src/main/resources" / rel
        if not source.is_file():
            errors.append("asset source missing: " + rel)
            continue
        content = source.read_bytes()
        expected = row["git_blob_sha"]
        if not re.fullmatch(r"[0-9a-f]{40}", expected or "") or git_blob_sha(content) != expected:
            errors.append("asset fingerprint mismatch: " + rel)
        else:
            verified_source.append(rel)
        if archive is not None:
            try:
                package_bytes = archive.read(rel)
                if package_bytes != content:
                    errors.append("JAR resource differs from source: " + rel)
                else:
                    verified_zip.append(rel)
            except KeyError:
                errors.append("JAR resource missing: " + rel)
            except (OSError, RuntimeError, zipfile.BadZipFile):
                errors.append("JAR resource unreadable: " + rel)
        status = row["status"]
        evidence = row["evidence_ref"]
        if status != "APPROVED" or not evidence or evidence == "-":
            blockers.append("redistribution permission not verified: " + rel)

    if archive is not None:
        archive.close()
    if len(listed) != len(set(listed)):
        errors.append("duplicate resource paths in asset manifest")
    if set(listed) != actual_paths:
        errors.append("manifest does not exactly cover bundled resources")
    if not rows:
        errors.append("asset manifest is empty")
    if not (root / "LICENSE").is_file():
        blockers.append("source code LICENSE is not owner-approved/published")
    if not (root / "docs" / "RELEASE_APPROVAL.md").is_file():
        blockers.append("human release approval not recorded")
    source_ok = not errors and len(verified_source) == len(actual_paths)
    package_ok = jar is None or (
        archive is not None and len(verified_zip) == len(actual_paths)
        and not any("JAR" in error for error in errors)
        and not any("duplicate resource" in error for error in errors))
    return {
        "schema_version": 1,
        "asset_count": len(actual_paths),
        "verified_source_count": len(verified_source),
        "verified_packaged_count": len(verified_zip) if jar is not None else None,
        "source_integrity": source_ok,
        "package_integrity": package_ok,
        "public_release_ready": source_ok and package_ok and not blockers,
        "errors": sorted(errors),
        "blockers": sorted(blockers),
        "assets": sorted(actual_paths),
    }


def main(argv=None) -> int:
    parser = argparse.ArgumentParser(description="AirRaid source and release-readiness gate")
    parser.add_argument("--root", type=Path, default=Path("."))
    parser.add_argument("--jar", type=Path)
    parser.add_argument("--mode", choices=["development", "release"], required=True)
    parser.add_argument("--output", type=Path, default=Path("target/release-readiness.json"))
    args = parser.parse_args(argv)
    report = inspect(args.root, args.jar)
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(report, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    print("AIRRAID_GATE mode=" + args.mode +
          " sources=" + ("PASS" if report["source_integrity"] else "FAIL") +
          " jar=" + ("PASS" if report["package_integrity"] else "FAIL") +
          " release=" + ("READY" if report["public_release_ready"] else "BLOCKED") +
          " blockers=" + str(len(report["blockers"])))
    if not report["source_integrity"] or not report["package_integrity"]:
        return 1
    return 0 if args.mode == "development" or report["public_release_ready"] else 2


if __name__ == "__main__":
    sys.exit(main())
