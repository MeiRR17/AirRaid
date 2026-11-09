import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class GameModelTest {
    @Test void freshGameAndPauseResumeAreDeterministic() {
        GameModel model = new GameModel();
        assertEquals(GameState.MENU, model.state());
        assertEquals(304, model.targets().size());
        model.startNewGame();
        double x = model.player(0).x();
        model.tick(160);
        assertTrue(model.player(0).x() > x);
        assertEquals(59_840, model.timeRemainingMs());
        model.pause();
        double pausedX = model.player(0).x();
        model.tick(160);
        assertEquals(pausedX, model.player(0).x());
        assertEquals(59_840, model.timeRemainingMs());
        model.resume();
        model.tick(160);
        assertEquals(59_680, model.timeRemainingMs());
    }

    @Test void oneProjectileAtATimeAndFireEdgeRequired() {
        GameModel model = new GameModel();
        model.startNewGame();
        model.pressFire(0);
        model.pressFire(1);
        assertTrue(model.bomb(0).isActive());
        assertTrue(model.bomb(1).isActive());
        double x = model.bomb(0).x();
        model.pressFire(0);
        assertEquals(x, model.bomb(0).x());
        model.releaseFire(0);
        model.tick(16);
        assertTrue(model.bomb(0).x() > x);
    }

    @Test void targetCanOnlyScoreOnce() {
        Target target = new Target(12, 20, 20, 20, 4);
        assertEquals(4, target.hit());
        assertEquals(0, target.hit());
        assertTrue(target.isDestroyed());
    }

    @Test void sixtySecondMatchEndsInTieWhenNoOneScores() {
        GameModel model = new GameModel();
        model.startNewGame();
        model.tick(GameModel.MATCH_DURATION_MS);
        assertEquals(0, model.timeRemainingMs());
        assertEquals(GameState.RESULTS, model.state());
        assertEquals("It's a tie!", model.resultText());
        model.tick(1000);
        assertEquals(0, model.timeRemainingMs());
    }

    @Test void replayResetsStateWithoutLeakingInputsOrDestroyedTargets() {
        GameModel model = new GameModel();
        model.startNewGame();
        model.pressFire(0);
        model.targets().get(0).hit();
        model.tick(112);
        model.startNewGame();
        assertEquals(GameState.RUNNING, model.state());
        assertEquals(GameModel.MATCH_DURATION_MS, model.timeRemainingMs());
        assertEquals(0, model.score(0));
        assertFalse(model.bomb(0).isActive());
        assertEquals(304, model.targets().stream().filter(t -> !t.isDestroyed()).count());
        model.stop();
        model.tick(1000);
        assertEquals(GameState.STOPPED, model.state());
        assertEquals(GameModel.MATCH_DURATION_MS, model.timeRemainingMs());
    }

    @Test void differentStepBatchesGiveEquivalentWorld() {
        GameModel left = new GameModel();
        GameModel right = new GameModel();
        left.startNewGame();
        right.startNewGame();
        left.pressFire(0);
        right.pressFire(0);
        left.tick(160);
        for (int i = 0; i < 10; i++) right.tick(16);
        assertEquals(left.player(0).x(), right.player(0).x(), 0.000001);
        assertEquals(left.bomb(0).y(), right.bomb(0).y(), 0.000001);
        assertEquals(left.timeRemainingMs(), right.timeRemainingMs());
    }

    @Test void invalidStepRejected() {
        GameModel model = new GameModel();
        assertThrows(IllegalArgumentException.class, () -> model.tick(-1));
        assertThrows(IllegalArgumentException.class, () -> model.pressFire(2));
    }
}
