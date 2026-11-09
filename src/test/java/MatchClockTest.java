import org.junit.jupiter.api.Test;
import javax.swing.SwingUtilities;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import static org.junit.jupiter.api.Assertions.*;

class MatchClockTest {
    @Test void fractionalMillisArePreservedAcrossMultiplePulses() {
        AtomicLong now = new AtomicLong(0);
        MatchClock clock = new MatchClock(now::get);
        now.addAndGet(500_000);
        assertEquals(0, clock.elapsedMilliseconds());
        now.addAndGet(500_000);
        assertEquals(1, clock.elapsedMilliseconds());
        now.addAndGet(16_400_000);
        assertEquals(16, clock.elapsedMilliseconds());
        now.addAndGet(600_000);
        assertEquals(1, clock.elapsedMilliseconds());
    }

    @Test void resetDropsPausedElapsedTimeAndBackwardsClockIsSafe() {
        AtomicLong now = new AtomicLong(2_000_000_000L);
        MatchClock clock = new MatchClock(now::get);
        now.addAndGet(500_000_000L);
        assertEquals(500, clock.elapsedMilliseconds());
        now.addAndGet(35_000_000_000L);
        clock.reset(); // pause / restart
        assertEquals(0, clock.elapsedMilliseconds());
        now.addAndGet(-1_000_000L); // defensive against an injected clock regression
        assertEquals(0, clock.elapsedMilliseconds());
    }

    @Test void controllerUsesRealElapsedTimeAndDoesNotConsumePauseTime() throws Exception {
        AtomicLong now = new AtomicLong(10_000_000_000L);
        AtomicInteger resultCalls = new AtomicInteger();
        AtomicInteger repaintCalls = new AtomicInteger();
        SwingUtilities.invokeAndWait(() -> {
            GameModel model = new GameModel();
            GameController controller = new GameController(
                    model, repaintCalls::incrementAndGet,
                    resultCalls::incrementAndGet, now::get);
            controller.startNewGame();
            now.addAndGet(300_000_000L); // simulate a coalesced 300ms UI frame
            controller.poll();
            assertEquals(59_700, model.timeRemainingMs());
            controller.pause();
            now.addAndGet(5_000_000_000L);
            controller.poll();
            assertEquals(59_700, model.timeRemainingMs());
            controller.resume();
            controller.poll();
            assertEquals(59_700, model.timeRemainingMs());
            now.addAndGet(100_000_000L);
            controller.poll();
            assertEquals(59_600, model.timeRemainingMs());
            now.addAndGet(60_000_000_000L);
            controller.poll();
            assertEquals(GameState.RESULTS, model.state());
            assertFalse(controller.timerRunning());
            assertEquals(1, resultCalls.get());
            assertEquals(0, model.timeRemainingMs());
            now.addAndGet(1_000_000_000L);
            controller.poll();
            assertEquals(1, resultCalls.get());
            controller.startNewGame();
            assertEquals(GameState.RUNNING, model.state());
            assertEquals(GameRules.MATCH_DURATION_MS, model.timeRemainingMs());
            controller.dispose();
            assertEquals(GameState.STOPPED, model.state());
            assertFalse(controller.timerRunning());
            assertTrue(repaintCalls.get() > 0);
        });
    }

    @Test void controllerMustBeUsedOnlyOnSwingEdt() {
        GameController controller = new GameController(
                new GameModel(), () -> {}, () -> {}, System::nanoTime);
        assertThrows(IllegalStateException.class, controller::startNewGame);
        assertThrows(IllegalStateException.class, controller::poll);
    }
}
