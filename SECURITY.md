# AirRaid security and vulnerability reporting

AirRaid is an **offline, local two-player desktop game**, not a real-world emergency-alert product. The supported development runtime is JDK 21. No accounts, online multiplayer, analytics service or server connection are required for gameplay.

## Reporting security problems

For suspected vulnerabilities, **do not post exploit details or personal information in public issues**. Use the repository's GitHub **Report a vulnerability** / private vulnerability reporting mechanism if enabled. If unavailable, contact a repository maintainer privately before publishing details. For non-sensitive gameplay bugs, open an ordinary GitHub issue with reproduction steps and the `master` commit SHA.

Please include the affected version, affected OS/JDK, steps to reproduce, and an impact description. Do **not** include passwords, tokens, personal directories, private files, account identifiers or traces containing personal information.

## Threat boundaries

- The game should not send gameplay telemetry or contact the network. Maven and GitHub Actions may access dependencies **during development builds**; this is separate from offline gameplay.
- UI preferences are limited to sound enablement, volume and reduced-motion settings under the current user's `~/.airraid/preferences.properties`. Reads are bounded to 4096 bytes, malformed values use safe defaults, and files that are symlinks or not ordinary regular files are refused.
- A failed desktop startup reports stable codes such as `AIRRAID_ERROR code=STARTUP`; it intentionally does **not** print home directories, private file contents or exception stack traces.
- Third-party media/fonts are **not legally cleared** for redistribution. See [asset licensing blocker #9](https://github.com/MeiRR17/AirRaid/issues/9).
- No Java production runtime libraries are declared in the current `pom.xml`; JUnit is test-scoped. Maven plugins and GitHub Actions still belong to the **build supply chain**.
- Build actions are pinned to specific immutable commit SHAs; the runtime and build dependency inventory is recorded in `target/dependency-inventory.json` by the CI quality gate.

## What is not yet certified

This document is a project security policy, **not** a claim of a completed penetration test, package vulnerability scan, independent source-code audit or public release authorization. A current vulnerability review, OS-specific hardening, software signing and release approval remain mandatory before v1.0.0.
