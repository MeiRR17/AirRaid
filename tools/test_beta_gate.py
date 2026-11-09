import json
from pathlib import Path
from tempfile import TemporaryDirectory
import unittest

from beta_gate import inspect, main, SCENARIOS


def session(index, os_name, two_players=False):
    return {
        "session_id": f"session-{index:03d}",
        "os": os_name,
        "human_players": 2 if two_players else 1,
        "completed_rounds": 1,
        "scenarios": {key: True for key in SCENARIOS},
        "evidence_ref": f"issues/playtest-session-{index:03d}",
        "defects": [],
    }


class BetaGateTest(unittest.TestCase):
    def setUp(self):
        self.temp = TemporaryDirectory()
        self.root = Path(self.temp.name)
        (self.root / "docs/beta-evidence").mkdir(parents=True)
        (self.root / "target").mkdir()
        self.sessions = [
            session(1, "linux", True),
            session(2, "windows"),
            session(3, "macos"),
        ]

    def tearDown(self):
        self.temp.cleanup()

    def write(self, relative, content):
        path = self.root / relative
        path.write_text(json.dumps(content), encoding="utf-8")

    def fixture(self):
        self.write("docs/beta-evidence/sessions.json", {
            "schema_version": 1, "sessions": self.sessions})
        self.write("docs/beta-evidence/triage.json", {
            "schema_version": 1, "open_p0_p1": 0,
            "signed_off": True, "evidence_ref": "issues/triage-reviewed"})
        self.write("target/release-readiness.json", {"public_release_ready": True})

    def test_missing_real_sessions_cannot_pass_beta(self):
        report = inspect(self.root)
        self.assertFalse(report["beta_candidate_ready"])
        self.assertEqual(0, report["real_human_sessions_validated"])
        self.assertTrue(report["automated_checks_are_not_human_playtests"])
        self.assertEqual(0, main(["--root", str(self.root),
                                  "--mode", "development"]))
        self.assertEqual(2, main(["--root", str(self.root), "--mode", "beta"]))

    def test_three_documented_sessions_and_approvals_pass_only_synthetic_gate(self):
        self.fixture()
        report = inspect(self.root)
        self.assertTrue(report["beta_candidate_ready"])
        self.assertEqual(3, report["real_human_sessions_validated"])
        self.assertEqual(["linux", "macos", "windows"], report["os_coverage"])
        self.assertTrue(report["two_human_keyboard_session_recorded"])
        # These records are synthetic TEST FIXTURES and must never be committed
        # as actual AirRaid playtest evidence.
        self.assertEqual(0, main(["--root", str(self.root), "--mode", "beta"]))

    def test_one_open_p1_or_missing_evidence_blocks_beta(self):
        self.fixture()
        self.sessions[0]["defects"].append({
            "severity": "P1", "status": "open", "issue_ref": "issues/critical-123"})
        self.write("docs/beta-evidence/sessions.json", {
            "schema_version": 1, "sessions": self.sessions})
        report = inspect(self.root)
        self.assertFalse(report["beta_candidate_ready"])
        self.assertEqual(1, report["open_p0_p1_playtest_defects"])

    def test_release_blockers_cannot_be_ignored(self):
        self.fixture()
        self.write("target/release-readiness.json", {
            "public_release_ready": False, "blockers": ["asset licenses"]})
        self.assertFalse(inspect(self.root)["beta_candidate_ready"])

    def test_os_coverage_and_real_two_player_session_are_mandatory(self):
        self.fixture()
        for entry in self.sessions:
            entry["os"] = "linux"
            entry["human_players"] = 1
        self.write("docs/beta-evidence/sessions.json", {
            "schema_version": 1, "sessions": self.sessions})
        report = inspect(self.root)
        self.assertFalse(report["beta_candidate_ready"])
        self.assertFalse(report["two_human_keyboard_session_recorded"])
        self.assertTrue(any("windows" in b for b in report["blockers"]))
        self.assertTrue(any("macos" in b for b in report["blockers"]))

    def test_invalid_or_duplicate_records_rejected(self):
        self.fixture()
        self.sessions.append(dict(self.sessions[0]))
        self.write("docs/beta-evidence/sessions.json", {
            "schema_version": 1, "sessions": self.sessions})
        self.assertTrue(any("Duplicate" in e for e in inspect(self.root)["errors"]))

    def test_corrupt_json_fails_development_build(self):
        (self.root / "docs/beta-evidence/sessions.json").write_text(
            "{not-json", encoding="utf-8")
        self.assertEqual(1, main(["--root", str(self.root),
                                  "--mode", "development"]))

    def test_unsigned_triage_blocks_beta(self):
        self.fixture()
        self.write("docs/beta-evidence/triage.json", {
            "schema_version": 1, "open_p0_p1": 0,
            "signed_off": False, "evidence_ref": "issues/triage-reviewed"})
        self.assertFalse(inspect(self.root)["beta_candidate_ready"])


if __name__ == "__main__":
    unittest.main()
