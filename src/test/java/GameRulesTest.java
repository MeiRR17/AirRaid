import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/** Canonical gameplay rules: timing, swept collision, misses and final outcomes. */
class GameRulesTest {
    private static GameModel emptyArena() {
        return new GameModel(List.of(), 80, GameRules.WIDTH - 144);
    }

    @Test void targetsHaveExactlyFourPointTiers() {
        GameModel model = new GameModel();
        assertEquals(304, model.targets().size());
        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 38; column++) {
                Target target = model.targets().get(row * 38 + column);
                assertEquals(row / 2 + 1, target.value());
                assertEquals(column * 20, target.x());
                assertEquals(550 + row * 20, target.y());
            }
        }
    }

    @Test void sweptCollisionCatchesTargetsPassedBetweenFrames() {
        Target target = new Target(100, 100, 20, 20, 4);
        assertEquals(0.20,
                GameRules.contactTime(80, 110, 130, 110, 10, target), 1e-9);
        assertEquals(Double.POSITIVE_INFINITY,
                GameRules.contactTime(80, 60, 130, 60, 10, target));
        assertEquals(1.0 / 3.0,
                GameRules.contactTime(100, 70, 100, 130, 10, target), 1e-9);
    }

    @Test void completeUnscoredFlightCountsExactlyOneCanyonStrike() {
        GameModel model = emptyArena();
        model.startNewGame();
        model.pressFire(0);
        model.tick(3_000);
        assertEquals(1, model.canyonStrikes(0));
        assertEquals(0, model.score(0));
        assertFalse(model.bomb(0).isActive());
        assertEquals(GameState.RUNNING, model.state());

        // A held key must not auto-launch a second projectile after reload.
        model.tick(3_000);
        assertEquals(1, model.canyonStrikes(0));
        model.releaseFire(0);
        model.pressFire(0);
        model.tick(3_000);
        assertEquals(2, model.canyonStrikes(0));
    }

    @Test void fourCanyonStrikesCauseImmediateLossRegardlessOfPoints() {
        GameModel model = emptyArena();
        model.startNewGame();
        for (int i = 0; i < 4; i++) {
            model.pressFire(0);
            model.tick(3_000);
            model.releaseFire(0);
        }
        assertEquals(GameRules.MAX_CANYON_STRIKES, model.canyonStrikes(0));
        assertEquals(GameState.RESULTS, model.state());
        assertEquals(GameModel.EndReason.CANYON_STRIKES, model.endReason());
        assertEquals(1, model.winningPlayer());
        assertTrue(model.timeRemainingMs() > 0);
        assertTrue(model.resultText().contains("Orange wins"));
        int remaining = model.timeRemainingMs();
        model.pressFire(1);
        model.tick(1000);
        assertEquals(remaining, model.timeRemainingMs());
        assertFalse(model.bomb(1).isActive());
    }

    @Test void bothPlayersFourStrikesInSameSimulationStepDraw() {
        GameModel model = emptyArena();
        model.startNewGame();
        for (int i = 0; i < 4; i++) {
            model.pressFire(0);
            model.pressFire(1);
            model.tick(3_000);
            model.releaseFire(0);
            model.releaseFire(1);
        }
        assertEquals(4, model.canyonStrikes(0));
        assertEquals(4, model.canyonStrikes(1));
        assertEquals(-1, model.winningPlayer());
        assertEquals(GameModel.EndReason.CANYON_STRIKES, model.endReason());
        assertTrue(model.resultText().startsWith("Draw"));
    }

    @Test void simultaneousHitOnOneTargetCanOnlyCreditOnePlayer() {
        GameModel model = new GameModel(List.of(new Target(390, 550, 20, 20, 4)),
                                        132, 596);
        model.startNewGame();
        model.pressFire(0);
        model.pressFire(1);
        model.tick(1_400);
        assertEquals(4, model.score(0));
        assertEquals(0, model.score(1));
        assertTrue(model.targets().get(0).isDestroyed());
        assertFalse(model.bomb(0).isActive());
        assertTrue(model.bomb(1).isActive(),
                "Loser of a simultaneous contact keeps its shot active");
    }

    @Test void scorerWinsAtDeadlineAndStrikesAreResetOnReplay() {
        GameModel model = new GameModel(List.of(new Target(390, 550, 20, 20, 3)),
                                        132, 596);
        model.startNewGame();
        model.pressFire(0);
        model.tick(1_400);
        assertEquals(3, model.score(0));
        assertEquals(0, model.canyonStrikes(0));
        model.tick(70_000);
        assertEquals(GameState.RESULTS, model.state());
        assertEquals(GameModel.EndReason.TIME_EXPIRED, model.endReason());
        assertEquals(0, model.winningPlayer());
        assertEquals("Blue wins!", model.resultText());
        assertEquals(0, model.timeRemainingMs());

        model.startNewGame();
        assertEquals(GameState.RUNNING, model.state());
        assertEquals(0, model.score(0));
        assertEquals(0, model.canyonStrikes(0));
        assertEquals(MATCH_DURATION(), model.timeRemainingMs());
        assertFalse(model.targets().get(0).isDestroyed());
        assertFalse(model.bomb(0).isActive());
        assertEquals(GameModel.EndReason.NONE, model.endReason());
    }

    private static int MATCH_DURATION() {
        return GameRules.MATCH_DURATION_MS;
    }

    @Test void pauseAndStopDoNotConsumeTimeOrInput() {
        GameModel model = emptyArena();
        model.startNewGame();
        model.pressFire(0);
        model.pause();
        int remaining = model.timeRemainingMs();
        model.tick(5_000);
        assertEquals(remaining, model.timeRemainingMs());
        model.resume();
        model.tick(32);
        model.stop();
        remaining = model.timeRemainingMs();
        model.tick(1000);
        assertEquals(remaining, model.timeRemainingMs());
        assertEquals(GameState.STOPPED, model.state());
    }

    @Test void largeAndSmallStepBatchesAgreeOnShotResult() {
        GameModel large = new GameModel();
        GameModel small = new GameModel();
        large.startNewGame();
        small.startNewGame();
        large.pressFire(0);
        small.pressFire(0);
        large.tick(2_500);
        for (int i = 0; i < 156; i++) small.tick(16);
        small.tick(4);
        assertEquals(large.score(0), small.score(0));
        assertEquals(large.canyonStrikes(0), small.canyonStrikes(0));
        assertEquals(large.timeRemainingMs(), small.timeRemainingMs());
    }
}
