# Contributing to AirRaid

AirRaid is a Java 21, Maven 3.9+ desktop game. PRs should target the default branch `master`, not `main`. Use an isolated feature branch for each roadmap milestone.

Run the minimum checks before opening a PR:

```bash
mvn -B -ntp clean verify
python3 -m unittest discover -s tools -p 'test_*.py' -v
python3 tools/dependency_inventory.py --pom pom.xml
python3 tools/release_gate.py --mode development --jar target/airraid-0.7.0.jar
```

The GUI smoke needs a graphical session; CI runs it in Xvfb. Test new rule changes in `GameModel` directly without Swing, and cover pause/reset/end-of-match conditions. Never add an unbounded worker thread or mutate Swing controls from a non-EDT worker.

The game is intended to work offline. Avoid unnecessary network, analytics, credentials or telemetry. Security defects should be reported **privately** according to `SECURITY.md`. Never commit private data, machine-specific paths, tokens, personal log files or raw stack traces containing sensitive information.

**Asset contributions:** New images, sprites, GIFs and fonts require written source/provenance and compatible redistribution permission *before* packaging. The old 23-resource bundle is not licensed for public redistribution merely because the repository is public. Update `docs/asset-provenance.tsv`, the asset inventory and the appropriate verified attribution when assets change.

**Code license:** The repository owner has not yet approved a source-code license. Do not add a license statement or assume that public GitHub access implies permission to redistribute. See `docs/LICENSE_STATUS.md`.

A merged PR does not imply readiness to publish installers. Release mode of `tools/release_gate.py` must pass after legal approvals and clean platform packaging evidence are supplied. Attach passing CI evidence and report remaining blockers explicitly.
