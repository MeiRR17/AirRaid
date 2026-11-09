# AirRaid

AirRaid is a local **two-player Java Swing arcade game**. Two automatically moving planes compete to destroy targets on a canyon. It is an arcade game, **not an emergency-alert application**.

> **Development status:** Legacy prototype. The production roadmap is planned; the app is not yet release-ready.

## Controls and objective

| Team | Action |
| --- | --- |
| Blue | Press **Enter** to launch a bomb |
| Orange | Press **Space** to launch a bomb |

Targets award 1, 2, 3 or 4 points. The current source has a 60-second countdown; the original project instructions also describe a four-canyon-hit loss rule that is **not yet implemented** consistently. Rules will be reconciled in v0.3.0.

## Run current prototype (development only)

Requires a local JDK and an IDE or terminal. From the repository root, the intended manual compilation workflow is:

```sh
mkdir -p out
javac -d out src/*.java
java -cp out Main
```

**Not yet validated on a clean machine.** Current code loads assets using paths relative to the working directory; at least one background path differs by letter case from the actual filename. A reproducible Maven build, bundled resources, tests and platform-native installers are planned for future versions.

## Development plan

- [Full versioned roadmap: v0.1.0 → v1.0.0](docs/ROADMAP.md)
- [Source-based audit and technical priorities](docs/BASELINE_AUDIT.md)
- [Release checklist / production gates](docs/RELEASE_CHECKLIST.md)
- [Progress tracking issue #3](https://github.com/MeiRR17/AirRaid/issues/3)

The next implementation milestone is **v0.1.0: repeatable JDK 21/Maven build and project structure**, followed by safe simulation and gameplay correctness. Use PRs targeting `master` and tag only validated versions.

## Screenshots from the original project

![Original gameplay screenshot](https://github.com/user-attachments/assets/629c0620-b8be-4053-b602-7febd0c01265)

![Original game screenshot](https://github.com/user-attachments/assets/2a287550-8de7-4c5b-adc0-a0b2d8361b4a)

## Releasing

There is currently no official production release. Before distributing any installer publicly, verify rights to all bundled sprites, background images, animation and font files. The release requirements and acceptance criteria are documented above.
