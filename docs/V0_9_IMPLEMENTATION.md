# AirRaid v0.9.0 — Beta stabilization implementation

**Status:** Engineering beta candidate with automated verification. Not a publicly distributed beta release. Real playtest and cross-platform install gates remain outstanding.

## Runtime improvements

- Fixed a real timing defect: Swing `Timer` previously advanced the simulation by **16ms per callback** regardless of time lost to coalesced GUI events. This could cause a nominal 60-second match to run significantly longer on slower hosts.
- Added `MatchClock`, a monotonic `System.nanoTime`-driven elapsed-time accumulator. It retains fractional milliseconds, excludes pause/focus-loss time, and resets on every restart/resume. `GameController` uses actual measured time rather than an assumed fixed callback interval; the pure `GameModel` remains deterministic with bounded, 16ms physics substeps.
- Added a small **Beta 0.9.0 / internal playtest** version indicator to the menu and game status strip. This distinguishes screenshots and support records from older builds without asserting production readiness.

## Automated stability tests

- `MatchClockTest`: fractional rounding, delayed callback catch-up, pause (even multi-second), restart, no repeated result callbacks, and EDT-only controller use.
- `BetaCampaignTest`: 10 complete simulated 60-second matches plus repeated replay, two-player fire and release, rapid restart, and four-strike-vs-timer precedence. These use simulated model time, not human observation.
- All existing headless gameplay, security, accessibility, source/JAR integrity and GUI smoke checks remain mandatory.
- CI continues Linux `jpackage` app-image and native launcher checks from v0.8.0, and captures **real** Settings/gameplay screenshots with v0.9.0 captions.

## Fail-closed beta acceptance tracker

- `tools/beta_gate.py`: validates structured real session records, three complete sessions, an actual two-human same-keyboard match, Linux/Windows/macOS coverage with offline+clean install/uninstall, human-reviewed P0/P1 triage, and external package redistribution approval.
- `tools/test_beta_gate.py`: tests missing real sessions, malformed/duplicate records, open P1 defects, missing OS/two-human coverage, legal blockers, and strictly synthetic fixture success. Synthetic test data are **never** written to the actual playtest record folder.
- `target/beta-readiness.json`: machine-readable evidence in CI. Development mode passes the code gate while preserving `beta_candidate_ready=false` if real human or legal evidence is absent. Beta mode fails closed until all checks are documented.
- `docs/BETA_PLAYTEST_PLAN.md`: complete manual execution/triage matrix. `docs/BETA_TROUBLESHOOTING.md`: support/issue diagnostics. `docs/beta-evidence/README.md`: non-personal record format. No live playtest results are fabricated.

## Open release blockers

1. Zero actual verified human sessions so far; at least three are mandatory, including two simultaneous human players.
2. Native Windows/macOS app images and clean Windows/macOS/Linux installer acceptance remain outstanding: [issue #13](https://github.com/MeiRR17/AirRaid/issues/13).
3. All 23 legacy visual resources lack verified redistribution permissions, an approved software license and human release sign-off: [issue #9](https://github.com/MeiRR17/AirRaid/issues/9).
4. Full transitive dependency CVE/license review, signing/notarization and public release authorization remain open.

**Result:** It is appropriate to merge beta *engineering* improvements after CI passes, but not appropriate to check off v0.9.0 as completely accepted or to publish a public beta/release tag.

Related: [roadmap](ROADMAP.md), [release checklist](RELEASE_CHECKLIST.md).
