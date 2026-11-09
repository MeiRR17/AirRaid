# AirRaid desktop packaging — v0.8.0 (internal QA)

**Status:** Java 21 jpackage app-image creation is automated. No package is approved for public distribution. The 23 visual resources still lack verified redistribution rights, a code license has not been authorized, and release owner approval is missing (Issues #9 and #3).

## Supported build hosts and output types

Java 21 jpackage is host-native: a Linux machine cannot generate a genuine Windows or macOS installer. <https://docs.oracle.com/en/java/javase/21/docs/specs/man/jpackage.html>

| Build host | App image with embedded JRE | Optional installer | QA status |
| --- | --- | --- | --- |
| Linux (Ubuntu CI) | `target/desktop-qa/images/AirRaid` | `.deb` | App-image build + native-launcher self-test automated; installer build/install pending |
| macOS (manual workflow) | `target/desktop-qa/images/AirRaid.app` | `.dmg` | Workflow provided; runner execution/clean install pending |
| Windows (manual workflow) | `target/desktop-qa/images/AirRaid` | `.exe` | Workflow provided; runner execution/clean install pending |

Each image includes a native launcher, the precise verified game JAR and its own `jlink`-generated Java 21 runtime. It needs no separately installed Java for launch. The source `jar` retains all 23 legacy visuals; the build process **does not grant redistribution rights**.

## Developer commands

Install a full JDK 21 (must include `jpackage`), Maven 3.9+, Python 3.11+ and platform-native packaging tools when generating an installer.

```sh
mvn -B -ntp clean verify
python3 -m unittest discover -s tools -p 'test_*.py' -v
python3 tools/release_gate.py --mode development --jar target/airraid-0.8.0.jar
python3 tools/package_desktop.py --mode development --output target/desktop-qa
```

The last command verifies the source/JAR inventory, generates a self-contained host app-image, confirms the embedded `java.desktop` runtime, validates the exact game JAR SHA-256, and **executes the native launcher** using its headless `--self-test` mode. A JSON evidence report is written to `target/desktop-evidence-linux.json`, `desktop-evidence-macos.json` or `desktop-evidence-windows.json`.

For **internal validation only**, `--installer` additionally requests `.deb`, `.dmg` or `.exe` from jpackage on its native OS. Creation may need host-specific tools (for example `fakeroot`/`dpkg-deb` on Linux and WiX tooling on Windows). Missing prerequisites must fail rather than be silently ignored. This option never publishes installers.

There is an explicit [manual GitHub Actions workflow](../.github/workflows/desktop-packages.yml) for a **three-OS native app-image QA matrix**. It is opt-in to conserve runner quota, and **uploads JSON evidence only**. Select the optional create-installers control only on an appropriate QA machine; unsigned installers are discarded with the runner. An unsigned Windows/macOS installer is **not** a production distribution.

## Artifact integrity and rollback

The packaging report includes source input JAR SHA-256, the whole app-image's deterministic *content* tree SHA-256, file count and size, embedded runtime status, source commit, host OS and architecture, and explicit `codesigned=false`, `notarized=false`, `clean_install_uninstall_tested=false`. If an installer is built internally, its filename, SHA-256 and byte size are reported.

These hashes are local **QA evidence**, not a signed public release manifest. Do not upload binaries or tag a production release while legal/security approvals are outstanding. A future approved release should publish immutable checksums and provenance alongside its installers after review. Preserve the previous signed stable version and rollback documentation when a stable installer exists; currently **no production stable installer is available to roll back to**.

## Clean install / uninstall acceptance (not yet certified)

1. Obtain the output *from an authorized internal QA build*, not from a public release; record OS, CPU architecture, and JDK builder version.
2. Install on a fresh, unprivileged Windows, macOS or Linux test machine with **no separate JRE installed**. Verify the native launcher starts the actual graphical game, not only `--self-test`.
3. Disconnect the machine from the network and confirm an entire two-player match is playable offline.
4. Check Fire (Enter/Space), Pause, Settings, reduced motion, results, replay and Exit. Confirm preferences reside only under the user profile.
5. Confirm security tooling reports the **unsigned/not-notarized** state accurately; do not bypass OS safety warnings on non-QA machines.
6. Uninstall with the host's package manager. Verify launcher/app files are removed; document whether user preferences are retained separately.
7. Repeat for each supported OS and architecture, record detailed logs, sign-off and exact SHA-256. Until done, leave the v0.8.0 roadmap checkbox open.

## Legal and security blocks

A `--mode release` run fails when the existing `tools/release_gate.py` reports missing asset rights, code LICENSE or release approval. Even a passing future license gate does not substitute for vulnerability scanning, publisher signing, notarization or a real installation test. No public binary upload is performed by any workflow in this milestone.

See [release checklist](RELEASE_CHECKLIST.md), [asset inventory](ASSET_LICENSE_AUDIT.md), [release policy](../SECURITY.md), [code license status](LICENSE_STATUS.md), and [tracking issues #3](https://github.com/MeiRR17/AirRaid/issues/3) / [#9](https://github.com/MeiRR17/AirRaid/issues/9).
