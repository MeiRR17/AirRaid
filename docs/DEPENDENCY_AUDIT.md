# AirRaid v0.7.0 — Build supply-chain component inventory

**Scope:** Maven metadata in the current `pom.xml` plus the Java 21 toolchain and three CI actions. This inventory is **not** a live vulnerability scan and is **not** evidence that zero critical/high CVEs exist.

| Component | Purpose | Pinned version |
| --- | --- | --- |
| Java | Build target / development runtime | 21 |
| JUnit Jupiter | Test dependency only | 5.11.4 |
| maven-compiler-plugin | Maven compilation | 3.14.0 |
| maven-surefire-plugin | JUnit test execution | 3.5.3 |
| maven-jar-plugin | Runnable JAR | 3.4.2 |
| actions/checkout | CI checkout | SHA `11bd71901bbe5b1630ceea73d27597364c9af683` (v4.2.2) |
| actions/setup-java | CI Java setup | SHA `c5195efecf7bdfc987ee8bae7a71cb8b11521c00` (v4.7.1) |
| actions/upload-artifact | CI screenshot / evidence upload | SHA `ea165f8d65b6e75b540449e92b4886f43607fa02` (v4.6.2) |

The action SHA values above were retrieved from the GitHub Git refs for the corresponding tags on 2026-11-09. The CI workflow pins these immutable references rather than floating major-version tags.

The versioned `tools/dependency_inventory.py` script produces `target/dependency-inventory.json`; this lists explicitly declared Maven versions and fails if a direct dependency/plugin version is missing or uses a snapshot/dynamic label. **It does not enumerate every transitive JUnit dependency or contact an advisory database.** The CI artifact includes this machine-readable inventory and a release-readiness report.

A complete v1.0 supply-chain gate still requires a fresh dependency/security advisory check including transitive and plugin dependencies, license-notice review, and OS-specific runtime/build-tool checks. Security claims must use dated evidence rather than the existence of this inventory alone.
