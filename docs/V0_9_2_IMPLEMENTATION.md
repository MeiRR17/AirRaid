# AirRaid v0.9.2 — Guided beta QA and package-integrity hardening

**Status:** implemented as a development patch, not an approved public beta or production release.

## What changed

- `QaChecklistScreen.java` adds an accessible, scrollable, eight-item manual checklist. It covers an end-to-end match, both player keys, pause/settings/resume, result correctness, replay, sound/motion preferences, offline use and native installation/uninstallation. The steps remain **self-reported**, resettable and local to a running app session.
- The menu and game toolbar expose **QA checklist**, with the `Q` shortcut. `Window` routes the checklist as another Swing card, pausing a running match before opening and **not auto-resuming** when Back returns to the game.
- Copy is deliberately opt-in: **Copy checklist** writes a plain-text self-reported report only to the local clipboard, without network calls, accounts, persistence or telemetry. Clipboard errors are caught and visible. It never calls the human beta acceptance gate or records a completed playtest.
- `AppVersion.NUMBER`, Maven, CI and app-image workflows advance to **0.9.2**; the visible beta labels, existing result QA report and headless `--version` stay synchronized.
- `GuiSmoke` exercises menu → checklist → Back → gameplay → checklist → pause/back/explicit resume → Results; captures a **real checklist screenshot** in the CI virtual display in addition to the game, settings and results screenshots.
- `QaChecklistScreenTest` verifies no implicit copying, explicit clicks, focusability, ephemeral state, copied text, clipboard failure and Escape navigation.
- `tools/release_gate.py` now rejects **JAR extra/duplicate `Resource/` entries** and **symlinked media files** even when their content matches the manifest; it continues verifying source Git fingerprints and byte-identical packaged media. New Python regression tests include those adversarial cases.

## Acceptance and limitations

Minimum engineering gate: Java 21 `mvn -B -ntp clean verify`, Python unit tests, asset/JAR integrity, beta readiness development report, Linux Java-bundled app-image launch and Xvfb GUI navigation/capture. The actual results are visible in the PR CI run and are **not** a human test certification.

Manual checklist ticks are **not** signed evidence. A real session must be observed and recorded using `docs/BETA_PLAYTEST_PLAN.md`, including test environment, actual people, a completed round, defect triage and internal evidence reference. The QA clipboard report cannot prove human participation.

### Remaining production blockers

- **0 of 3 verified real human playtests** (including two simultaneous participants): [#15](https://github.com/MeiRR17/AirRaid/issues/15).
- Windows/macOS/Linux clean installer validation and actual offline full-play tests: [#13](https://github.com/MeiRR17/AirRaid/issues/13).
- **23 visual assets without independently verified redistribution rights**, source-code license and release-owner signoff, dated dependency-security review: [#9](https://github.com/MeiRR17/AirRaid/issues/9).

The app remains an **internal beta engineering build**. No public installer, production tag, GitHub Release or legal license clearance is claimed.
