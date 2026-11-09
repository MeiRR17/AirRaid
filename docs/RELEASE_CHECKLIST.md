# AirRaid — v1.0.0 Release Checklist

This is a **gate**, not a report of completed work. An unchecked item is unfinished until supported by evidence. Record test output, PRs and release artifact links alongside completed items.

## Source & gameplay
- [ ] Released commit and tag match reviewed `master` HEAD.
- [ ] `mvn verify` succeeds on the pinned Java 21 toolchain from a clean checkout.
- [ ] Correct and deterministic engine behavior; no immortal worker threads or unsynchronized Swing UI changes.
- [ ] Start, Pause, Resume, Restart, Results and Exit work without accumulating threads or timers.
- [ ] Two simultaneous local players: blue Enter, orange Space. Neither key disables the other.
- [ ] Only one active bomb per player and only one scoring event per target.
- [ ] The canonical match clock, canyon-hit rule, tie outcome and all target values are specified and tested.
- [ ] 10 successive full matches and restarts run without crash or visible resource growth.

## UX, graphics & assets
- [ ] Correct rendering, keyboard focus and rules navigation at 100%, 150% and 200% display scaling.
- [ ] No font/image reload in paint loop; no missing resources on a case-sensitive file system.
- [ ] No placeholder links/paths or development-only UI remain.
- [ ] Keyboard-only gameplay; text and controls not distinguished by color alone.
- [ ] Every bundled asset/font has documented permission or a compatible distribution license. Required replacements completed.

## Testing, security & quality
- [ ] P0/P1 defect queue empty; any P2 issues documented as known limitations.
- [ ] Unit tests cover rule outcomes, boundaries, collisions, resets, pause/resume and simultaneous input.
- [ ] No known critical/high supply-chain issues affecting packaged software; component version inventory recorded.
- [ ] No secrets, tokens, personal telemetry, or undisclosed network features in the distribution.
- [ ] Local settings tolerate missing/corrupt files; errors are communicated without unhandled exceptions.
- [ ] Game logs do not collect personal data and do not silently disclose user files.

## Distribution & legal
- [ ] Clean Windows install/run/uninstall verified.
- [ ] Clean macOS install/run/uninstall verified.
- [ ] Clean Linux install/run/uninstall verified.
- [ ] Bundled compatible Java runtime included; no separate JVM required.
- [ ] Offline play verified without network connection.
- [ ] Checksums, sizes, supported OS/architecture, version/build SHA, and provenance published for each artifact.
- [ ] Code license, third-party notices and content permissions published; platform signing/notarization status explicit.
- [ ] Versioned changelog, screenshots, install guide, support instructions and issue-reporting template published.
- [ ] Prior stable artifact preserved and rollback instructions confirmed.

## Release decision
- [ ] At least three complete external/local playtest sessions recorded; one with two simultaneous human players.
- [ ] All acceptance evidence linked in the release PR or release issue.
- [ ] Release owner explicitly approves publishing `v1.0.0`.
- [ ] Final `v1.0.0` tag and GitHub Release point to exactly the validated commit.

**Release gate:** Any unchecked P0 item blocks release. No public "production" or "stable" label before all mandatory checks pass.
