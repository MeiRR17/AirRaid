# AirRaid

AirRaid is an offline, **two-player Java Swing arcade game**. Two automatic planes race to destroy targets using bombs. It is not an air-raid warning service.

**Development milestone: v0.4.0 — responsive interface and complete game flow.** This is not yet a packaged production release.

## Build and play

Requires JDK 21 and Maven 3.9+:

```sh
mvn -B -ntp clean verify
java -jar target/airraid-0.4.0.jar
```

The JAR includes the existing graphics, GIF and fonts. A graphical desktop is required to play; unit tests run headlessly, and CI verifies the GUI using Xvfb.

## Screens and navigation

The application now uses one **resizable** Swing window, with integrated screens:

- **Main menu:** Start, Rules, Exit. Press **Enter** to start or **H** to view instructions. Tab navigates menu buttons.
- **Game:** toolbar with Menu, Pause/Resume, Restart and Rules; a persistent, legible scores/strikes/timer status bar; a scalable arena.
- **Rules:** accessible, scrollable in-app reference. **Escape** or **Back** returns to the previous screen. Opening Rules during a game pauses it; returning never silently resumes.
- **Results:** dedicated view with winner/tie, both scores and strike totals, **Play again** and **Main menu**.

| Control | During game |
| --- | --- |
| **Enter** | Blue — fire (once per press) |
| **Space** | Orange — fire (once per press) |
| **P** | Pause / resume |
| **R** | Restart the match |
| **H** | Rules (pauses match) |
| **Escape** | Exit match and return to main menu |

The game pauses automatically on desktop focus loss; resume explicitly with **P** or the Resume button.

## Gameplay rules

Each bomb can destroy one intact target for **1–4 points**. A shot missing the target field causes a canyon strike. **Four strikes** immediately lose the match (simultaneous fourth strikes draw). Otherwise, the higher score at **60 seconds** wins; equal scores draw. The simulation and its rules remain independent of the window dimensions.

Full rule contract: [Instructions/About](Instructions/About) and [v0.3.0 Game Rules](docs/V0_3_GAME_RULES.md).

## Scaling and accessibility

The logical game world is fixed at **800×800**. `GameCanvas` preserves its aspect ratio and letterboxes it inside any available panel size; gameplay physics are unaffected by window resizing or DPI changes. The toolbar and status are Swing-managed layouts rather than fixed pixel positions. The Rules card scrolls at smaller window sizes.

This milestone improves keyboard access and basic readable screens. Full assistive-technology validation, persistent preferences, and accessibility refinements remain planned for v0.6.0.

## Architecture and checks

`GameModel` is a deterministic simulation, with no worker threads. `GameController` drives one EDT timer and stops it on pause, result, or exit. `Window` coordinates menu/rules/game cards; `Gameplay` manages game/result cards, keyboard bindings, status and lifecycle. `GameCanvas` renders scaled sprites from bundled assets.

`GameCanvasTest` checks landscape/portrait/zero-size layout, offscreen rendering and the results panel. `GuiSmoke` validates the menu→rules→game→rules→game→results→replay→menu flow, keyboard controls, resize, and repeated start/disposal without timer leakage.

- [Roadmap v0.1.0–v1.0.0](docs/ROADMAP.md)
- [v0.4.0 implementation and acceptance](docs/V0_4_IMPLEMENTATION.md)
- [Progress tracker](https://github.com/MeiRR17/AirRaid/issues/3)
- [Production checklist](docs/RELEASE_CHECKLIST.md)

**Next planned version:** v0.5.0 — visual polish, asset performance, sound/licensing. Native installers, cross-OS smoke and real two-person playtests remain to be done before production.

## Original screenshots — *not v0.4.0 captures*

![Original AirRaid screenshot](https://github.com/user-attachments/assets/629c0620-b8be-4053-b602-7febd0c01265)

![Original AirRaid screenshot](https://github.com/user-attachments/assets/2a287550-8de7-4c5b-adc0-a0b2d8361b4a)

## Asset rights

Before public redistribution, verify copyright and permitted use of bundled fonts, artwork and animation. See `docs/RELEASE_CHECKLIST.md`.
