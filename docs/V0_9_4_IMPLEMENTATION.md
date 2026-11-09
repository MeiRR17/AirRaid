# AirRaid v0.9.4 — Safe keyboard auto-repeat and clearer pause state

**Status:** Internal beta engineering patch. Not a public beta or production release.

## Functional implementation

1. Added `ShortcutLatch` (pure Java): first `KEY_PRESSED` event for a held shortcut is accepted, further OS auto-repeat events are ignored, and a real `KEY_RELEASED` re-arms it. `clear()` safely recovers when an operating-system focus switch loses the release event.
2. Integrated `bindOnce` in `Gameplay` for **P** (pause/resume), **R** (restart), and **M** (sound toggle). **Enter** and **Space** keep their independent fire press/release semantics and are not absorbed by the shortcut latch.
3. Reset held-shortcut state from `Window` after focus loss and return from Rules, Settings or the QA checklist. Opening a submenu still pauses the round and returning still requires explicit P to resume.
4. Updated the arena pause overlay to a readable, static, visually framed **MATCH PAUSED** dialog with explicit instructions and frozen-clock reassurance. It uses the existing canvas painting path, no new timers or sounds, and does not intercept controls.
5. Updated the Rules screen to document that held toggles do not repeat and that releasing the key permits the next action.

## Verification

- `ShortcutLatchTest` tests first-key-only behavior across 30 repeats, independent key identities, release rearming, focus-loss clearing, 12 repeated P events without pause/resume thrashing, and 12 repeated R events without destroying a running round's time.
- `GuiSmoke` simulates press, repeat and release on the Swing EDT and checks that one held P pauses exactly once. It creates a **real screenshot** of the paused game view with game clock stopped, then resumes using a fresh press.
- Normal CI also runs the entire Java/Python suites, JAR source/asset integrity, bundled Java 21 Linux native-launcher smoke, and actual Swing screenshots of game, compact HUD, settings, QA and results.

A successful CI run indicates engineering checks pass, **not** that a real human played the game or that native Windows/macOS installers are certified.

## Existing release blockers

- **0/3 real human playtest sessions** verified, including no witnessed two-human one-keyboard playtest ([#15](https://github.com/MeiRR17/AirRaid/issues/15)).
- Windows/macOS/Linux clean install/uninstall and full offline human-gameplay certification incomplete ([#13](https://github.com/MeiRR17/AirRaid/issues/13)).
- **23 legacy bundled visual assets** lack validated redistribution grants; software LICENSE choice, owner approval, security review and signing remain outstanding ([#9](https://github.com/MeiRR17/AirRaid/issues/9)).

**No public beta/production tag or installer should be published until the real release gates are resolved.**
