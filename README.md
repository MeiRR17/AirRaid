# AirRaid

**AirRaid** is an offline, two-player Java Swing arcade game. Each plane flies automatically and players use bombs to score against targets. This project is a *game*, not an emergency alert system.

**Current development version: v0.6.0 — accessibility, keyboard support and local preferences.** Public native installers and the production release remain pending.

## Build and play

Requires JDK 21 and Maven 3.9+:

```sh
mvn -B -ntp clean verify
java -jar target/airraid-0.6.0.jar
```

The application runs without accounts or network access. Tests run headlessly; GitHub Actions also launches it under a virtual display, checks the game/navigation flow and uploads **real screenshots of the Settings and gameplay screens**.

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

## Implementation and validation

The `PreferencesStore` safely reads/writes versioned local settings, `UserPreferences` provides immutable validated snapshots, and `SettingsScreen` is an accessible Swing interface. `Window` coordinates persistence and settings navigation; `Gameplay`, `GameCanvas` and `GameOpening` apply volume, audio and reduced-motion choices immediately.

Tests cover settings round trips, schema corruption/invalid values, safe defaults, audio PCM volume and mute, reduced-motion effects, and a GUI navigation/persistence smoke. A real GUI screenshot is published as a workflow artifact rather than substituting conceptual renderings for an actual capture.

- [v0.6.0 implementation and acceptance](docs/V0_6_IMPLEMENTATION.md)
- [Version roadmap](docs/ROADMAP.md)
- [Progress tracker](https://github.com/MeiRR17/AirRaid/issues/3)
- [Canonical game rules](docs/V0_3_GAME_RULES.md)
- [Visual asset license inventory](docs/ASSET_LICENSE_AUDIT.md)
- [Public release blocker — 23 unverified visual assets](https://github.com/MeiRR17/AirRaid/issues/9)

**Next:** v0.7.0 quality/security/asset-rights hardening; then native installation packages, playtesting and v1.0.0 release gates. Public redistribution remains blocked until required asset rights are independently verified or resources are replaced.

## Historical screenshots (original game, not this version)

![Original AirRaid game](https://github.com/user-attachments/assets/629c0620-b8be-4053-b602-7febd0c01265)

![Original AirRaid game](https://github.com/user-attachments/assets/2a287550-8de7-4c5b-adc0-a0b2d8361b4a)
