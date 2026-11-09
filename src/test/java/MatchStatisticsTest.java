import org.junit.jupiter.api.Test;
import java.util.List;
import javax.swing.SwingUtilities;
import static org.junit.jupiter.api.Assertions.*;

/** Exact, deterministic event counters for real successful projectile launches. */
class MatchStatisticsTest {
    @Test void completedUnfiredRoundHasWellDefinedZeroAccuracy() {
        GameModel model = new GameModel();
        model.startNewGame();
        model.tick(GameRules.MATCH_DURATION_MS);
        for (int player = 0; player < 2; player++) {
            assertEquals(0, model.shotsLaunched(player));
            assertEquals(0, model.targetsHit(player));
            assertEquals(0, model.hitRatePercent(player));
        }
    }

    @Test void oneHeldFireIsOneLaunchAndOneCanyonMiss() {
        GameModel model = new GameModel(List.of(), 80, 656);
        model.startNewGame();
        model.pressFire(0);
        for (int repeat = 0; repeat < 30; repeat++) model.pressFire(0);
        assertEquals(1, model.shotsLaunched(0));
        assertEquals(0, model.shotsLaunched(1));
        model.tick(3000);
        assertEquals(1, model.canyonStrikes(0));
        assertEquals(1, model.shotsLaunched(0));
        assertEquals(0, model.targetsHit(0));
        assertEquals(0, model.hitRatePercent(0));
        model.releaseFire(0);
        model.pressFire(0);
        model.tick(3000);
        assertEquals(2, model.shotsLaunched(0));
        assertEquals(2, model.canyonStrikes(0));
        assertEquals(0, model.hitRatePercent(0));
    }

    @Test void contestedTargetNeverDoubleCountsHits() {
        GameModel model = new GameModel(
                List.of(new Target(390, 550, 20, 20, 4)), 132, 596);
        model.startNewGame();
        model.pressFire(0);
        model.pressFire(1);
        model.tick(1400);
        assertEquals(1, model.shotsLaunched(0));
        assertEquals(1, model.shotsLaunched(1));
        assertEquals(1, model.targetsHit(0));
        assertEquals(0, model.targetsHit(1));
        assertEquals(100, model.hitRatePercent(0));
        assertEquals(0, model.hitRatePercent(1));
        assertEquals(4, model.score(0));
        assertEquals(0, model.score(1));
        assertTrue(model.bomb(1).isActive());
    }

    @Test void hitRateIsPerLaunchAndResetsCompletelyOnReplay() {
        GameModel model = new GameModel(
                List.of(new Target(390, 550, 20, 20, 3)), 132, 596);
        model.startNewGame();
        model.pressFire(0);
        model.tick(1400);
        assertEquals(1, model.targetsHit(0));
        model.releaseFire(0);
        model.pressFire(0);
        model.tick(3000);
        assertEquals(2, model.shotsLaunched(0));
        assertEquals(1, model.targetsHit(0));
        assertEquals(50, model.hitRatePercent(0));
        model.startNewGame();
        assertEquals(0, model.shotsLaunched(0));
        assertEquals(0, model.targetsHit(0));
        assertEquals(0, model.hitRatePercent(0));
        assertEquals(0, model.shotsLaunched(1));
        assertEquals(0, model.targetsHit(1));
    }

    @Test void pausedFinishedOrInvalidInputsNeverInflateCounters() {
        GameModel model = new GameModel(List.of(), 80, 656);
        model.startNewGame();
        model.pause();
        model.pressFire(0);
        assertEquals(0, model.shotsLaunched(0));
        model.resume();
        model.pressFire(1);
        assertEquals(1, model.shotsLaunched(1));
        model.tick(GameRules.MATCH_DURATION_MS);
        assertEquals(GameState.RESULTS, model.state());
        int shots = model.shotsLaunched(1);
        model.releaseFire(1);
        model.pressFire(1);
        model.tick(3000);
        assertEquals(shots, model.shotsLaunched(1));
        assertThrows(IllegalArgumentException.class, () -> model.shotsLaunched(-1));
        assertThrows(IllegalArgumentException.class, () -> model.targetsHit(2));
        assertThrows(IllegalArgumentException.class, () -> model.hitRatePercent(2));
    }

    @Test void resultsCardAndOfflineReportAgreeOnStats() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            GameModel model = new GameModel(List.of(), 80, 656);
            model.startNewGame();
            model.pressFire(0);
            model.tick(3000);
            model.tick(GameRules.MATCH_DURATION_MS);
            ResultsScreen screen = new ResultsScreen(() -> {}, () -> {}, text -> true);
            screen.showResult(model, UserPreferences.defaults());
            assertTrue(screen.playerStatText(0).contains("Shots 1"));
            assertTrue(screen.playerStatText(0).contains("Hits 0"));
            assertTrue(screen.playerStatText(0).contains("Hit rate 0%"));
            assertTrue(screen.playerStatText(1).contains("Shots 0"));
            assertNotNull(screen.getAccessibleContext().getAccessibleName());
            String report = screen.reportText();
            assertTrue(report.contains("Blue shots launched: 1"));
            assertTrue(report.contains("Blue targets hit: 0"));
            assertTrue(report.contains("Orange shots launched: 0"));
            assertTrue(report.contains("Orange hit rate: 0%"));
            assertTrue(report.contains("NOT verified human playtest evidence"));
        });
    }
}
