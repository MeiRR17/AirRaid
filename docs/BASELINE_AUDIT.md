# AirRaid — Source Baseline Audit (2026-11-09)

**Inspected revision:** `MeiRR17/AirRaid master@238cb9ee422489b35e1a7cd3093b2319df83fce7`.  
**Method:** Read source and repository tree using the connected GitHub repository. **No compilation, interactive run, or OS smoke test performed.**  
**Evidence links:** [tree](https://github.com/MeiRR17/AirRaid/tree/master), [Gameplay](https://github.com/MeiRR17/AirRaid/blob/master/src/Gameplay.java), [Player](https://github.com/MeiRR17/AirRaid/blob/master/src/Player.java), [Bomb](https://github.com/MeiRR17/AirRaid/blob/master/src/Bomb.java), [Window](https://github.com/MeiRR17/AirRaid/blob/master/src/Window.java), [GameOpening](https://github.com/MeiRR17/AirRaid/blob/master/src/GameOpening.java), [rules](https://github.com/MeiRR17/AirRaid/blob/master/Instructions/About).

## Inventory
- Java Swing game: `src/Main.java`, `Window.java`, `GameOpening.java`, `Gameplay.java`, `Player.java`, `Bomb.java`, `Target.java`, `RulesWindow.java`, `Utils.java`.
- Static assets in `Resource/`: aircraft sprite alternatives, bombs, targets, score hearts, fonts, background, GIF, icon.
- Controls written in `Instructions/About`: Enter (blue), Space (orange).
- `master` is the default branch; no GitHub release objects seen at inspection. Only one pre-existing PR was found, merged in early November 2026 to remove IDE metadata.
- No `pom.xml`, `build.gradle`, `src/test` tree or `.github/workflows` paths in inspected `master` tree.

## Evidence-based risks and remediation order

| Priority | Finding | Source evidence | Target |
| --- | --- | --- | --- |
| P0 | Game entities extend `Thread` with `while (true)` loops and no shutdown contract. | `Player.run()`; `Bomb.run()` | v0.2.0 |
| P0 | Game simulation and mutable scores/labels are changed by both worker thread and Swing timer; Swing widgets are updated outside the EDT. | `Gameplay` constructor, `handleCollision()`, `gameTimer` | v0.2.0 |
| P0 | `Gameplay` constructor starts plane, bomb, target and game-loop threads and starts clock before Start is clicked. | `Window` creates `new Gameplay(...)` before Start; `Gameplay` constructor calls `start()` | v0.2.0 |
| P0 | Background path mismatches repository filename capitalization. Relative paths assume process working directory equals repository root. | `Gameplay` uses `Resource/General/Background.png`; tracked file is `Resource/General/background.png` | v0.1.0–v0.2.0 |
| P0 | Source-level game contract and implementation disagree on four canyon hits and termination. | `Instructions/About` describes four hits; `Gameplay` uses a 60,000 ms timer | v0.3.0 |
| P0 | No reproducible build or tests are present in current tree. | root tree contains plain `src/` and `Resource/` | v0.1.0, v0.7.0 |
| P1 | Static mutable score/collision counters will persist across constructed game instances; pause/restart lifecycle absent. | `Gameplay.blueScore`, `orangeScore`, `blueChecker`, `orangeChecker` | v0.2.0–v0.4.0 |
| P1 | Font file is opened and parsed during each paint, with fatal exception on I/O failure. | `Gameplay.paintComponent` | v0.2.0 |
| P1 | Opening screen GIF uses placeholder `path/to/your/gif/file.gif`; layout mixes independent absolute panels. | `GameOpening` | v0.4.0 |
| P1 | Keyboard state is cleared on exact coordinate equality and conditional `else if` branch; this can fail if movement skips coordinates. | `Gameplay.handleBombReturn`, `keyReleased` | v0.3.0 |
| P1 | Collision/reload checks occur inside a nested grid traversal (8 × 38 positions), increasing opportunities for repeated processing. | `Gameplay.checkCollisions` | v0.3.0 |
| P1 | Rendering relies on absolute 800×800 dimensions and overrides JFrame `paint`. | `Window`, `Gameplay` | v0.4.0–v0.5.0 |
| P1 | No verified licensing/provenance for bundled art and fonts in inspected tree. Two font filenames point to the same Git blob. | `Resource/` tree | v0.5.0–v0.7.0 |
| P1 | No installer, rollback metadata, checksums or release artifacts. | inspected repository tree and release listing | v0.8.0–v1.0.0 |

## Preserve during refactor
- Existing art and two-player identity unless a specific asset's license needs replacement.
- Player controls: blue Enter, orange Space.
- Four target values: 1–4, consumed once.
- Existing one-minute match as the current runtime behavior until game-rules reconciliation.
- No network dependencies or new user accounts.

## First safe actions
1. Run `javac -d out src/*.java` as a provisional baseline compile command (not yet verified).
2. Use a feature branch to add Maven and move sources/assets, verify a clean checkout works on JDK 21.
3. Refactor simulation before adding gameplay features.
4. Define and test canyon-hit semantics before claiming rules parity.
5. Collect rights information for original sprites/GIF/font before any public binary distribution.

**Release warning:** Do not describe the application as production-ready on the basis of a source review or documentation changes. The current audit is a worklist, not a QA certification.
