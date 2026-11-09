# AirRaid — Production Roadmap (0.1.0 → 1.0.0)

**Status:** Planned. This document describes future work, not delivered features.  
**Audit date:** 2026-11-09  
**Reference baseline:** `master@238cb9ee422489b35e1a7cd3093b2319df83fce7`  
**Product:** Offline two-player desktop arcade game in Java Swing. **Not** a real-world air-raid alerting application.  
**Default integration branch:** `master` (not `main`).  
**Versioning:** Semantic Versioning `MAJOR.MINOR.PATCH`; pre-1.0 releases are incremental milestones. Tags must be created only for completed, validated versions.

## Product contract for v1.0

Two people share one keyboard. The blue airplane is controlled with Enter; orange with Space. Planes travel automatically. Each player can have at most one active bomb; bombs can destroy one target, with target values 1–4, at most once. The match lasts 60 seconds unless an agreed end-state rule ends it sooner. Scores and remaining time remain visible. At the end the game shows blue winner, orange winner or tie; a new match starts from a fully reset state. Desktop builds run without network or a Java installation because their package includes an appropriate runtime. Support Windows, macOS and Linux, including high-DPI displays.

**Rules requiring reconciliation before v0.3.0:** `Instructions/About` specifies losing after four canyon hits, but the current code implements a 60-second timer and lacks a canyon-hit counter. Decide and document the precise collision semantics, how a bomb scores, and when a match ends. Until decided, preserve the implemented 60-second rule and do not present the four-hit rule as implemented.

**Non-goals for 1.0:** online multiplayer, user accounts, servers, ads, monetization, and mobile ports. No analytics or network calls by default. Consider these only after 1.0 with explicit scope and privacy review.

## Quality and release policy

- **P0 / blocking:** game cannot start or finish reliably; deadlock/hang; data races in game state; incorrect score; runaway threads; broken assets; missing installable distribution; crash on supported OS; unknown commercial-distribution rights to required art/fonts.
- **P1 / before 1.0:** keyboard-focus reliability, pause/restart, error handling, accessible instructions, reproducible builds, minimal regression tests, smoke tests on each OS.
- **P2 / post-1.0:** leaderboards, more arenas, AI opponent, online multiplayer.
- **Merge rule:** feature branches → reviewed PR → fast relevant checks → `master`. Avoid directly editing `master` for planned development. No release claim without build/run evidence.
- **Test strategy:** concentrate on pure deterministic engine tests; run a small smoke suite per PR. Full distribution smoke tests only at release milestones. Do not run redundant long CI matrices on every documentation change.
- **Packaging standard:** select one build system (proposed Maven), pin plugin and dependency versions; Java 21 LTS target. Use `jpackage` or equivalent to ship platform-specific runtime bundles. Add provenance and checksums for releases.
- **Dependencies:** each version below assumes the previous version's exit gate has passed. If a gate fails, repair it in a PATCH release rather than marking an incomplete milestone delivered.

## Phase 0 — establish a reliable baseline

### v0.1.0 — Reproducible build and project hygiene
**Priority:** P0. **Estimated effort:** 6–10 focused hours.
- Introduce a Maven `pom.xml` targeting Java 21 and conventional `src/main/java`, `src/main/resources`, `src/test/java`. Move existing sources/assets without changing mechanics in the same step.
- Centralize dependency and plugin versions; use JUnit Jupiter for tests and a project-level launcher; establish `mvn verify` as the documented build command.
- Document exact JDK, startup commands, supported OS, initial game rules, and known defects; keep source-control metadata out of the repo.
- Build a runnable development JAR; confirm all current assets' filenames and locations. At this stage, fail loudly with diagnostics if an asset is absent instead of silently showing a blank icon.
- Introduce one lightweight compile/test workflow *only if GitHub Actions budget is available*; otherwise document equivalent local commands and defer automatic CI.
**Acceptance:** clean checkout compiles on JDK 21; `mvn verify` passes; a local launch smoke test shows the game window; no IDE-specific setup needed. No release packaging claims yet.

### v0.2.0 — Deterministic engine and thread safety
**Priority:** P0. **Estimated effort:** 10–16 hours.
- Replace per-`Player`, per-`Bomb`, and per-`Target` `Thread` subclasses with plain game entities updated by a single controlled game loop. Prefer one Swing EDT `Timer` (or a well-defined model/snapshot boundary) and fixed timestep/delta clamping.
- Create `GameState`, `GameRules`, `GameController`, `GamePanel`, and immutable/safe input-state interfaces; isolate simulation from drawing.
- Implement explicit states `MENU`, `RUNNING`, `PAUSED`, `RESULTS`, `STOPPED`; provide lifecycle-safe start, stop, dispose and replay.
- Eliminate static mutable scores and worker-driven UI updates; ensure model changes and Swing widgets are synchronized through the EDT contract.
- Load image/font assets once via classpath resource loader; remove font I/O from `paintComponent`; never launch gameplay from a UI constructor before Start.
**Acceptance:** no unbounded background loops; Start creates one simulation; leaving a match stops updates; 10 consecutive starts/restarts do not accumulate timers or entities; deterministic pure-engine unit tests pass.

## Phase 1 — complete the core game

### v0.3.0 — Complete gameplay rules
**Priority:** P0. **Estimated effort:** 8–12 hours.
- Encode the canonical rule specification (60-second clock; blue Enter; orange Space; target weights 1–4; one active bomb per player; no re-scoring destroyed targets).
- Repair bomb ownership, launch, cooldown/reload, boundary return, and movement; correctly distinguish pressed, held and released keys.
- Use entity bounds and collision resolution exactly once per tick; prevent two bombs from scoring the same target.
- Resolve four-canyon-hit rule versus timer, and make win/loss/tie and early termination unambiguous; explicitly document priority when both conditions occur in the same tick.
- Add unit tests for collisions, target durability, bonus/score mapping, countdown boundary, tie, restart, and simultaneous hits.
**Acceptance:** deterministic scripted match simulations produce exact expected scores and outcome; no negative countdown or duplicate points; both players' controls work independently.

### v0.4.0 — Functional game flow and resilient UI
**Priority:** P1. **Estimated effort:** 7–11 hours.
- Replace placeholder `path/to/your/gif/file.gif` in `GameOpening` and repair title/start-button layout; make Start, Pause, Resume, Restart, Main Menu and Exit discoverable.
- Integrate the rules panel into the application instead of a separate unmanaged popup; fix keyboard focus after opening/closing dialogs and resizing.
- Add readable HUD: timer, team colors, points, remaining penalties (if applicable), pause state; support winner/tie results and replay.
- Keep game simulation separate from pixel layout; account for viewport resizing and high DPI using a consistent logical canvas.
- Test mouse+keyboard navigation, rapid Start/Restart and closing a window mid-match.
**Acceptance:** a new user can launch, read controls, play, pause, finish, replay and exit without restarting the process; UI is not clipped at 100–200% desktop scaling.

### v0.5.0 — Presentation, performance and assets
**Priority:** P1. **Estimated effort:** 5–9 hours.
- Audit sprite sizes, correct image aspect ratio and scaling, fix case-sensitive resource paths (notably `background.png` vs `Background.png`).
- Cache images/fonts; use `Graphics2D` with proper render hints and dispose copied graphics contexts safely; avoid per-frame allocation where possible.
- Add distinct shot/hit/round-end feedback and user-controllable sound; ship with sound muted or disabled if rights cannot be proven.
- Ensure no unnecessary asset loading during the simulation; check frame pacing and memory over repeated rounds.
- Document asset inventory, owners, fonts, provenance and redistribution licenses.
**Acceptance:** no broken images on case-sensitive OS; no recurring asset I/O in render path; extended repeated-game smoke run has stable frame pacing and no obvious resource leak.

## Phase 2 — product hardening

### v0.6.0 — Accessibility, settings and usability
**Priority:** P1. **Estimated effort:** 5–8 hours.
- Add audio mute/volume, key help, and persistent non-sensitive local preferences with safe defaults and schema/versioning.
- Support keyboard-only operation and visible focus; do not rely on team color alone to convey scores or state.
- Ensure legible contrast, minimum readable text sizes, sensible focus/escape behavior and basic screen scaling.
- Handle corrupt settings by resetting safely and reporting a friendly message.
**Acceptance:** app is fully playable without a mouse after initial launch; preferences survive restart; clean profile and malformed settings behave safely.

### v0.7.0 — Test coverage, security and license gate
**Priority:** P0/P1. **Estimated effort:** 7–12 hours.
- Add unit tests for all core rules plus headless-safe model tests. Add UI smoke-test instructions and targeted tests for launcher, pause/restart, missing assets and shutdown.
- Audit dependency versions and build plugins, include security/upkeep policy; no secrets or personally identifiable data in log output.
- Audit licenses and redistribution rights for `Resource/Font`, `Resource/General`, aircraft, target sprites, GIF and icon. Replace any unlicensed assets before public release. Two font filenames currently reference the same blob; investigate rather than assuming two distinct licensed fonts.
- Introduce structured error logging with no sensitive content and clear local diagnostic instructions.
- Add `LICENSE`, `THIRD_PARTY_NOTICES.md`, `CONTRIBUTING.md`, `SECURITY.md` with verified/appropriate content—not boilerplate claims.
**Acceptance:** all game-rule tests green; zero unresolved release-blocking license findings; no known critical dependency vulnerability; graceful failures are covered.

### v0.8.0 — Desktop distributions
**Priority:** P0. **Estimated effort:** 8–14 hours.
- Use `jlink`/`jpackage` where suitable to produce native installers or app bundles for Windows, macOS and Linux; package all required resources and a compatible Java runtime.
- Pin reproducible build inputs; create signed artifacts where certificates and distribution permissions are available (clearly mark unsigned builds).
- Add release automation or documented operator scripts for version tags, artifact naming, SHA-256 checksums, SBOM/third-party notices, changelog and rollback.
- Validate clean install, launch, uninstall, and no-network offline play on each target OS.
**Acceptance:** unprivileged clean machine can install and launch the game without separately installing Java; package contents match source commit; checksums published.

### v0.9.0 — Release candidate / beta
**Priority:** P0/P1. **Estimated effort:** 6–10 hours.
- Feature freeze; run cross-platform playtest matrix, keyboard contention scenarios, headless game-rule regression, crash/restart loops and fresh-install checks.
- Gather feedback from at least three complete playtest sessions including two people on one keyboard; fix P0/P1 defects, document known P2 issues.
- Prepare gameplay screenshots, release notes, version information, support instructions and a short troubleshooting guide.
- Publish beta to a controlled release audience; never label beta as production-ready.
**Acceptance:** zero open P0/P1 defects; every supported OS passes installation/play/replay/exit smoke test; beta release artifacts independently downloadable and verified.

## Phase 3 — public release

### v1.0.0 — Production release
**Priority:** release gate. **Estimated effort:** 4–8 hours.
- Re-run release checklist against immutable RC commit; publish signed builds if signing is available and clearly state platform trust warnings if not.
- Tag `v1.0.0` only on the approved `master` commit; publish binaries with changelog, checksum, license/attribution and reproducibility metadata.
- Ensure README explains controls, download/install, supported OS, troubleshooting and how to report bugs.
- Monitor real user bug reports, triage regressions and maintain `1.0.x` patch cadence. Keep prior release artifacts for rollback.
**Acceptance:** every gate in `docs/RELEASE_CHECKLIST.md` is checked with concrete evidence; no unresolved P0/P1; README and release artifacts point to the same version and commit.

## Out-of-scope feature backlog (after v1.0)

- **v1.1.0:** new arenas, game modes, optional match length, local high scores.
- **v1.2.0:** animations, controller support, localization (Hebrew/English with RTL testing).
- **v2.0.0 (candidate only):** potential networking, online play or major engine rewrite *only if validated demand exists*. Never promise these as part of v1.0.

## Critical path / tracking

`0.1 build → 0.2 safe engine → 0.3 correct rules → 0.4 game flow → 0.5 assets → 0.6 usability → 0.7 testing & license → 0.8 packaging → 0.9 beta → 1.0 release`.

**Estimated cumulative effort:** 66–110 focused engineering hours from the per-version ranges above, plus approximately 10 hours of contingency (planning envelope: 66–120 hours). These are not elapsed-time or delivery-date guarantees; complexity and asset rights may expand them. Work is not complete simply because documentation exists. The tasks/acceptance criteria above are the authoritative scope. Keep a parent tracking GitHub issue with per-version checkboxes. For each version, attach commit/PR/release evidence, test commands and blockers before checking it off.

## First implementation PR after this planning PR

`feat/v0.1.0-build-foundation`:
1. Introduce `pom.xml` and `src/main/java` / `src/main/resources`.
2. Preserve game behavior while moving files and making asset paths consistent.
3. Add baseline compile and launcher smoke instructions.
4. Run `mvn verify` and launch from a clean checkout.
5. Open PR into `master`; tag `v0.1.0` only after acceptance evidence.

**Audit limitations:** This roadmap is based on a static source-tree inspection, not a compiled or executed application. Build, rendering, OS packaging and gameplay functionality have not yet been validated. All findings describe observed source or absent repository files, not proven runtime failures.
