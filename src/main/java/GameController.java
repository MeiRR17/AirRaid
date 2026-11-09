import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.util.Objects;

/** EDT-only lifecycle adapter between the deterministic model and Swing. */
public final class GameController {
    private final GameModel model;
    private final Timer timer;
    private final Runnable afterTick;
    private final Runnable onResults;

    public GameController(GameModel model, Runnable afterTick, Runnable onResults) {
        this.model = Objects.requireNonNull(model);
        this.afterTick = Objects.requireNonNull(afterTick);
        this.onResults = Objects.requireNonNull(onResults);
        timer = new Timer(16, event -> {
            model.tick(16);
            afterTick.run();
            if (model.state() == GameState.RESULTS) {
                ((Timer) event.getSource()).stop();
                onResults.run();
            }
        });
        timer.setCoalesce(true);
    }

    private static void assertEdt() {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("GameController must run on the Swing EDT");
        }
    }

    public void startNewGame() {
        assertEdt();
        timer.stop();
        model.startNewGame();
        timer.start();
        afterTick.run();
    }

    public void pause() {
        assertEdt();
        if (model.state() == GameState.RUNNING) {
            timer.stop();
            model.pause();
            afterTick.run();
        }
    }

    public void resume() {
        assertEdt();
        if (model.state() == GameState.PAUSED) {
            model.resume();
            timer.start();
            afterTick.run();
        }
    }

    public void dispose() {
        assertEdt();
        timer.stop();
        model.stop();
        afterTick.run();
    }

    public boolean timerRunning() {
        assertEdt();
        return timer.isRunning();
    }
}
