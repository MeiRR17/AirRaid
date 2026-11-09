#!/usr/bin/env python3
"""Fail-closed beta acceptance report; no invented playtest results or external services.

Development mode allows engineering CI while reporting unmet human and legal
acceptance criteria. Beta mode requires documented sessions, triage and rights.
No sensitive tester identities, recordings, locations or telemetry are collected.
"""
import argparse
import json
from pathlib import Path
import re
import sys

TARGET_OSES = {"linux", "windows", "macos"}
SCENARIOS = (
    "offline",
    "two_player_fire",
    "pause_resume",
    "settings",
    "results",
    "replay",
    "exit",
    "install_uninstall",
)
SEVERITIES = {"P0", "P1", "P2", "P3"}
STATUSES = {"open", "closed"}


def read_json(path, errors):
    if not path.is_file():
        return None
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except (ValueError, OSError, UnicodeError):
        errors.append("Invalid beta evidence JSON: " + path.name)
        return None


def inspect(root):
    root = Path(root)
    errors = []
    blockers = []
    sessions_path = root / "docs/beta-evidence/sessions.json"
    triage_path = root / "docs/beta-evidence/triage.json"
    release_path = root / "target/release-readiness.json"
    sessions_doc = read_json(sessions_path, errors)
    triage = read_json(triage_path, errors)
    release = read_json(release_path, errors)
    if sessions_doc is None:
        blockers.append("Three real, documented human playtests not recorded")
        sessions = []
    elif not isinstance(sessions_doc, dict) or sessions_doc.get("schema_version") != 1 \
            or not isinstance(sessions_doc.get("sessions"), list):
        errors.append("Playtest sessions schema invalid")
        sessions = []
    else:
        sessions = sessions_doc["sessions"]

    valid = []
    seen = set()
    opened = []
    for session in sessions:
        if not isinstance(session, dict):
            errors.append("Playtest entry must be an object")
            continue
        session_id = session.get("session_id")
        if not isinstance(session_id, str) or not re.fullmatch("[a-zA-Z0-9_-]{3,40}", session_id):
            errors.append("Session id invalid")
            continue
        if session_id in seen:
            errors.append("Duplicate session id")
            continue
        seen.add(session_id)
        if session.get("os") not in TARGET_OSES:
            errors.append("Unsupported playtest OS")
            continue
        players = session.get("human_players")
        rounds = session.get("completed_rounds")
        scenarios = session.get("scenarios")
        evidence = session.get("evidence_ref")
        if (type(players) is not int or players < 1 or players > 2
                or type(rounds) is not int or rounds < 1
                or not isinstance(scenarios, dict)
                or any(type(scenarios.get(key)) is not bool for key in SCENARIOS)
                or not isinstance(evidence, str) or len(evidence.strip()) < 8):
            errors.append("Incomplete playtest session: " + session_id)
            continue
        defects = session.get("defects")
        if not isinstance(defects, list):
            errors.append("Defect list missing: " + session_id)
            continue
        has_invalid_defect = False
        for defect in defects:
            if (not isinstance(defect, dict)
                    or defect.get("severity") not in SEVERITIES
                    or defect.get("status") not in STATUSES
                    or not isinstance(defect.get("issue_ref"), str)
                    or len(defect["issue_ref"].strip()) < 5):
                has_invalid_defect = True
                break
            if defect["severity"] in ("P0", "P1") and defect["status"] == "open":
                opened.append(defect["issue_ref"])
        if has_invalid_defect:
            errors.append("Invalid defect record: " + session_id)
            continue
        valid.append(session)

    if len(valid) < 3:
        blockers.append("At least three fully documented human playtest sessions required")
    if not any(s["human_players"] == 2 and s["scenarios"]["two_player_fire"] for s in valid):
        blockers.append("One real two-human one-keyboard session required")
    for os_name in sorted(TARGET_OSES):
        if not any(s["os"] == os_name and all(s["scenarios"][key] for key in SCENARIOS)
                   for s in valid):
            blockers.append("Full offline/install/play/replay/uninstall session missing: " + os_name)
    if opened:
        blockers.append("Open P0/P1 defects in playtests must be resolved")

    if not isinstance(triage, dict) or triage.get("schema_version") != 1:
        blockers.append("Human-reviewed issue-triage sign-off missing")
    elif (triage.get("open_p0_p1") != 0
          or triage.get("signed_off") is not True
          or not isinstance(triage.get("evidence_ref"), str)
          or len(triage["evidence_ref"].strip()) < 8):
        blockers.append("Issue triage is unsigned or has open P0/P1 defects")

    # Beta binaries would redistribute the bundled legacy visual resources.
    if not isinstance(release, dict) or release.get("public_release_ready") is not True:
        blockers.append("Source, bundled-media rights and release approval remain blocked")

    return {
        "schema_version": 1,
        "beta_candidate_ready": not errors and not blockers,
        "real_human_sessions_validated": len(valid),
        "two_human_keyboard_session_recorded": any(
            s["human_players"] == 2 and s["scenarios"]["two_player_fire"] for s in valid),
        "os_coverage": sorted({s["os"] for s in valid}),
        "open_p0_p1_playtest_defects": len(opened),
        "errors": sorted(set(errors)),
        "blockers": sorted(set(blockers)),
        "automated_checks_are_not_human_playtests": True,
    }


def main(argv=None):
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", type=Path, default=Path("."))
    parser.add_argument("--mode", choices=("development", "beta"), required=True)
    parser.add_argument("--output", type=Path, default=Path("target/beta-readiness.json"))
    args = parser.parse_args(argv)
    report = inspect(args.root)
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(report, indent=2, sort_keys=True) + "\n",
                           encoding="utf-8")
    print("AIRRAID_BETA_GATE mode=" + args.mode
          + " ready=" + str(report["beta_candidate_ready"]).lower()
          + " real_sessions=" + str(report["real_human_sessions_validated"])
          + " blockers=" + str(len(report["blockers"]))
          + " errors=" + str(len(report["errors"])))
    if report["errors"]:
        return 1
    return 0 if args.mode == "development" or report["beta_candidate_ready"] else 2


if __name__ == "__main__":
    sys.exit(main())
