import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The deterministic simulation. No Swing, no wall-clock reads and no background threads.
 * A caller supplies elapsed milliseconds; all mutations take place on its calling thread.
 * Swing's EDT is the sole owner when driven by GameController.
 */
public final class GameModel {
    public static final int WIDTH = 800;
    public static final int HEIGHT = 800;
    public static final int MATCH_DURATION_MS = 60_000;
    private static final int STEP_MS = 16;
    private static final int TARGET_ROWS = 8;
    private static final int TARGET_COLS = 38;

    private final Player[] planes = new Player[2];
    private final Bomb[] bombs = new Bomb[2];
    private final int[] score = new int[2];
    private final boolean[] held = new boolean[2];
    private final List<Target> targets = new ArrayList<>();
    private GameState state = GameState.MENU;
    private int remainingMs = MATCH_DURATION_MS;

    public GameModel() {
        resetWorld();
    }

    private void resetWorld() {
        planes[0] = new Player(80, HEIGHT / 4.0, 64, 44, +1);
        planes[1] = new Player(WIDTH - 144, HEIGHT / 4.0, 64, 44, -1);
        bombs[0] = new Bomb(+1);
        bombs[1] = new Bomb(-1);
        bombs[0].attach(planes[0]);
        bombs[1].attach(planes[1]);
        score[0] = 0;
        score[1] = 0;
        held[0] = false;
        held[1] = false;
        remainingMs = MATCH_DURATION_MS;
        targets.clear();
        for (int row = 0; row < TARGET_ROWS; row++) {
            for (int col = 0; col < TARGET_COLS; col++) {
                targets.add(new Target(col * 20, 550 + row * 20, 20, 20, row / 2 + 1));
            }
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
    }

    private void clearInput() {
        held[0] = false;
        held[1] = false;
    }

    private static void validatePlayer(int index) {
        if (index != 0 && index != 1) {
            throw new IllegalArgumentException("player index must be 0 or 1");
        }
    }

    public void pressFire(int index) {
        validatePlayer(index);
        if (state == GameState.RUNNING && !held[index]) {
            held[index] = true;
            bombs[index].launch(planes[index]);
        }
    }

    public void releaseFire(int index) {
        validatePlayer(index);
        held[index] = false;
    }

    /**
     * Advances by a supplied time interval, internally subdivided to avoid skipping
     * small targets on a slow display frame. Does nothing when not running.
     */
    public void tick(int milliseconds) {
        if (milliseconds < 0) {
            throw new IllegalArgumentException("Elapsed time must be nonnegative");
        }
        if (state != GameState.RUNNING) {
            return;
        }
        int left = Math.min(milliseconds, remainingMs);
        while (left > 0 && state == GameState.RUNNING) {
            int delta = Math.min(STEP_MS, left);
            advanceStep(delta / 1000.0);
            remainingMs -= delta;
            left -= delta;
            if (remainingMs <= 0) {
                remainingMs = 0;
                state = GameState.RESULTS;
                clearInput();
            }
        }
    }

    private void advanceStep(double seconds) {
        for (int index = 0; index < planes.length; index++) {
            planes[index].advance(seconds, WIDTH);
            Bomb bomb = bombs[index];
            if (!bomb.isActive()) {
                bomb.attach(planes[index]);
                continue;
            }
            bomb.advance(seconds);
            if (bomb.outside(WIDTH, HEIGHT)) {
                bomb.reset(planes[index]);
                continue;
            }
            for (Target target : targets) {
                if (!target.isDestroyed() && bomb.bounds().intersects(target.bounds())) {
                    score[index] += target.hit();
                    bomb.reset(planes[index]);
                    break;
                }
            }
        }
    }

    public GameState state() { return state; }
    public int timeRemainingMs() { return remainingMs; }
    public int score(int index) { validatePlayer(index); return score[index]; }
    public Player player(int index) { validatePlayer(index); return planes[index]; }
    public Bomb bomb(int index) { validatePlayer(index); return bombs[index]; }
    public List<Target> targets() { return Collections.unmodifiableList(targets); }

    public String resultText() {
        if (state != GameState.RESULTS) {
            return "";
        }
        if (score[0] > score[1]) {
            return "Blue wins!";
        }
        if (score[1] > score[0]) {
            return "Orange wins!";
        }
        return "It's a tie!";
    }
}
