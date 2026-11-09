# AirRaid v0.9.5 — Match statistics and real Results-screen evidence

**Internal beta engineering patch.** This change does not authorize public redistribution or claim that human beta sessions have happened.

## Implemented functionality

1. `GameModel` now maintains separate per-player **shotsLaunched** and **targetsHit** counters. A shot is counted only when `Bomb.launch` actually succeeds, never for OS key-repeat, paused-state input or an attempted launch while a previous bomb is active. A target is counted only after `Target.hit()` successfully claims an undestroyed target; contested simultaneous contacts cannot double-count one target.
2. `hitRatePercent` computes rounded `100 * targetsHit / shotsLaunched`, defined as zero when there are no launches. It is an **end-of-round conversion rate**, not a claim that all airborne projectiles reached the ground before the 60-second deadline. The counters reset on each new game, and remain immutable when state becomes RESULTS.
3. `ResultsScreen` displays two high-contrast **BLUE** and **ORANGE** performance cards showing shots, targets hit and percentage, with a visible rate definition. Existing score/strike and winner information stays on screen; accessible names distinguish the players. The layout uses a two-column grid suitable for compact windows.
4. The explicitly user-triggered local **Copy QA report** also contains these engine-derived statistics. No names, personal directories, device identifiers, timestamps, file writes or network access are introduced.
5. New deterministic `MatchStatisticsTest` cases cover zero-shot accuracy, repeated held fire, misses, one uniquely credited shared target, 50% shot-to-hit conversion, pause/results input rejection, replay resets, invalid player indices, and visual/report consistency. The existing rule and beta campaigns remain active.
6. `GuiSmoke` fires both players in an actual model round, verifies the populated stats and QA report, and captures a real `620×480` Results screenshot (`airraid-v095-ci-results-stats.png`) under Xvfb. It still verifies replay, local settings, other screens and lifecycle cleanup.

## Quality and release gates

For the PR, run Java 21 Maven unit tests and package creation, Python source/JAR and beta readiness gates, the Linux bundled-runtime `jpackage` launcher smoke and Xvfb GUI capture upload. Passing CI only establishes **engineering behavior** in the tested environment.

The following remain unresolved and must not be checked off through this version:

- **0 verified real human beta sessions** out of three required; no actual witnessed two-human one-keyboard playtest or P0/P1 triage sign-off ([issue #15](https://github.com/MeiRR17/AirRaid/issues/15)).
- Windows/macOS/Linux clean native install, offline full gameplay and uninstall verification ([issue #13](https://github.com/MeiRR17/AirRaid/issues/13)).
- **23 legacy bundled visual resources** have no verified redistribution grants; owner-approved source-code license, release authorization and current dependency security review remain outstanding ([issue #9](https://github.com/MeiRR17/AirRaid/issues/9)).

**No public installer, release tag, production label or external human-test claim is created.**
