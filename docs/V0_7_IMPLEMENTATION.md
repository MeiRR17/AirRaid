# AirRaid v0.7.0 — Quality and security implementation evidence

**Status:** Engineering changes proposed in a focused PR. **The production quality gate remains OPEN** until the 23 legacy visual-asset permissions, source-code license, fresh advisory scan and owner release approval are resolved. This document does not claim the v1.0.0 exit criteria are met.

## Changes to runtime and tests

1. `AppDiagnostics.java` reports a stable `AIRRAID_ERROR code=...` and short user-facing message on desktop startup failure, rather than dumping private stack traces or file paths into the console. `Main.java` catches unexpected startup runtime exceptions on the Swing EDT.
2. `PreferencesStore.java` refuses symlinks and non-regular files in the settings slot. It still uses a bounded read and an atomic sibling-file replacement, with safe fallbacks on unsupported filesystems.
3. `GameInvariantTest.java` runs seeded two-player input campaigns and deterministic comparison scenarios. It checks game clock monotonicity, canyon strike bounds, consistent total score against destroyed targets, finite positions, safe reset, no double-scored end events and collision edge geometry.
4. `SecurityRegressionTest.java` validates safe diagnostic codes, symlink refusal, and explicit missing asset errors. Both test classes are independent of GUI focus, network and OS audio devices.

## Source and packaged media provenance

`docs/asset-provenance.tsv` enumerates every bundled resource (23 original graphics, fonts, sprite variants, GIF, icon). Rows contain the canonical `Resource/...` classpath path, its original SHA-1 Git blob fingerprint and an **UNVERIFIED** redistribution status. SHA-1 here is a Git content identifier, **not** a cryptographic security claim or proof of ownership.

`tools/release_gate.py` uses Python 3 standard library only. It compares the manifest against all actual resource paths, recomputes Git blob hashes from source bytes, and optionally checks every built JAR entry byte-for-byte against its source. It reports the results in `target/release-readiness.json`.

- `--mode development`: passes only source/JAR integrity, and publishes **public_release_ready=false** with legal blockers.
- `--mode release`: fails when any image/font/GIF lacks an evidence reference, when the repository code license is not approved, when explicit human release approval is absent, or when source/package integrity fails.
- `tools/test_release_gate.py` exercises integrity success, tampered blob, altered JAR, untracked extra asset, path traversal, and synthetic approval mechanics. The approved fixture is solely a **test of the gate mechanism**, not approval of AirRaid's real assets.

## Dependencies, build workflow and security policy

- `tools/dependency_inventory.py` reads the Maven POM, requires pinned direct dependency/plugin versions, and exports `target/dependency-inventory.json` with `vulnerability_scan_performed=false`.
- The GitHub workflow pins `actions/checkout`, `actions/setup-java` and `actions/upload-artifact` to immutable commit SHAs retrieved from the official release tag refs on 2026-11-09. Permissions remain minimal (`contents: read`).
- `SECURITY.md`: responsible private vulnerability reporting and concise offline-game threat model.
- `CONTRIBUTING.md`: test instructions, privacy, safe asset/licensing requirements.
- `THIRD_PARTY_NOTICES.md`: explicitly **provisional**, contains no fabricated approvals.
- `docs/LICENSE_STATUS.md`: explains why an owner-unauthorized `LICENSE` is not fabricated.
- `docs/DEPENDENCY_AUDIT.md`: pinned component list and clear limitations of the inventory.

## CI contract and real screenshots

The bounded CI workflow runs Maven Java 21 unit tests, the Python gate unit tests, dependency inventory, source/JAR resource-integrity check, Xvfb GUI workflow smoke and screenshot upload to `airraid-v070-gui-captures`. The artifact also includes the two JSON evidence reports.

Reproduce from a clean checkout:

```sh
mvn -B -ntp clean verify
python3 -m unittest discover -s tools -p 'test_*.py' -v
python3 tools/dependency_inventory.py --pom pom.xml
python3 tools/release_gate.py --mode development --jar target/airraid-0.7.0.jar
```

The GUI smoke uses a temporary isolated home directory and can be run under `xvfb-run` on Linux. Evidence captures are **real Swing screenshots** under a virtual desktop; they are not visual designs.

## Outstanding production blockers (do not check off v0.7.0)

- **23/23 visual assets** still lack verifiable redistribution grants: [Issue #9](https://github.com/MeiRR17/AirRaid/issues/9).
- Owner has **not approved the source-code license**; code contributor/rightsholder review remains outstanding.
- A current **transitive dependency/security vulnerability scan** and legal notice review have not been conducted. A pinned component list is not proof of safety.
- Windows/macOS/Linux packaged install/uninstall smoke, signing, full accessibility testing and external human playtests remain release requirements.

The next *engineering* version can begin on `v0.8.0`, but **public production release remains blocked** until these conditions have documented evidence and the human release owner approves publishing.
