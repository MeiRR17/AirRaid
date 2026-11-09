# v0.2.0 — Deterministic engine and lifecycle

**Target branch:** `master`. **Scope:** Engine safety, resource-safe Swing lifecycle, deterministic test coverage.

## Replaced / refactored
- `Player`, `Bomb` and `Target` are plain mutable game entities, not `Thread` subclasses. No entity launches its own animation loop.
- `GameModel` now owns a complete match snapshot: planes, projectile slots, target durability, scoring, input edge state, time remaining and phase.
- `GameState` explicit phases: `MENU → RUNNING ↔ PAUSED → RESULTS` and `STOPPED` on disposal.
- `GameController` is the one EDT-bound Swing `Timer`. Pausing/disposal stops the timer, and replay resets the same model without accumulating worker processes.
- `Gameplay` now displays model snapshots only, with keyboard bindings for both players; no off-EDT labels, unbounded loops, or filesystem I/O in the paint path.
- `Window` creates the game **only after Start** and calls disposal when navigating back to the menu or closing the frame.
- `GameOpening` contains a working Start button and bundled animation, replacing the unused legacy placeholder.
- Bumped Maven build version to `0.2.0`.

## Design invariants
1. Simulation has one owner at a time. Under Swing, that owner is the EDT; the pure model itself has no UI dependencies and can be tested directly.
2. The engine receives elapsed time in milliseconds and advances in substeps no larger than 16 ms.
3. Each player can hold one active bomb; destroyed targets do not award points again.
4. Start/restart resets score, time, target flags, bomb state, and held keys.
5. Pause/resume does not change the simulation while paused.
6. Returning to menu / exit stops the Swing timer and stops game mutations.
7. Asset Git blobs from v0.1.0 are unchanged.

## Tests
- `mvn -B -ntp clean verify` — deterministic model tests, resource tests.
- CI `xvfb-run -a timeout 20s java -cp target/classes:target/test-classes GuiSmoke` — verifies graphical Start, Pause, Resume, Restart, Menu navigation and timer shutdown.
- The CI workflow checks that the executable JAR embeds resources and Main.

## Deliberately deferred to v0.3.0+
- The old `Instructions/About` four-canyon-hit rule was never coded. Product rules and tie priority must be formalized in v0.3.0; v0.2.0 keeps 60 seconds.
- Full physics balancing, exact input-response tuning, controller support, scaling, audio, native installers, gamepad, licensing audit, and multi-OS regression tests are future milestones.
- A successful headless Xvfb smoke confirms basic launch and navigation, **not** visual pixel-quality or usability on every operating system.

## Release gate
Do not mark v0.2.0 complete before CI compiles, all model tests pass, the GUI smoke succeeds and the PR has merged. Keep v1.0.0 production gates unchanged.
