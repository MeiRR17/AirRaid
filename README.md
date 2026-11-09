# AirRaid

**AirRaid** is an offline, two-player Java Swing arcade game. Each plane flies automatically and players use bombs to score against targets. This project is a *game*, not an emergency alert system.

**Current development version: v0.9.4 — stable keyboard shortcuts and clear pause feedback.** Timing, replay stability and structured QA reporting are implemented. This is not a public beta or production distribution; media rights and platform acceptance remain blocked.

## Build and play

Requires JDK 21 and Maven 3.9+:

```sh
mvn -B -ntp clean verify
java -jar target/airraid-0.9.4.jar
```

The application runs without accounts or network access. Tests run headlessly; GitHub Actions also launches it under a virtual display, checks the game/navigation flow and uploads **real screenshots of the Settings and gameplay screens**.

## Desktop packages (new in v0.8.0)

Build the executable JAR first, then create a native application image with a **bundled Java 21 runtime** (no separate JVM required for the launcher):

```sh
mvn -B -ntp clean verify
python3 tools/package_desktop.py --mode development --output target/desktop-qa
```

The packaging script uses the host's JDK 21 `jpackage` tool and creates the application image under `target/desktop-qa/images/`. It verifies the bundled native launcher with `--self-test`, checks the included runtime modules and the exact input JAR SHA-256, and writes `target/desktop-evidence-<platform>.json`.

For **internal QA only**, pass `--installer` to also create an **unsigned** host-native installer: Linux `.deb`, macOS `.dmg`, or Windows `.exe`. These builds are **not published or uploaded** because artwork/font licensing and source-code approval remain unresolved. The regular PR CI verifies a Linux app image and its bundled launcher; the opt-in [three-platform packaging workflow](.github/workflows/desktop-packages.yml) can run app-image verification on macOS and Windows as well. Cross-platform clean installation and uninstall tests are **not yet certified**.

See [Desktop packaging guide](docs/DESKTOP_DISTRIBUTION.md), [v0.8.0 verification](docs/V0_8_IMPLEMENTATION.md) and [release gates](docs/RELEASE_CHECKLIST.md).

## Keyboard-repeat-safe gameplay — new in v0.9.4

**Holding P, R or M now triggers only one action per physical key press.** This prevents OS key repeat from rapidly toggling Pause/Resume, repeatedly erasing the match with Restart, or flipping sound repeatedly. The action becomes available again on key release; shortcut states are safely reset after window focus loss or returning from a modal game screen. Enter and Space remain independent continuous two-player firing controls.

The paused game displays a large, static **MATCH PAUSED** panel explaining that the timer is stopped and that pressing P or clicking Resume continues the round. The pause view respects reduced-motion settings and requires no new animation, threads or network access. CI tests held-key sequences and captures the real pause overlay; Windows/macOS human testing is still outstanding.

See [v0.9.4 engineering evidence](docs/V0_9_4_IMPLEMENTATION.md) and [beta testing plan](docs/BETA_PLAYTEST_PLAN.md).

## Responsive match scoreboard — new in v0.9.4

The previously long, single-line status text is split into six labelled areas in a 2×3 panel: **Blue**, **Orange**, **time remaining**, **match state**, **sound / reduced motion**, and **beta version**. The scoreboard stays visible when resizing the game down to the supported minimum (620×480), and each cell has an accessible name.

**Enter always means Blue fire during a running match.** Holding Enter at the end of a round can no longer inadvertently start a new game and discard the result. Use the explicit **Play again** button or the **R** shortcut to replay. This is covered by a key-repeat regression test and a real small-window screenshot from Xvfb CI.

See [implementation and acceptance notes](docs/V0_9_3_IMPLEMENTATION.md). Release remains blocked by human playtests, cross-platform installation approval and asset rights.

## Guided QA checklist — new in v0.9.2

Select **QA checklist** from the main menu or game toolbar, or press **Q**. The in-app checklist provides eight real-world testing steps: a complete two-player round, simultaneous keyboard input, pause and settings, winner and timer, replay, sound and reduced motion, offline gameplay, and clean installation/uninstallation.

Mark steps manually with checkboxes. Progress remains local to the running application, never uploaded or automatically written to disk. Press **Copy checklist** to copy a privacy-minimal, explicitly self-reported text summary to the clipboard for an issue or test record. A checkmark is **not** human verification, and it does not satisfy the beta or release gates. Opening the checklist during a match pauses the game, and returning does not resume it automatically; press P to resume.

The v0.9.2 source/JAR integrity gate also detects duplicate and extra `Resource/` entries in packaged JARs and rejects symlinked source assets. This verifies package consistency, **not legal redistribution rights**. The GUI smoke now captures a real screenshot of the checklist alongside game, settings and results.

See [v0.9.2 implementation](docs/V0_9_2_IMPLEMENTATION.md) and [human playtest plan](docs/BETA_PLAYTEST_PLAN.md).

## Offline QA report — new in v0.9.1

After a complete round, the results screen offers **Copy QA report**. It copies a text summary containing the beta version, outcome, reason, scores, canyon strikes, time remaining, sound preference and reduced-motion setting. Copying requires an explicit button click, sends nothing over the network and creates no log or file. The report deliberately contains **no names, home paths, timestamps, device IDs or account information**. If the clipboard is unavailable, the results screen displays an explanation without crashing or losing the match.

This local report is useful when submitting a beta bug report. It is **not evidence of a completed human playtest** and does not satisfy the independent three-session acceptance gate. The GUI smoke now captures the **real Results screen** as well as Settings and gameplay. Run `java -jar target/airraid-0.9.4.jar --version` to print the beta version without opening the GUI.

Implementation and tests: [v0.9.1 QA reporting](docs/V0_9_1_IMPLEMENTATION.md).

## Beta stabilization (v0.9.0)

- The Swing match timer now advances from **monotonic elapsed time**, rather than assuming each GUI callback represents exactly 16 ms. Coalesced or delayed UI callbacks no longer silently extend the nominal 60-second match; pausing and restarting reset the time baseline.
- The Java test suite exercises **ten simulated complete matches**, repeated restarts, two-player key contention, pause/resume timing and one-time match endings. These are automated model tests, **not** human playtests.
- Real human playtest acceptance is tracked through `tools/beta_gate.py`. Until three documented sessions, a two-human one-keyboard test, complete Windows/macOS/Linux install/play/uninstall verification, zero triaged P0/P1 defects and legal permission are recorded, `target/beta-readiness.json` correctly reports **not ready**.
- The title screen and in-game status clearly identify **Beta 0.9.0**. CI captures real Java Swing screenshots; no public beta installers or release tags are published.

See [beta playtest matrix](docs/BETA_PLAYTEST_PLAN.md), [troubleshooting/support](docs/BETA_TROUBLESHOOTING.md), [implementation evidence](docs/V0_9_IMPLEMENTATION.md) and the [beta bug report template](.github/ISSUE_TEMPLATE/beta-bug-report.yml).

Development evidence commands:

```sh
mvn -B -ntp clean verify
python3 -m unittest discover -s tools -p 'test_*.py' -v
python3 tools/release_gate.py --mode development --jar target/airraid-0.9.4.jar
python3 tools/beta_gate.py --mode development
```

The separate `--mode beta` command **fails closed** while human/legal evidence is absent. Do not use a synthetic test fixture to claim a real user session.

## Keyboard controls

| Key | Main menu | During a match |
| --- | --- | --- |
| **Enter** | Start a match | Blue plane fire |
| **Space** | Activate a focused button | Orange plane fire |
| **P** | — | Pause / resume |
| **R** | — | Restart |
| **H** | Rules | Rules (pauses match) |
| **S** | Settings | Settings (pauses match) |
| **M** | — | Toggle sounds |
| **Escape** | — | Return to main menu |
| **Tab / Shift+Tab** | Move between menu/settings/results controls | — |

Within Settings, **Space** toggles the selected checkbox, **arrow keys** adjust the volume slider, **Escape** returns to the prior screen. The match stays paused after leaving Settings or Rules; press **P** to resume deliberately. All game actions have accessible text labels or keyboard shortcuts.

## Settings (new in v0.6.0)

The integrated Settings screen contains:

- **Enable game sounds** — optional, disabled on a clean installation. If no audio output is available, the game continues silently.
- **Volume** — 0–100% slider, implemented by software scaling synthesized PCM sounds, so it does not require hardware mixer gain controls.
- **Reduce animation and visual effects** — displays a still frame of the menu GIF and suppresses temporary shot/hit/miss animations, while leaving scores and strike labels readable.
- **Restore defaults** — resets to sound off, 60% volume, and normal motion.
- **Keyboard help** — built into the screen, with tab navigation and visible focused controls.

Preferences are saved locally to `~/.airraid/preferences.properties` (under the current Java user-home directory) using schema version 1 and atomic file replacement when supported. Only the three non-sensitive UI settings are written. A missing, corrupted, oversized or unknown-schema file loads safe defaults, and the Settings screen provides a recovery message. No credentials or telemetry are stored.

## Gameplay rules

Targets yield 1–4 points once per target. A bomb that exits the arena without hitting an intact target gives its owner one canyon strike. **Four strikes immediately lose** the match; simultaneous fourth strikes draw. Otherwise, the higher score at **60 seconds** wins; equal scores draw.

The simulation remains deterministic and independent of rendering or audio. Pausing stops the game timer; switching menus disposes the old simulation. The arena scales proportionally in resizable windows.

## Quality and security (v0.7.0)

- Bounded, seeded two-player gameplay invariants and end-of-round regression tests ensure scores cannot exceed destroyed target values and match state stays deterministic.
- Local settings refuse symbolic links and special files, protecting unrelated user files from being read or overwritten through the preferences path.
- Startup diagnostics use stable error codes without printing file paths, stack traces, tokens or personal data.
- Every bundled image/font/GIF is listed in `docs/asset-provenance.tsv` with its source Git blob SHA and `UNVERIFIED` rights status. `tools/release_gate.py` checks these resources against the source tree **and executable JAR**, producing a machine-readable report in `target/release-readiness.json`.
- CI verifies the declaration of directly pinned Maven components using `tools/dependency_inventory.py` and now pins GitHub Actions by immutable commit SHA.
- Public release mode is intentionally **blocked** because 23 visual assets lack verified redistribution rights and a source-code license has not been approved. These facts are not repaired by passing tests. See [security policy](SECURITY.md), [third-party notices](THIRD_PARTY_NOTICES.md), [code-license status](docs/LICENSE_STATUS.md), [dependency audit](docs/DEPENDENCY_AUDIT.md) and [quality evidence](docs/V0_7_IMPLEMENTATION.md).

Development commands (after `mvn clean verify`):

```sh
python3 -m unittest discover -s tools -p 'test_*.py' -v
python3 tools/dependency_inventory.py --pom pom.xml
python3 tools/release_gate.py --mode development --jar target/airraid-0.9.4.jar
```

The development command succeeds only when media source/JAR integrity checks pass, while still reporting legal blockers. Use `--mode release` to enforce the unresolved licensing and owner-approval gates; it must not be treated as passing until provenance and rights are independently documented.

## Implementation and validation

The `PreferencesStore` safely reads/writes versioned local settings, `UserPreferences` provides immutable validated snapshots, and `SettingsScreen` is an accessible Swing interface. `Window` coordinates persistence and settings navigation; `Gameplay`, `GameCanvas` and `GameOpening` apply volume, audio and reduced-motion choices immediately.

Tests cover settings round trips, schema corruption/invalid values, safe defaults, audio PCM volume and mute, reduced-motion effects, and a GUI navigation/persistence smoke. A real GUI screenshot is published as a workflow artifact rather than substituting conceptual renderings for an actual capture.

- [v0.6.0 implementation and acceptance](docs/V0_6_IMPLEMENTATION.md)
- [Version roadmap](docs/ROADMAP.md)
- [Progress tracker](https://github.com/MeiRR17/AirRaid/issues/3)
- [Canonical game rules](docs/V0_3_GAME_RULES.md)
- [Visual asset license inventory](docs/ASSET_LICENSE_AUDIT.md)
- [Public release blocker — 23 unverified visual assets](https://github.com/MeiRR17/AirRaid/issues/9)

**Next:** complete real human beta acceptance and remaining v0.5/v0.7 rights/security and v0.8 OS installation gates before preparing v1.0.0 production authorization; then, playtesting and v1.0.0 release gates. Public redistribution remains blocked until required asset rights are independently verified or resources are replaced.

## Historical screenshots (original game, not this version)

![Original AirRaid game](https://github.com/user-attachments/assets/629c0620-b8be-4053-b602-7febd0c01265)

![Original AirRaid game](https://github.com/user-attachments/assets/2a287550-8de7-4c5b-adc0-a0b2d8361b4a)
