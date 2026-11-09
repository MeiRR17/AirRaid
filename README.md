# AirRaid

**AirRaid** is an offline, two-player Java Swing arcade game. Each plane flies automatically and players use bombs to score against targets. This project is a *game*, not an emergency alert system.

**Current development version: v0.8.0 — native desktop packaging foundation.** The game can be packaged with its own Java runtime, but packages are internal QA builds. Public installers and production release approval remain blocked.

## Build and play

Requires JDK 21 and Maven 3.9+:

```sh
mvn -B -ntp clean verify
java -jar target/airraid-0.8.0.jar
```

The application runs without accounts or network access. Tests run headlessly; GitHub Actions also launches it under a virtual display, checks the game/navigation flow and uploads **real screenshots of the Settings and gameplay screens**.

## Desktop packages (new in v0.8.0)

Build the executable JAR first, then create a native application image with a **bundled Java 21 runtime** (no separate JVM required for the launcher):

```sh
mvn -B -ntp clean verify
python3 tools/package_desktop.py --mode development --output target/desktop-qa
```

The packaging script uses the host's JDK 21 `jpackage` tool and creates the application image under `target/desktop-qa/images/`. It verifies the bundled native launcher with `--self-test`, checks the included runtime modules and the exact input JAR SHA-256, and writes `target/desktop-evidence-<platform>.json`.

For **internal QA only**, pass `--installer` to also create an **unsigned** host-native installer: Linux `.deb`, macOS `.dmg`, or Windows `.exe`. These builds are **not published or uploaded** because artwork/font licensing and source-code approval remain unresolved. The regular PR CI tests a Linux `.deb`; the opt-in [three-platform packaging workflow](.github/workflows/desktop-packages.yml) can run app-image verification on macOS and Windows as well. Cross-platform clean installation and uninstall tests are **not yet certified**.

See [Desktop packaging guide](docs/DESKTOP_DISTRIBUTION.md), [v0.8.0 verification](docs/V0_8_IMPLEMENTATION.md) and [release gates](docs/RELEASE_CHECKLIST.md).

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
python3 tools/release_gate.py --mode development --jar target/airraid-0.8.0.jar
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

**Next:** v0.9.0 beta and real two-player playtesting, while v0.5/v0.7 license and security gates plus v0.8 cross-OS install/uninstall certification remain open; then, playtesting and v1.0.0 release gates. Public redistribution remains blocked until required asset rights are independently verified or resources are replaced.

## Historical screenshots (original game, not this version)

![Original AirRaid game](https://github.com/user-attachments/assets/629c0620-b8be-4053-b602-7febd0c01265)

![Original AirRaid game](https://github.com/user-attachments/assets/2a287550-8de7-4c5b-adc0-a0b2d8361b4a)
