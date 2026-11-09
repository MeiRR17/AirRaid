# AirRaid v0.9.3 — Responsive HUD and safe replay controls

**Scope:** Internal beta engineering patch. Public binary distribution and v1.0.0 authorization remain blocked.

## Gameplay and accessibility changes

- Replaced the single, horizontally overflowing status `JLabel` at the bottom of `Gameplay` with an accessible `JPanel(GridLayout(2,3))`. Six independently labelled cells show Blue score/strikes, Orange score/strikes, remaining time, active game state, optional audio/reduced motion, and the internal beta build identifier. Both players' states and the actual timer remain visible in a 620×480 app window.
- Blue and Orange scores use contrasting blue/orange text **plus explicit words**, not color alone. The remaining cells use neutral high-contrast text. Accessible names identify each cell for assistive technologies.
- Removed accidental round restart on the global `Enter` blue-fire binding while game state is RESULTS. Auto-repeat of a held player key no longer deletes the outcome. The explicit **Play again** button and **R** replay shortcut remain functional.
- Version `0.9.3` is reflected consistently in `pom.xml`, `AppVersion`, the headless `--version` check, the normal Java 21 CI and optional cross-platform desktop QA workflow.

## Evidence and safety tests

- `ResponsiveHudTest` validates all six cells, semantic labels, a real 620×480 layout, pause/resume state transitions and lifecycle cleanup. Its second regression verifies that a dozen synthetic `Enter` repeats after match completion cannot auto-restart.
- `GuiSmoke` checks the compact-scoreboard content at the minimum supported dimensions; adds a **real Java Swing screenshot** (`airraid-v093-ci-compact-hud.png`) and verifies a held `Enter` binding cannot dismiss the results screen.
- Other Java/Python tests, source/JAR rights-gated integrity reports, bundled-runtime Linux native-launcher smoke, settings/QA/results screenshots and game lifecycle tests remain in the PR CI pipeline.

## Outstanding production blockers

- Three **real** human playtests (including two simultaneous players on one keyboard) remain unrecorded: [#15](https://github.com/MeiRR17/AirRaid/issues/15).
- Clean, offline Windows/macOS/Linux install/run/uninstall certification is still incomplete: [#13](https://github.com/MeiRR17/AirRaid/issues/13).
- All 23 legacy visual assets lack verified redistribution rights, source-code license and owner release sign-off remain unresolved, and a dated supply-chain vulnerability review has not been completed: [#9](https://github.com/MeiRR17/AirRaid/issues/9).

A successful CI run approves merging the **engineering patch only**; do not create a public installer, beta/production release or v1.0 tag until all independent acceptance evidence is verified.
