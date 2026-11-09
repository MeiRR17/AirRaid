# AirRaid

**AirRaid** is an offline, local two-player Java Swing arcade game, not an emergency alert application. Two automatically moving planes compete to destroy targets worth 1–4 points.

> Version **0.1.0 build foundation**: the source layout, Maven build and resource loading have been modernized. This is not a production release. Known game-loop and rules defects remain for later milestones.

## Build and play

Requirements: **JDK 21** and **Maven 3.9+**. From the repository root:

```sh
mvn clean verify
java -jar target/airraid-0.1.0.jar
```

Maven compiles the sources in `src/main/java`, packages the graphics/fonts from `src/main/resources/Resource`, and runs headless resource-loading tests. No IDE or repository working directory is required to run the generated JAR. Desktop GUI needs a graphical session; unit tests run headlessly.

### Controls

| Player | Fire |
| --- | --- |
| Blue | **Enter** |
| Orange | **Space** |

The current source uses a 60-second match. The older project instructions also mention losing after four canyon hits, but that logic is not consistently implemented. v0.3.0 will establish a canonical rule set.

## Roadmap and tracking

- [Versioned roadmap — v0.1.0 through v1.0.0](docs/ROADMAP.md)
- [Baseline audit and known issues](docs/BASELINE_AUDIT.md)
- [Production release checklist](docs/RELEASE_CHECKLIST.md)
- [Progress tracking issue #3](https://github.com/MeiRR17/AirRaid/issues/3)

Next: **v0.2.0 — deterministic engine, safe game lifecycle and thread safety**. In particular, the legacy planes and bombs still run background threads; this milestone does not claim to fix them.

## Game screenshots (from the original project)

![Original gameplay screenshot](https://github.com/user-attachments/assets/629c0620-b8be-4053-b602-7febd0c01265)

![Original game screenshot](https://github.com/user-attachments/assets/2a287550-8de7-4c5b-adc0-a0b2d8361b4a)

## Distribution and licensing

There is currently no approved production installer. Before distributing third-party fonts or artwork publicly, verify the respective rights and add license/attribution metadata. See `docs/RELEASE_CHECKLIST.md`.
