# AirRaid v0.9.1 — Privacy-minimal offline beta reporting

**Development patch only.** The game remains an internal QA build. No production, public beta or installer release is authorized.

## Engineering implementation

1. `AppVersion.java` centralizes the beta number `0.9.1` and `INTERNAL BETA` channel, used by menu, gameplay, report and `Main --version`. The POM and Java version are checked together by the CI's headless launcher command.
2. `BetaRoundReport.java` creates a stable, plain-text summary **only after** a match reaches RESULTS. The report captures match outcome, end reason, points, canyon strikes, time remaining and non-sensitive audio/reduced-motion preference flags. It records no usernames, timestamps, personal file paths, devices, credentials or network information. It explicitly states that an automatically generated round result is **NOT** verified human-playtest evidence.
3. The Results card gains a **Copy QA report** button. Pressing the button is the *only* step that writes the report to the system clipboard. The app does **not** transmit it, create a report file or open an external site. If the clipboard is unavailable, an accessible status message explains the problem, leaving results and replay functional.
4. Screenshot smoke now verifies results, report button and game state after pressing Copy QA report, then captures an actual Swing **Results** screenshot. It also checks menu/game version and the existing replay/exit flow.
5. `BetaRoundReportTest.java` exercises completed-match protection, exact source-backed result values, deterministic output, privacy omissions, explicit user action and unavailable clipboard. Existing `GameCanvasTest` is adjusted for the changed Results-screen constructor.
6. `README.md`, `CHANGELOG.md`, beta troubleshooting and playtest instructions are updated. Maven and native-app workflow version move to `0.9.1`.

## Quality gate

- Java 21 `mvn -B -ntp clean verify` and all headless tests.
- Python `unittest discover`, dependency inventory, source/JAR integrity, **fail-closed beta readiness**.
- Self-contained Linux `jpackage` app image and actual launcher self-test.
- Xvfb GUI smoke for Settings → Gameplay → Results → Copy QA report → Play again → Menu, with three real screenshots, and evidence upload.
- No assertion about external user acceptance, licenses or cross-OS install results is made on the basis of these checks.

## Known unresolved production blockers

- Human testing: **0 verified complete sessions**, including the required two-human match. Issue [#15](https://github.com/MeiRR17/AirRaid/issues/15).
- Windows/macOS/Linux native clean install/uninstall verification, signing and notarization. Issue [#13](https://github.com/MeiRR17/AirRaid/issues/13).
- 23 previously bundled image/font/GIF assets with unverified redistribution rights; code-license choice, release owner approval, current vulnerability review. Issue [#9](https://github.com/MeiRR17/AirRaid/issues/9).

The patch may be merged on clean CI, but **no v1.0.0 tag or public binary distribution** should occur until these open gates have real evidence and sign-off.
