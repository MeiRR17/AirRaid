import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.util.Objects;
import java.util.function.LongSupplier;

/** One EDT-owned timer driven by elapsed monotonic time, not assumed frame rate. */
public final class GameController {
    private final GameModel model;
    private final Timer timer;
    private final Runnable afterTick;
    private final Runnable onResults;
    private final MatchClock clock;

    public GameController(GameModel model, Runnable afterTick, Runnable onResults) {
        this(model, afterTick, onResults, System::nanoTime);
    }

    /** Deterministic test injection for slow-frame and pause/restart regressions. */
    GameController(GameModel model, Runnable afterTick, Runnable onResults,
                   LongSupplier nanoTime) {
        this.model = Objects.requireNonNull(model);
        this.afterTick = Objects.requireNonNull(afterTick);
        this.onResults = Objects.requireNonNull(onResults);
        clock = new MatchClock(nanoTime);
        timer = new Timer(16, event -> poll());
        timer.setCoalesce(true);
    }

    private static void assertEdt() {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("GameController must run on the Swing EDT");
        }
    }

    /** Also used in headless EDT tests to simulate coalesced display frames. */
    void poll() {
        assertEdt();
        // A deterministic test or a synchronous model action may finish the
        // match before the next scheduled Swing Timer event. Deliver results
        // once even if no elapsed milliseconds remain for this callback.
        if (model.state() == GameState.RESULTS) {
            deliverResultsOnce();
            return;
        }
        if (model.state() != GameState.RUNNING) return;
        int elapsed = clock.elapsedMilliseconds();
        if (elapsed <= 0) return;
        model.tick(elapsed);
        afterTick.run();
        if (model.state() == GameState.RESULTS) deliverResultsOnce();
    }

    private void deliverResultsOnce() {
        if (!timer.isRunning()) return;
        timer.stop();
        onResults.run();
    }

    public void startNewGame() {
        assertEdt();
        timer.stop();
        model.startNewGame();
        clock.reset();
        timer.start();
        afterTick.run();
    }

    public void pause() {
        assertEdt();
        if (model.state() == GameState.RUNNING) {
            timer.stop();
            model.pause();
            clock.reset();
            afterTick.run();
        }
    }

    public void resume() {
        assertEdt();
        if (model.state() == GameState.PAUSED) {
            model.resume();
            clock.reset();
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
