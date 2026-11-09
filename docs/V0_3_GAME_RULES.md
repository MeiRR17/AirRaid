# AirRaid v0.3.0 — Canonical gameplay contract & verification

**Scope:** Complete the original two-player game's observable rules without extending the v1.0 feature set. Implemented by `GameRules` and `GameModel`. The code and tests are authoritative; no binary release is implied.

## Match flow
1. Start creates fresh plane, bomb, target, score, strikes and input state. Time starts at **60,000 ms**.
2. Blue fires with **Enter**, orange with **Space**; one active projectile per team. A new shot requires a key release, then press, and the previous projectile must have reloaded.
3. The plane travels automatically. Bombs travel diagonally toward the target field and floor.
4. A shot that first contacts an intact target destroys it and awards that player its value (**1, 2, 3 or 4 points**). Destroyed targets never reappear within that match.
5. A shot that reaches the left, right or bottom world boundary without an intact-target hit causes **one canyon strike**. This is the formal interpretation of the ambiguous legacy instruction, “If a player hits the canyon 4 times, they lose the game.”
6. Reaching **four strikes** immediately loses, no matter how many points that player has. Both players reaching four in the **same simulation step** is a draw.
7. When the clock reaches 0 without a four-strike result, higher score wins, equal score is a draw.

## Precedence and deterministic fairness
- Each simulation tick is subdivided into steps no longer than **16 ms**. Events for both projectiles are gathered **before** resolving the round result.
- Each bomb's path is swept continuously through targets, avoiding tunneling. The earliest intact target hit wins ahead of a later playfield exit; if a bomb exits first, it records a miss.
- If two bombs contact the **same** target in the same step, earliest fractional contact receives the points. An *exact* tie is broken by rotating priority (Blue first for each new match, then Orange, then Blue). A target can score once; the second projectile stays active until it next hits or exits.
- On a step that simultaneously reaches the 60-second deadline and a fourth strike, the **four-strike rule takes priority**.
- If one player strikes out and the other scores in the same step, both shot events are processed, but the four-strike rule determines the winner.
- After a match has ended, subsequent fire and tick events are ignored. Restart resets everything, including priority.
- Pause does not advance the clock or physics. Resume continues from the same state. Closing the game or returning to the menu stops the single Swing timer.

## Target layout and game bounds
- Canvas size: 800 × 800 logical game units. Plane trajectory remains horizontal; only fire input is player-controlled.
- Targets: 8 rows × 38 columns = 304, width and height 20 units.
- Targets start at y=550, rows at intervals of 20. Rows 0–1: 1 point; 2–3: 2 points; 4–5: 3 points; 6–7: 4 points.
- Bomb size: 12 units; the target is expanded by bomb size during swept AABB collision.
- Game boundaries: left beyond x+width ≤ 0; right at x ≥ 800; bottom at y ≥ 800. A fully unscored exit is one canyon strike.

## Test coverage (JUnit and GUI smoke)
`GameRulesTest` verifies four target tiers, swept hit/non-hit geometry, a single strike for a missed shot, no firing while held, four-strike defeat, simultaneous four-strike draw, same-target simultaneous contact, deadline score winner, complete reset, paused clock, and equivalence between batched and incremental deterministic time steps. Existing `GameModelTest` covers the legacy time/tie, single active projectile, target scoring durability and lifecycle. `GuiSmoke` validates opening, pause, resume, restart and ten start/dispose cycles.

Run `mvn -B -ntp clean verify`; the PR workflow also checks JAR packaging and launches the UI under Xvfb.

## Outside v0.3.0
Native OS packaging, scaling/high-DPI layout, audio, settings, gamepad and licensing audit still require future roadmap milestones. A successful simulated match does not certify that a real two-person playtest was conducted. Do not tag a public production release before all v1.0 gates pass.
