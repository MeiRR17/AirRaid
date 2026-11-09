# AirRaid v0.5.0 — Rendering, event feedback and optional audio

**Goal:** improve gameplay feedback and rendering without changing the v0.3.0 canonical rules or adding copyrighted external sound samples.

## Delivered code

- `Assets.java` caches images and base fonts in concurrent maps keyed by absolute classpath resource path. No file access or font decoding during `GameCanvas.paintComponent`; fonts at 18/23/26 px are cached at initialization.
- `GameModel.java` emits `Feedback` records for SHOT, TARGET_HIT, CANYON_MISS and ROUND_FINISHED. `drainFeedback` returns an immutable snapshot once; it does not influence the deterministic game loop or scoring.
- `GameCanvas.java` draws short-lived hit rings, text labels (+1…+4), miss feedback and shot rings. It advances visual effect lifetimes using the existing Swing frame updates. No new render loop, network calls or background worker threads.
- `SoundEffects.java` synthesizes deterministic 22,050-Hz signed PCM samples for four original cues. Sound stays disabled until the player presses **Sound: Off** or **M**. On audio device error, the game continues silently. Open clips are closed on match disposal.
- `Gameplay.java` now listens to the model's events, updates effects, optionally triggers cues and shows audio status. Replay disposes transient visual feedback.
- The Maven/CI version moves from 0.4.0 to **0.5.0**.
- `FeedbackAndAudioTest` verifies event delivery, hit/miss, round-end, restart, audio PCM range and deterministic buffer shape, resource reuse, visual effect expiration and safety without a sound device.
- Xvfb smoke captures a **real** gameplay window screenshot to `target/airraid-v050-ci-gameplay.png` and uploads it as a short-lived GitHub Actions artifact for inspection.

## Performance contract

- Assets must be loaded once per classpath path rather than per round or per render frame.
- Effects are bounded to a maximum of 16 active visual indicators and expire after 24 frame callbacks.
- Audio buffers are created only when the user explicitly enables sound; sound is **off by default** and safe without a sound device.
- No regression in existing deterministic score or collision tests.
- No new non-daemon game logic threads or detached audio-processing loops.

## Asset rights: a real release blocker, not a completed audit

[ASSET_LICENSE_AUDIT.md](ASSET_LICENSE_AUDIT.md) lists the **23** legacy visual assets, their byte sizes and Git blob fingerprints. Source and redistribution licenses are presently unknown for every one of them; v0.5.0 does not attempt to invent authorization. This means **code may be merged while public distribution remains blocked** pending confirmed rights or original replacements.

The synthesized audio creates **no third-party audio assets**, but does not cure the earlier font, GIF or aircraft-art licensing risks. The font files with different names actually contain identical bytes.

## Acceptance and scope

Minimum CI gate: Java 21 `mvn -B -ntp clean verify`, JAR payload validation, Xvfb smoke and a real screenshot artifact. Manual multi-OS audio, player and performance acceptance testing remains open. Do not mark full release-production readiness or create a public production tag. A future v0.6.0 should persist user sound/motion preferences and include accessibility coverage.
