import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Deterministic game rules with no threads, timers or Swing imports.
 * The caller owns this model; the production UI only updates it on the EDT.
 */
public final class GameModel {
    public static final int WIDTH = GameRules.WIDTH;
    public static final int HEIGHT = GameRules.HEIGHT;
    public static final int MATCH_DURATION_MS = GameRules.MATCH_DURATION_MS;

    public enum EndReason { NONE, TIME_EXPIRED, CANYON_STRIKES }

    public enum FeedbackType { SHOT, TARGET_HIT, CANYON_MISS, ROUND_FINISHED }

    /** Visual/sound cue from model state, never from a separate audio thread. */
    public record Feedback(FeedbackType type, int player, double x, double y, int points) { }

    private final List<Target> targetLayout;
    private final double blueStartX;
    private final double orangeStartX;
    private final Player[] planes = new Player[2];
    private final Bomb[] bombs = new Bomb[2];
    private final int[] score = new int[2];
    private final int[] canyonStrikes = new int[2];
    private final boolean[] fireHeld = new boolean[2];
    private final List<Target> targets = new ArrayList<>();
    private final List<Feedback> pendingFeedback = new ArrayList<>();
    private GameState state = GameState.MENU;
    private EndReason endReason = EndReason.NONE;
    private int winningPlayer = -1;
    private int remainingMs = MATCH_DURATION_MS;
    private int nextSimultaneousHitPriority = 0;

    public GameModel() {
        this(defaultTargets(), 80, WIDTH - 144);
    }

    /** Package-private fixture constructor for deterministic collision tests. */
    GameModel(List<Target> layout, double blueStartX, double orangeStartX) {
        if (layout == null) throw new IllegalArgumentException("layout is required");
        this.targetLayout = List.copyOf(layout);
        this.blueStartX = blueStartX;
        this.orangeStartX = orangeStartX;
        resetWorld();
    }

    private static List<Target> defaultTargets() {
        List<Target> built = new ArrayList<>();
        for (int row = 0; row < GameRules.TARGET_ROWS; row++) {
            for (int col = 0; col < GameRules.TARGET_COLUMNS; col++) {
                built.add(new Target(col * 20, 550 + row * 20, 20, 20, row / 2 + 1));
            }
        }
        return built;
    }

    private void resetWorld() {
        pendingFeedback.clear();
        planes[0] = new Player(blueStartX, HEIGHT / 4.0, 64, 44, +1);
        planes[1] = new Player(orangeStartX, HEIGHT / 4.0, 64, 44, -1);
        bombs[0] = new Bomb(+1);
        bombs[1] = new Bomb(-1);
        bombs[0].attach(planes[0]);
        bombs[1].attach(planes[1]);
        for (int player = 0; player < 2; player++) {
            score[player] = 0;
            canyonStrikes[player] = 0;
            fireHeld[player] = false;
        }
        remainingMs = MATCH_DURATION_MS;
        endReason = EndReason.NONE;
        winningPlayer = -1;
        nextSimultaneousHitPriority = 0;
        targets.clear();
        for (Target template : targetLayout) {
            targets.add(new Target(template.x(), template.y(), template.width(),
                                   template.height(), template.value()));
        }
    }

    public void startNewGame() {
        resetWorld();
        state = GameState.RUNNING;
    }

    public void pause() {
        if (state == GameState.RUNNING) {
            state = GameState.PAUSED;
            clearInput();
        }
    }

    public void resume() {
        if (state == GameState.PAUSED) {
            state = GameState.RUNNING;
        }
    }

    public void stop() {
        state = GameState.STOPPED;
        clearInput();
        pendingFeedback.clear();
    }

    private void clearInput() {
        fireHeld[0] = false;
        fireHeld[1] = false;
    }

    private static void validatePlayer(int index) {
        if (index < 0 || index > 1) {
            throw new IllegalArgumentException("player index must be 0 or 1");
        }
    }

    public void pressFire(int index) {
        validatePlayer(index);
        if (state != GameState.RUNNING || fireHeld[index]) return;
        fireHeld[index] = true;
        if (bombs[index].launch(planes[index])) {
            pendingFeedback.add(new Feedback(FeedbackType.SHOT, index,
                    bombs[index].x(), bombs[index].y(), 0));
        }
    }

    public void releaseFire(int index) {
        validatePlayer(index);
        fireHeld[index] = false;
    }

    public void tick(int milliseconds) {
        if (milliseconds < 0) {
            throw new IllegalArgumentException("Elapsed time must be nonnegative");
        }
        if (state != GameState.RUNNING) return;
        int left = Math.min(milliseconds, remainingMs);
        while (left > 0 && state == GameState.RUNNING) {
            int delta = Math.min(GameRules.SIMULATION_STEP_MS, left);
            simulateStep(delta / 1000.0);
            remainingMs -= delta;
            left -= delta;
            resolveEndOfStep();
        }
    }

    /** One potential impact per bomb per simulation substep. */
    private record Impact(int player, Target target, double fraction) {}

    private void simulateStep(double seconds) {
        List<Impact> impacts = new ArrayList<>(2);
        for (int player = 0; player < 2; player++) {
            Player plane = planes[player];
            plane.advance(seconds, WIDTH);
            Bomb bomb = bombs[player];
            if (!bomb.isActive()) {
                bomb.attach(plane);
                continue;
            }

            double x0 = bomb.x();
            double y0 = bomb.y();
            bomb.advance(seconds);
            double x1 = bomb.x();
            double y1 = bomb.y();
            double boundaryTime = GameRules.exitTime(x0, y0, x1, y1, bomb.size());

            Target firstTarget = null;
            double firstContact = Double.POSITIVE_INFINITY;
            for (Target target : targets) {
                if (target.isDestroyed()) continue;
                double contact = GameRules.contactTime(x0, y0, x1, y1,
                                                       bomb.size(), target);
                if (contact < firstContact && contact <= boundaryTime) {
                    firstContact = contact;
                    firstTarget = target;
                }
            }

            if (firstTarget != null) {
                impacts.add(new Impact(player, firstTarget, firstContact));
            } else if (boundaryTime <= 1.0) {
                impacts.add(new Impact(player, null, boundaryTime));
            }
        }

        // Both shots are advanced before impacts are applied, so an early result
        // cannot suppress the other player's action within the same time slice.
        final int priority = nextSimultaneousHitPriority;
        impacts.sort(Comparator.comparingDouble(Impact::fraction)
                .thenComparingInt(hit -> hit.player() == priority ? 0 : 1));
        boolean exactSharedImpact = impacts.size() == 2
                && impacts.get(0).target() != null
                && impacts.get(0).target() == impacts.get(1).target()
                && Math.abs(impacts.get(0).fraction() - impacts.get(1).fraction()) <= 1e-9;

        for (Impact impact : impacts) {
            int player = impact.player();
            if (impact.target() == null) {
                canyonStrikes[player]++;
                pendingFeedback.add(new Feedback(FeedbackType.CANYON_MISS, player,
                        bombs[player].x(), bombs[player].y(), 0));
                bombs[player].reset(planes[player]);
            } else if (!impact.target().isDestroyed()) {
                int awarded = impact.target().hit();
                score[player] += awarded;
                pendingFeedback.add(new Feedback(FeedbackType.TARGET_HIT, player,
                        impact.target().x() + impact.target().width() / 2.0,
                        impact.target().y() + impact.target().height() / 2.0,
                        awarded));
                bombs[player].reset(planes[player]);
            }
            // If the other player claimed this same target earlier in the
            // substep, the second bomb stays airborne; no duplicate points.
        }
        if (exactSharedImpact) {
            nextSimultaneousHitPriority = 1 - nextSimultaneousHitPriority;
        }
    }

    private void resolveEndOfStep() {
        boolean blueOut = canyonStrikes[0] >= GameRules.MAX_CANYON_STRIKES;
        boolean orangeOut = canyonStrikes[1] >= GameRules.MAX_CANYON_STRIKES;
        if (blueOut || orangeOut) {
            finish(EndReason.CANYON_STRIKES,
                   blueOut && orangeOut ? -1 : (blueOut ? 1 : 0));
        } else if (remainingMs == 0) {
            int winner = Integer.compare(score[0], score[1]);
            finish(EndReason.TIME_EXPIRED, winner > 0 ? 0 : (winner < 0 ? 1 : -1));
        }
    }

    private void finish(EndReason reason, int winner) {
        state = GameState.RESULTS;
        endReason = reason;
        winningPlayer = winner;
        clearInput();
        pendingFeedback.add(new Feedback(FeedbackType.ROUND_FINISHED,
                winner, WIDTH / 2.0, HEIGHT / 2.0, 0));
    }

    /**
     * Drain-only channel for UI feedback. Calling it never alters game physics.
     * The caller must have the same ownership as tick/pressFire (EDT in Swing).
     */
    public List<Feedback> drainFeedback() {
        if (pendingFeedback.isEmpty()) return List.of();
        List<Feedback> snapshot = List.copyOf(pendingFeedback);
        pendingFeedback.clear();
        return snapshot;
    }

    public GameState state() { return state; }
    public EndReason endReason() { return endReason; }
    public int winningPlayer() { return winningPlayer; }
    public int timeRemainingMs() { return remainingMs; }
    public int score(int index) { validatePlayer(index); return score[index]; }
    public int canyonStrikes(int index) { validatePlayer(index); return canyonStrikes[index]; }
    public Player player(int index) { validatePlayer(index); return planes[index]; }
    public Bomb bomb(int index) { validatePlayer(index); return bombs[index]; }
    public List<Target> targets() { return Collections.unmodifiableList(targets); }

    public String resultText() {
        if (state != GameState.RESULTS) return "";
        if (endReason == EndReason.CANYON_STRIKES) {
            if (winningPlayer == -1) return "Draw: both players reached 4 strikes!";
            return winningPlayer == 0 ? "Blue wins! Orange reached 4 strikes."
                                      : "Orange wins! Blue reached 4 strikes.";
        }
        if (winningPlayer == 0) return "Blue wins!";
        if (winningPlayer == 1) return "Orange wins!";
        return "It's a tie!";
    }
}
