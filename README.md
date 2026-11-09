# AirRaid

An **offline two-player Java Swing arcade game**. Two automatic aircraft compete to destroy targets using bombs, with a four-miss limit and a 60-second clock. AirRaid is **not** a real-world emergency alert system.

**Development version: v0.5.0 — rendering, responsive visual feedback and optional sound.** Not a production distribution.

## Build & launch

Requires JDK 21 and Maven 3.9+:

```sh
mvn -B -ntp clean verify
java -jar target/airraid-0.5.0.jar
```

The runnable JAR bundles the game's original visual assets. Maven tests work without a desktop; the GUI requires a graphical session. CI runs a bounded Xvfb smoke test and attaches a real game-window screenshot to the workflow run.

## Gameplay and controls

| Key | Action |
| --- | --- |
| Enter | Blue player's bomb |
| Space | Orange player's bomb |
| P | Pause/resume |
| R | Restart |
| H | Rules |
| Escape | Main menu |
| M | Toggle sound (off by default) |

On-screen buttons provide Start, Rules, Exit, Pause, Restart, Sound On/Off, replay and Main Menu. The game also pauses on window focus loss.

Targets are worth **1–4 points**, and each intact target can award points once. Four canyon strikes from missed bombs lose the match. If both players hit four in the same simulation step, the outcome is a draw. If no strike limit is reached, highest score wins after 60 seconds.

## Improvements in v0.5.0

- **Shared resource cache:** `Assets` loads each sprite/font source only once, even when the game is restarted repeatedly. Fonts are derived at fixed sizes outside the paint loop.
- **Visible feedback:** the game now displays short-lived flight-shot rings, `+points` hit popups and `MISS` markers. Graphics are drawn in the existing game canvas; no extra render timers or model threads were introduced.
- **Original synthesized audio:** four concise PCM cues for firing, hits, misses and round completion. The user explicitly enables them with the **Sound: Off** button or **M**. No prerecorded or third-party sound files are embedded. If the machine has no suitable audio output, the game remains playable and audio is unavailable.
- **Safe lifecycle:** feedback is cleared on replay and disposal; audio clips are closed when the game is left.
- **License transparency:** all 23 previously bundled visual assets are catalogued in [the asset inventory](docs/ASSET_LICENSE_AUDIT.md). Their redistribution rights are **unverified**, which remains a blocker for public production packages.

Game screens remain resizable and retain their logical 800×800 aspect ratio. A real graphical CI screenshot may be found as a workflow artifact, rather than substituting concept images for the running app.

## Development and verification

`GameModel` is a deterministic pure simulation. It produces immutable `Feedback` records that `Gameplay` consumes to trigger optional audio and canvas effects. Neither sound nor particle drawing affects scores, collisions or match outcomes. `SoundEffects.synthesize` can be unit-tested without opening a real audio device.

- [v0.5 implementation evidence](docs/V0_5_IMPLEMENTATION.md)
- [Asset license inventory and blockers](docs/ASSET_LICENSE_AUDIT.md)
- [Game rules](docs/V0_3_GAME_RULES.md)
- [Full production roadmap](docs/ROADMAP.md)
- [Version tracker](https://github.com/MeiRR17/AirRaid/issues/3)
- [Release checklist](docs/RELEASE_CHECKLIST.md)

**Next:** v0.6.0 (preferences, accessible controls, motion/audio settings and keyboard-only testing), then hardening, installer packaging and real playtests. There is no v1.0.0 production claim or release tag yet.

## Original game screenshots (before v0.4 and v0.5)

![Original AirRaid](https://github.com/user-attachments/assets/629c0620-b8be-4053-b602-7febd0c01265)

![Original AirRaid](https://github.com/user-attachments/assets/2a287550-8de7-4c5b-adc0-a0b2d8361b4a)
