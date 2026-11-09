# AirRaid v0.6.0 — Accessible local preferences and keyboard usability

## Implementation
- Introduces immutable `UserPreferences(soundEnabled, volumePercent, reducedMotion)` with validation, safe defaults, and explicit schema version 1.
- `PreferencesStore` persists those three non-sensitive options to `~/.airraid/preferences.properties`. Reads reject corrupted, oversized, unrecognized-schema and invalid values. Writes use a sibling temporary file and `ATOMIC_MOVE` when available, falling back to replace; failures show a warning without crashing the game.
- A new integrated `SettingsScreen` exposes sound checkbox, volume 0–100 slider, reduced-motion checkbox, keyboard instruction panel, recovery text, Restore defaults and Back. It is keyboard operable and has accessibility names.
- Main Menu adds a Settings button and **S** shortcut; Gameplay adds Settings button and **S** shortcut. Entering Settings pauses active gameplay; Back returns without unpausing. Tab/Shift+Tab navigate interactive Settings controls and Escape returns.
- `Window` loads preference values at startup, propagates changes to the menu and active match, saves them immediately, and exposes a testing injection constructor so GUI tests cannot write real user profiles.
- `GameOpening` uses the first still GIF frame when reduced motion is enabled rather than painting moving animation. `GameCanvas` suppresses temporary particle effects; game model, scores and physics remain unchanged.
- `SoundEffects` software-scales PCM waveforms for 0–100% volume; audio defaults off. The sound toggle can persist through sessions. Missing audio output is handled without crashing or misreporting actual playback.
- Maven and JAR advance to `0.6.0`.

## Tests and evidence
- `PreferencesStoreTest`: clean-profile defaults, persist/reopen, schema mismatch, malformed boolean/volume, corrupt/oversized storage, nonfile path, cleanup and immutable value bounds.
- `AccessibilityPreferencesTest`: settings controls & immediate updates, accessible labels, visual effects reduced-motion switch, software PCM mute/gain, audio without hardware.
- Existing deterministic rules, asset and canvas unit tests remain.
- Xvfb `GuiSmoke`: full menu/settings/game/rules/results/replay lifecycle, keyboard S/P, settings pause discipline, persisted profile load and two real PNG captures.
- CI artifact `airraid-v060-gui-captures` contains `airraid-v060-ci-settings.png` and `airraid-v060-ci-gameplay.png`; both are captured from the real Java Swing application.
- Acceptance requires `mvn -B -ntp clean verify`, packaged JAR, and bounded 20-second Xvfb smoke.

## Limitations and production gates
- This is a functional keyboard usability and basic accessibility milestone, not a formal screen-reader/WCAG conformance audit. Hardware and OS-specific assistive technology validation remains open.
- The current preference file is a small local unencrypted record of non-sensitive display/sound choices. It contains no user identifiers, credentials or external analytics.
- No actual platform-dependent volume mixer is required, because volume is applied to PCM before playback.
- The **23 legacy visual assets** still have unverified redistribution permissions. [Issue #9](https://github.com/MeiRR17/AirRaid/issues/9) remains a public release blocker. This does not prevent merging v0.6.0 development functionality but must block a claimed production v1.0.0 release.
