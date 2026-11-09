# AirRaid

An offline, **two-player Java Swing arcade game**. Two automatically flying planes compete to destroy targets while avoiding canyon strikes. AirRaid is a game, **not** an emergency alerting application.

**Development version: v0.3.0 — gameplay rules and scoring.** This is not yet a production-ready desktop distribution.

## Build and run

Install **JDK 21** and **Maven 3.9+**, then run:

```sh
mvn -B -ntp clean verify
java -jar target/airraid-0.3.0.jar
```

The runnable JAR bundles all graphics and fonts. For a GUI smoke test, GitHub Actions launches the application under Xvfb and checks Start, Pause, Resume, Restart, Menu and disposal. Normal unit tests run headlessly.

## Controls

| Player | Action |
| --- | --- |
| Blue | **Enter** — launch bomb |
| Orange | **Space** — launch bomb |
| Both | **P** — pause/resume, **R** — restart |
| Menu | Buttons: **Start**, **Pause/Resume**, **Restart**, **Menu** |

Hold-and-repeat does not auto-fire. Each player can have only one live bomb at a time.

## Rules (v0.3.0)

- **Targets:** four score tiers worth 1–4 points, arranged in eight rows. An intact target may award points only once.
- **Canyon strikes:** a bomb that exits the left, right or lower edge without destroying a target gives its owner **one strike**. A scored hit does not add a strike.
- **Four-strike loss:** reaching four strikes immediately loses the round, regardless of the score. If both players reach four on the same simulation step, the result is a draw.
- **60-second clock:** if no one has lost by strikes, the player with the most points wins at the end; equal scores are a tie.
- **Simultaneous events:** all events in a simulation step count before declaring a winner. Strikes take precedence over the timer at the same step; an exact two-bomb hit on the same target awards it once, with alternating tie priority beginning with Blue.
- **Pause/restart:** pause freezes simulation; restart completely clears points, strikes, targets and held input.

The complete specification, edge-case rules, and tests are documented in [Game Rules v0.3.0](docs/V0_3_GAME_RULES.md) and [Instructions/About](Instructions/About).

## Architecture and tests

`GameModel` owns deterministic scoring, collision events and match outcomes. `GameRules` defines bounds, target tiers, the time limit and swept collision geometry. `GameController` drives one Swing EDT timer and stops on a result. `Gameplay` displays two scores, the remaining time, and both canyon strike counts. The test suite includes boundary exits, score durability, simultaneous collisions, ties, pause and restart, as well as GUI lifecycle smoke tests.

## Roadmap

- [Production roadmap to v1.0.0](docs/ROADMAP.md)
- [Progress tracker](https://github.com/MeiRR17/AirRaid/issues/3)
- [Production release checklist](docs/RELEASE_CHECKLIST.md)
- [Legacy baseline audit](docs/BASELINE_AUDIT.md)

**Next:** v0.4.0 (polished game flow, scalable layout, focus/accessibility and responsive UI). Cross-platform native installers, rights review, and real user playtests still remain before v1.0.0.

## Original project screenshots (not screenshots of v0.3.0)

![Original game screenshot](https://github.com/user-attachments/assets/629c0620-b8be-4053-b602-7febd0c01265)

![Original game screenshot](https://github.com/user-attachments/assets/2a287550-8de7-4c5b-adc0-a0b2d8361b4a)

## License

Before distributing a public installer, verify rights to each bundled sprite, font, GIF and image; see the [release checklist](docs/RELEASE_CHECKLIST.md).
