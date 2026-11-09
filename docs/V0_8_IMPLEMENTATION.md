# AirRaid v0.8.0 — Desktop packaging foundation

This milestone delivers native packaging **code and test infrastructure**, not a public installer or production release.

## Implemented

- `Main --self-test` exercises the native launcher, bundled Java runtime, original graphics/font resources, and deterministic game model **without needing a display or network**. It writes `AIRRAID_PACKAGE_SELFTEST_PASS` on success.
- `tools/package_desktop.py` performs a fail-closed source/JAR integrity check, creates a clean staging directory with only the verified JAR, and invokes JDK 21 `jpackage` on the current OS to produce a native app image with an embedded `jlink` runtime.
- Package verifier checks the OS-specific launcher and runtime modules path, exact JAR SHA-256 and complete app-image content SHA-256. It writes `target/desktop-evidence-<platform>.json` with architecture, version, file count, byte size, signing and approval status.
- Optional host-native `--installer` mode requests a Linux `.deb`, macOS `.dmg` or Windows `.exe` through jpackage. **The installer option is an internal-only workflow and must not be interpreted as a certified install/uninstall test.**
- `tools/test_package_desktop.py` tests Windows/macOS/Linux path expectations, SHA-256 consistency, missing runtime/tampered JAR rejection, a simulated application image and release-mode refusal.
- Normal Java 21 PR CI runs the Python tests, game tests, source/JAR integrity, Linux jpackage app-image and native launcher self-test, plus the existing Xvfb GUI test and actual game/settings screenshot artifact.
- An **on-demand** GitHub Actions matrix includes three OS runners, builds and smoke-tests app images, and uploads metadata only. It does **not** publish binaries, invoke GitHub Releases or create a production tag.
- The Maven project and JAR advance from `0.7.0` to `0.8.0`.

## No release authorization

`tools/release_gate.py` still reports missing permissions for **23/23 visual assets**, no approved source-code LICENSE, and no owner release approval. Any public native installer would redistribute bundled assets without verified permission and is prohibited until review. Code signing and macOS notarization are not performed. The explicit `public_distribution_allowed` metadata remains `false`.

## Acceptance evidence still outstanding

- Windows and macOS native app-image runs must be completed in the on-demand matrix and verified.
- Native `.deb`, `.dmg`, `.exe` packages must be built, examined and clean-installed/removed on each supported OS. The Linux app-image smoke alone does not fulfill those requirements.
- Offline two-person full match without a separately installed JRE, requested CPU architectures, launcher icons/OS metadata, signature/notarization status and full uninstall audit.
- Rights, software license, current transitive CVE scan, notices, original assets or replacements, release-owner approval.

**Roadmap status:** v0.8.0 implementation can be merged after CI; do not check off the full distribution acceptance gate or publish a production tag until the above evidence is available.

Full commands and test protocol are in [DESKTOP_DISTRIBUTION.md](DESKTOP_DISTRIBUTION.md).
