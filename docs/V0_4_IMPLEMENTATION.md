# AirRaid v0.4.0 — Integrated screens, resizing and keyboard flow

## Scope implemented

1. **One resizable window** replaces the former fixed 800×800 desktop frame. It uses `CardLayout` to coordinate Main Menu, Rules and Gameplay. Minimum size is 620×480, preferred size 960×800.
2. **In-app rules** replace the disconnected legacy rules popup. Main Menu offers Start, Rules and Exit. In-game Rules opens from the toolbar or H key, pauses the simulation, and Back/Escape returns to the previous screen without auto-resuming.
3. **Complete round flow** uses game `CardLayout` with a scalable arena and a dedicated Results card. Winner/tie, both scores/strike counts, Play again and Main menu are visible after the model ends a match.
4. **Keyboard reliability** keeps Enter and Space mapped to different players without toolbar buttons stealing them. P pauses, R restarts, H opens rules, Escape exits a match. Menu supports Enter to start and keyboard focus/Tab on actionable buttons.
5. **Responsive layout** removes `null` layout and absolute button bounds. Swing lays out the header toolbar, arena and persistent status bar. The `GameCanvas` renderer maps fixed 800×800 logical game coordinates to an undistorted centered viewport.
6. **Focus safety** pauses the game on window focus loss, preserves pause through rules navigation, and refocuses game/replay controls after screen transitions.
7. All existing game rules (v0.3.0), source assets, JDK 21 and Maven conventions remain unchanged. Version and JAR name advance to 0.4.0.

## Test plan

```sh
mvn -B -ntp clean verify
java -jar target/airraid-0.4.0.jar
```

In CI the focused Xvfb GUI smoke also verifies:
- Visible Start/Rules/Exit menu controls
- Menu → Rules → Back → Menu
- Window is resizable and arena retains square geometry at 730×580
- Enter and Space independently fire; P resumes a paused game
- Game → Rules pauses, returning retains pause
- R restarts, ten game start/disposal cycles do not retain simulation timers
- End-of-round results card shows tie, timer stops, Play again resets gameplay
- Escape returns to menu and disposes the active match

Headless JUnit adds viewport geometry, zero-sized rendering safety, an offscreen paint test, and the results view. Previous model scoring and resource tests remain.

## Remaining production work

- Full manual visual checks at OS DPI settings 100%, 150%, and 200% and on Windows/macOS/Linux are **not** replaced by Xvfb.
- Audio, asset rights audit, native installers, user preferences, and full assistive-technology testing belong to later versions.
- A green CI and a source-correct implementation permit merging **this milestone**, not tagging a public 1.0 production release.
