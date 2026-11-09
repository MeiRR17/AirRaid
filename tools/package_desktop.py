#!/usr/bin/env python3
"""Build and validate an INTERNAL jpackage desktop distribution.

No installer is uploaded or released. A public release run fails closed while
the separate source/media license, approval and production gates are unresolved.
Requires Java 21 jpackage from a host matching the intended target OS.
"""
import argparse
import hashlib
import json
import os
from pathlib import Path
import platform
import shutil
import subprocess
import sys
import xml.etree.ElementTree as ET

from release_gate import inspect as inspect_licenses


SUPPORTED = {"Linux": ("linux", "deb"), "Darwin": ("macos", "dmg"),
             "Windows": ("windows", "exe")}


def host_target(system=None):
    system = system or platform.system()
    if system not in SUPPORTED:
        raise ValueError("Unsupported packaging operating system")
    return SUPPORTED[system]


def project_version(pom):
    xml = ET.parse(pom).getroot()
    ns = {"p": "http://maven.apache.org/POM/4.0.0"}
    version = xml.findtext("p:version", namespaces=ns)
    if not version or not version.count(".") == 2 or any(not part.isdigit()
                                                           for part in version.split(".")):
        raise ValueError("Project has no fixed semantic version")
    return version


def sha256(path):
    h = hashlib.sha256()
    with Path(path).open("rb") as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b""):
            h.update(block)
    return h.hexdigest()


def tree_sha256(folder):
    """Deterministic content inventory, independent of file timestamps."""
    h = hashlib.sha256()
    count = 0
    total = 0
    for file in sorted(p for p in Path(folder).rglob("*")
                       if p.is_file() or p.is_symlink()):
        relative = file.relative_to(folder).as_posix()
        h.update(relative.encode("utf-8") + b"\0")
        if file.is_symlink():
            # macOS app bundles may use internal framework links. Record them,
            # but reject any symlink targeting outside the captured package.
            destination = file.resolve(strict=True)
            if not destination.is_relative_to(Path(folder).resolve()):
                raise ValueError("Package symlink escapes application image")
            h.update(b"symlink\0" + os.readlink(file).encode("utf-8"))
        else:
            h.update(bytes.fromhex(sha256(file)))
            total += file.stat().st_size
        count += 1
    return {"tree_sha256": h.hexdigest(), "files": count, "bytes": total}


def image_paths(package_dir, system):
    image = Path(package_dir) / ("AirRaid.app" if system == "Darwin" else "AirRaid")
    if system == "Darwin":
        launcher = image / "Contents/MacOS/AirRaid"
        java_home = image / "Contents/runtime/Contents/Home"
    elif system == "Windows":
        launcher = image / "AirRaid.exe"
        java_home = image / "runtime"
    else:
        launcher = image / "bin/AirRaid"
        java_home = image / "lib/runtime"
    return image, launcher, java_home


def verify_image(image, launcher, java_home, source_jar):
    if not image.is_dir():
        raise ValueError("jpackage application image missing")
    if not launcher.is_file():
        raise ValueError("Native application launcher missing")
    if not java_home.is_dir() or not (java_home / "lib/modules").is_file():
        raise ValueError("Bundled Java runtime image missing")
    packaged = list(image.rglob(source_jar.name))
    if len(packaged) != 1 or sha256(packaged[0]) != sha256(source_jar):
        raise ValueError("Packaged game JAR differs from verified input")
    count = tree_sha256(image)
    if count["files"] < 3:
        raise ValueError("Suspiciously incomplete application image")
    return count


def command_run(cmd, cwd):
    subprocess.run([str(x) for x in cmd], cwd=cwd, check=True,
                   timeout=180)


def package(root, output, mode, installer, skip_launcher=False):
    root = Path(root).resolve()
    output = Path(output).resolve()
    system = platform.system()
    os_label, installer_type = host_target(system)
    version = project_version(root / "pom.xml")
    jar = root / "target" / ("airraid-" + version + ".jar")
    if not jar.is_file():
        raise ValueError("Run Maven clean verify before desktop packaging")

    approval = inspect_licenses(root, jar)
    if not approval["source_integrity"] or not approval["package_integrity"]:
        raise ValueError("Source/JAR media integrity gate failed")
    if mode == "release" and not approval["public_release_ready"]:
        raise ValueError("Public release blocked by asset/code rights and owner approval")
    tool = shutil.which("jpackage")
    if not tool:
        raise ValueError("Java 21 jpackage executable unavailable")
    # Ensure exactly the validated application JAR enters the launcher payload,
    # not test reports or accidental working-directory files.
    staging = output / "staging"
    images = output / "images"
    installers = output / "installers"
    for directory in (staging, images, installers):
        if directory.exists():
            shutil.rmtree(directory)
        directory.mkdir(parents=True)
    shutil.copy2(jar, staging / jar.name)

    image_cmd = [tool, "--type", "app-image", "--name", "AirRaid",
                 "--app-version", version, "--input", staging,
                 "--main-jar", jar.name, "--main-class", "Main",
                 "--add-modules", "java.desktop", "--dest", images]
    command_run(image_cmd, root)
    image, launcher, runtime = image_paths(images, system)
    content = verify_image(image, launcher, runtime, jar)

    if not skip_launcher:
        # This proves that the packaged native launcher and its bundled JVM
        # can start the application with NO global Java installation.
        command_run([launcher, "--self-test"], root)

    build_metadata = {
        "schema_version": 1,
        "status": "internal_qa_only",
        "public_distribution_allowed": False,  # Never publish builds from this internal QA workflow
        "host_os": os_label,
        "host_arch": platform.machine(),
        "app_version": version,
        "java_runtime_bundled": True,
        "native_launcher_selftest": not skip_launcher,
        "app_image": content,
        "input_jar_sha256": sha256(jar),
        "installer": None,
        "codesigned": False,
        "notarized": False,
        "clean_install_uninstall_tested": False,
        "asset_release_blockers": len(approval["blockers"]),
        "source_git_sha": os.environ.get("GITHUB_SHA", "unrecorded"),
    }
    if installer:
        installer_cmd = [tool, "--type", installer_type,
                         "--app-image", image,
                         "--name", "AirRaid", "--app-version", version,
                         "--dest", installers]
        if system == "Linux":
            installer_cmd.extend(["--linux-package-name", "airraid"])
        command_run(installer_cmd, root)
        ext = "." + installer_type
        candidates = list(installers.glob("*" + ext))
        if len(candidates) != 1:
            raise ValueError("Expected exactly one native installer")
        candidate = candidates[0]
        build_metadata["installer"] = {
            "format": installer_type,
            "filename": candidate.name,
            "sha256": sha256(candidate),
            "bytes": candidate.stat().st_size,
            "install_uninstall_tested": False,
        }
    # Store evidence separately; do not upload/copy installers to GitHub.
    manifest = root / "target" / ("desktop-evidence-" + os_label + ".json")
    manifest.parent.mkdir(parents=True, exist_ok=True)
    manifest.write_text(json.dumps(build_metadata, indent=2, sort_keys=True) + "\n",
                        encoding="utf-8")
    return manifest, build_metadata


def main(argv=None):
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", type=Path, default=Path("."))
    parser.add_argument("--output", type=Path, default=Path("target/desktop-qa"))
    parser.add_argument("--mode", choices=("development", "release"),
                        default="development")
    parser.add_argument("--installer", action="store_true",
                        help="Also create host-native unsigned installer (internal QA only)")
    args = parser.parse_args(argv)
    try:
        evidence, report = package(args.root, args.output, args.mode, args.installer)
    except (ValueError, OSError, subprocess.CalledProcessError,
            subprocess.TimeoutExpired, ET.ParseError) as error:
        print("AIRRAID_DESKTOP_PACKAGE_FAIL " + type(error).__name__, file=sys.stderr)
        return 1
    print("AIRRAID_DESKTOP_PACKAGE_PASS os=" + report["host_os"]
          + " java_bundled=true installer=" + str(args.installer).lower()
          + " release=" + ("blocked" if report["asset_release_blockers"] else "review"))
    print("AIRRAID_DESKTOP_EVIDENCE " + str(evidence))
    return 0


if __name__ == "__main__":
    sys.exit(main())
