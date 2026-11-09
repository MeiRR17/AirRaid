# AirRaid change history

This changelog tracks development milestones. None of these entries is an approved production release.

## 0.9.3 — Responsive scoreboard and results safety
- Replace long single-line match status with a six-cell, high-contrast 2×3 scoreboard for points, strikes, clock, game state, audio and beta build.
- Prevent repeated Enter (blue fire) from accidentally replaying a finished round; keep explicit Play again and R.
- Add accessible HUD tests, key-repeat regression and a true 620×480 game screenshot under CI.
- No public beta or production release; legal, human and cross-platform acceptance remains open.

## 0.9.2 — Guided internal beta QA and package integrity
- Add accessible, local-only eight-step QA checklist to menu and game toolbar with Q shortcut, explicit copy and reset; preserve active match in paused state.
- Never classify checked boxes as externally verified human playtest sessions.
- Detect duplicate/untracked JAR resource entries and symlinked source assets; test fail-closed integrity behavior.
- Add real CI checklist screenshot, updated QA regression tests and Maven 0.9.2 build metadata. Public beta and production release remain blocked.

## 0.9.1 — Internal QA reporting patch
- Add a user-triggered offline round summary on the Results screen; Clipboard failures cannot abort a match.
- Centralize beta version display and provide a headless `--version` option for support and reproducible reports.
- Test privacy-minimal report creation, explicit consent, copy failure, and replay stability; capture real results-screen screenshots.
- No public beta release or change to outstanding human, licensing or platform approval gates.

## 0.9.0 — Internal beta stabilization
- Fix real-time match pacing under coalesced or delayed Swing timer events using a monotonic elapsed clock.
- Add deterministic ten-full-match/replay and dual-player input stress tests.
- Add fail-closed beta evidence reporting, controlled playtest matrix, triage schema, support troubleshooting and bug issue template.
- Display beta version in menu and game status; archive real CI screenshots and beta readiness evidence.
- Public beta release remains blocked by human playtest/OS installation, legal rights and code-license approval.

## 0.8.0 — Desktop packaging foundation
- Add host-native jpackage app-image creation for Windows/macOS/Linux using a bundled Java 21 runtime.
- Add a packaged-launcher headless self-test, archive/media consistency checks and SHA-256 package evidence.
- Add opt-in cross-platform QA workflow and optional unsigned host-native installer generation (not publicly uploaded).
- Preserve release blockers for unlicensed visual assets, code-license approval, vulnerability review, signing and real install/uninstall tests.

## 0.7.0 — Quality/security gates
- Add seeded simulation invariants, safe startup diagnostics and symlink-safe local preferences.
- Add provenance manifest, release-integrity gate, direct dependency inventory, pinned CI actions and security/contribution policy.

## 0.6.0 — Settings and accessibility
- Persist safe local preferences for audio volume, sound and reduced motion; add keyboard-accessible Settings screen.

## 0.5.0 — Rendering and sound
- Cache visual resources, show gameplay feedback and synthesize optional audio. Record 23 asset-rights blockers.

## 0.4.0 — Game flow
- Resizeable arena, integrated menu/rules/results screens, keyboard navigation and replay.

## 0.3.0 — Rules
- Four-canyon-strike loss, deterministic collision outcomes and scoring rules.

## 0.2.0 — Game engine
- Replace independent game entity threads with one EDT-controlled simulation and safe lifecycle.

## 0.1.0 — Build foundation
- Standard Maven Java 21 project structure, classpath media, resource and GUI smoke tests.
