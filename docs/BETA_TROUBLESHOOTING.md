# AirRaid beta — troubleshooting & support

**This is an internal QA build, not a public production release.** Report security-sensitive problems privately according to [SECURITY.md](../SECURITY.md). Ordinary gameplay issues can be reported through the [GitHub issue tracker](https://github.com/MeiRR17/AirRaid/issues) using the provided beta bug-report template.

## Common issues

**The game window does not open.** From a source checkout with JDK 21 and Maven 3.9+, run `mvn -B -ntp clean verify` and `java -jar target/airraid-0.9.0.jar`. For a built native app image, use its launcher with `--self-test` to verify its embedded Java runtime and media; this is not a graphical functional test. If the launcher displays `AIRRAID_ERROR code=STARTUP`, record OS, version and the stable code rather than private home paths or stack traces.

**The game seems slower than a real 60-second round.** In v0.9.0, the simulation uses measured monotonic elapsed time instead of assuming every repaint happens exactly after 16ms. Confirm that the game is not **Paused** or off-focus; pause time intentionally does not count.

**Enter or Space does not fire.** Click the game area; confirm the match is running (not on Results or Rules). Blue is Enter; Orange is Space; both use press/release edges and permit one active bomb per player.

**Sound is absent.** Sound is off by default. Use Settings (S) to enable it. It is normal for machines without an audio output device to remain silent; this should not crash gameplay.

**The menu image moves too much.** Enable **Reduce animation and visual effects** in Settings. This switches the animated menu to a still frame and disables transient hit effects.

**Saved settings fail to load.** The profile lives under the current user's `~/.airraid/preferences.properties`. AirRaid intentionally refuses symlinks and malformed files; use Restore defaults. Do not attach the original file if it contains sensitive local data.

**Installers report "unsigned" or fail OS reputation checks.** These packages are **not licensed or signed for public distribution**. Do not bypass Gatekeeper, SmartScreen or enterprise security controls on production systems. Follow the internal QA plan and obtain permission before any public release.

## Beta issue reporting checklist

Provide version `0.9.0`, OS/architecture, source commit or SHA-256 (if available), expected and actual outcomes, keyboard steps, whether the issue survives restart, and a screenshot with no personal information if useful. Classify P0 (crash/data/security blocking release), P1 (core game/install unusable), P2 (minor gameplay/UI), P3 (cosmetic). Link reproducible defects to the beta triage; do not count simulated playtests as human testing.
