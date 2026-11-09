#!/usr/bin/env python3
"""List pinned Maven direct dependencies and build plugins without fetching network data."""
import argparse
import json
from pathlib import Path
import sys
import xml.etree.ElementTree as ET

NS = {"m": "http://maven.apache.org/POM/4.0.0"}


def inventory(pom: Path):
    tree = ET.parse(pom)
    root = tree.getroot()
    props = {p.tag.rsplit("}", 1)[-1]: (p.text or "").strip()
             for p in root.findall("./m:properties/*", NS)}

    def resolve(text):
        value = (text or "").strip()
        for _ in range(5):
            if not (value.startswith(chr(36) + "{") and value.endswith("}")):
                break
            value = props.get(value[2:-1], "")
        return value

    def record(base, parent):
        entries = []
        for element in root.findall(base, NS):
            data = {key: (element.findtext("m:" + key, default="", namespaces=NS) or "").strip()
                    for key in ("groupId", "artifactId", "version", "scope")}
            data["version"] = resolve(data["version"])
            if (not data["version"] or data["version"].startswith(chr(36) + "{")
                    or "SNAPSHOT" in data["version"].upper()
                    or data["version"].upper() in ("LATEST", "RELEASE")):
                raise ValueError("Maven " + parent + " version must be pinned: " + data["artifactId"])
            entries.append(data)
        return entries

    dependencies = record("./m:dependencies/m:dependency", "dependency")
    plugins = record("./m:build/m:plugins/m:plugin", "plugin")
    return {
        "schema_version": 1,
        "java_release": resolve(props.get("maven.compiler.release")),
        "dependencies": dependencies,
        "build_plugins": plugins,
        "vulnerability_scan_performed": False,
        "note": "Direct components only. This inventory is NOT a vulnerability scan or third-party license clearance."
    }


def main():
    p = argparse.ArgumentParser()
    p.add_argument("--pom", type=Path, default=Path("pom.xml"))
    p.add_argument("--output", type=Path, default=Path("target/dependency-inventory.json"))
    args = p.parse_args()
    report = inventory(args.pom)
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(report, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    print("AIRRAID_DEPENDENCY_INVENTORY pins=" +
          str(len(report["dependencies"]) + len(report["build_plugins"])) +
          " vulnerability_scan=NOT_RUN")


if __name__ == "__main__":
    try:
        main()
    except (ET.ParseError, ValueError, OSError) as failure:
        print("AIRRAID_DEPENDENCY_INVENTORY_FAILED", file=sys.stderr)
        sys.exit(1)
