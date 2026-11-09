# AirRaid v0.9.0 — Internal beta playtesting and acceptance

**Status: development beta, not authorized for public distribution.** Do not distribute bundled graphics/fonts until the 23 outstanding visual-media rights are verified and the code-license/release owner gates are cleared ([#9](https://github.com/MeiRR17/AirRaid/issues/9), [#13](https://github.com/MeiRR17/AirRaid/issues/13)). The three required **human** sessions have **not** yet been conducted or recorded. Automated tests are not substitutes for human playtests.

## Feature freeze

The v0.9.0 beta engineering scope is limited to resolving game timing accuracy, regression coverage, reproducible defect reports, and playtest acceptance tooling. Do not introduce online accounts, telemetry, game modes, third-party sound samples or live network requirements. Do not publish a GitHub beta release with redistributable installers while legal distribution is blocked.

## Minimum human session matrix

| Session | Target host | People | Required observations |
| --- | --- | --- | --- |
| 01 | Linux clean QA install | 2 simultaneous players on one keyboard | Full match, independent Enter/Space, overlapping fire, strikes, pause/resume, score, replay, offline, uninstall |
| 02 | Windows clean QA install | ≥1 real tester | Fresh launch without separate Java, full match, Settings, window resize, audio unavailable, replay, offline, uninstall |
| 03 | macOS clean QA install | ≥1 real tester | Same workflow, high-DPI, reduced motion, focus loss, game result, offline, uninstall |

The sessions must be observed on actual systems, with each session’s result and proof linked to a GitHub issue or independently retained internal QA record. Three test fixtures in CI are **synthetic** and never represent completed real-world sessions.

## Test protocol for each session

1. Record OS/architecture, tested AirRaid commit and native installer SHA-256 in the issue/evidence reference. Do not record testers' names, personal file paths, passwords or recordings without consent.
2. Install the authorized internal QA package on a clean user account with no separately installed Java. Confirm the bundled-JRE launcher opens the graphical game.
3. Disconnect from the network. Run at least one **complete round**, not just a quick startup. Blue uses Enter and Orange uses Space at the same time. Confirm independent bombs, no doubled scoring, one bomb per team and accurate time limits.
4. Pause via P and by switching to Settings and Rules. Verify no time progression while paused; resume explicitly. Resize the window and verify targets remain aligned.
5. Exercise sound off, volume, reduced motion and persisted settings. Check no crash on machines without an audio device.
6. Finish the round either through strikes or the 60-second timer. Confirm correct winner/tie and zero further score changes after completion. Replay and return to menu.
7. Uninstall and document retained user preferences, leftover installer files, and any OS-specific dialogs. Report P0/P1/P2 defects with reproduction steps and corresponding issues.
8. Record real evidence in `docs/beta-evidence/sessions.json` (schema below) **only after** the session is performed. Evidence references should resolve to non-sensitive issues or internal artifacts.

## Machine-readable evidence schema

`tools/beta_gate.py` reads `docs/beta-evidence/sessions.json`, containing an object with `schema_version: 1` and `sessions: [ ... ]`. Each session must contain:

- `session_id`: unique neutral ID, e.g. `qa-linux-001`.
- `os`: exactly `linux`, `windows` or `macos`.
- `human_players`: 1 or 2; at least one session needs 2 humans on the same keyboard.
- `completed_rounds`: positive integer.
- `scenarios`: booleans for `offline`, `two_player_fire`, `pause_resume`, `settings`, `results`, `replay`, `exit`, `install_uninstall`.
- `evidence_ref`: a substantive non-sensitive reference to a QA issue or approved report.
- `defects`: array of objects, each with `severity` (P0/P1/P2/P3), `status` (open/closed), and `issue_ref`.

A separate `docs/beta-evidence/triage.json` requires `schema_version: 1`, `open_p0_p1: 0`, `signed_off: true`, and `evidence_ref` linking to reviewed issue triage. These fields must be backed by actual evidence; they are not to be filled merely to make CI green. The beta gate additionally requires the source/media/license release-readiness JSON to declare legal approval.

## Running the gates

```sh
mvn -B -ntp clean verify
python3 -m unittest discover -s tools -p 'test_*.py' -v
python3 tools/release_gate.py --mode development --jar target/airraid-0.9.0.jar
python3 tools/beta_gate.py --mode development
```

Development mode publishes `target/beta-readiness.json` and reports **BLOCKED** while human sessions, P0/P1 sign-off or rights are missing, but still lets unrelated code improvements pass CI. `--mode beta` is fail-closed and must not pass without documentary evidence.

## Exit criteria and P0/P1 status

The beta stage cannot be checked off or published until **three real complete sessions**, two-person keyboard testing, **clean install/uninstall across all three OSes**, no unresolved P0/P1, and authorized redistribution are verified. A zero-defect count produced by an empty issue list is **not proof** of a triaged zero; human triage sign-off is a distinct mandatory input.

See the [beta implementation report](V0_9_IMPLEMENTATION.md), [troubleshooting](BETA_TROUBLESHOOTING.md), [packaging QA](DESKTOP_DISTRIBUTION.md), and [production checklist](RELEASE_CHECKLIST.md).
