# AirRaid

AirRaid is an **offline two-player desktop arcade game** written in Java 21 and Swing. It is not an emergency-alert service.

**Current milestone: v0.2.0 — deterministic game engine and lifecycle.** It is a development build, not a production release.

## Build and run

Requires **JDK 21** and **Maven 3.9+**:

```sh
mvn -B -ntp clean verify
java -jar target/airraid-0.2.0.jar
```

The JAR includes all original graphics and fonts. A desktop graphical session is required to play; engine and resource tests run headlessly. The CI build also launches the game under a virtual display to check the GUI lifecycle.

## Controls and game flow

| Player | Key |
| --- | --- |
| Blue | Enter — fire |
| Orange | Space — fire |
| Either | P — pause/resume |

Use **Start** to begin, **Pause/Resume** to control the simulation, **Restart** for a fresh match, and **Menu** to stop and return to the title screen. Each player has at most one active bomb. Targets score 1–4 points once each. The existing time limit is 60 seconds; when the time expires the winner/tie is displayed.

### Architecture

- `GameModel` owns the simulation state, planes, bombs, targets, scoring and fixed time steps. It is deterministic and has **no background worker threads**.
- `GameController` drives a **single Swing Timer** on the EDT, and controls start/pause/resume/disposal.
- `Gameplay` renders from the model and maps keyboard actions to model input. `Window` owns menu/game transitions and disposes old simulations.
- `Assets` loads original binary media from the classpath; the Maven build embeds it in the runnable JAR.

**Known limits:** four-canyon-hit rule in the old `Instructions/About` does not yet match the game engine (v0.3.0); final polishing, accessibility and installer packaging are later milestones. This version intentionally does **not** claim public production readiness. No network accounts are needed.

## Plans and evidence

- [Production roadmap](docs/ROADMAP.md)
- [Legacy baseline audit](docs/BASELINE_AUDIT.md)
- [v0.2.0 engine implementation notes](docs/V0_2_IMPLEMENTATION.md)
- [Release gates](docs/RELEASE_CHECKLIST.md)
- [Version tracker](https://github.com/MeiRR17/AirRaid/issues/3)

## Screenshots from the original project (not this new build)

![Original AirRaid game](https://github.com/user-attachments/assets/629c0620-b8be-4053-b602-7febd0c01265)

![Original AirRaid game](https://github.com/user-attachments/assets/2a287550-8de7-4c5b-adc0-a0b2d8361b4a)

## Licensing

Before redistributing bundled images, animation or fonts in a public binary, verify each asset's rights and attribution. See the release checklist.
