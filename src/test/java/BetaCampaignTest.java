import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/** Beta stability campaign: completed rounds, rapid restarts and both-player input. */
class BetaCampaignTest {
    @Test void tenEntireRoundsCanReplayWithoutStateLeakage() {
        GameModel model = new GameModel();
        for (int match = 0; match < 10; match++) {
            model.startNewGame();
            assertEquals(GameState.RUNNING, model.state());
            assertEquals(304, model.targets().size());
            assertEquals(0, model.canyonStrikes(0));
            assertEquals(0, model.canyonStrikes(1));
            assertEquals(0, model.score(0));
            assertEquals(0, model.score(1));
            assertTrue(model.drainFeedback().isEmpty());
            model.tick(GameRules.MATCH_DURATION_MS);
            assertEquals(GameState.RESULTS, model.state());
            assertEquals(GameModel.EndReason.TIME_EXPIRED, model.endReason());
            assertEquals(0, model.timeRemainingMs());
            assertEquals(-1, model.winningPlayer());
            assertEquals("It's a tie!", model.resultText());
            assertEquals(1, model.drainFeedback().stream()
                    .filter(f -> f.type() == GameModel.FeedbackType.ROUND_FINISHED).count());
            model.tick(1500);
            assertTrue(model.drainFeedback().isEmpty());
        }
        model.stop();
        assertEquals(GameState.STOPPED, model.state());
    }

    @Test void contestedTwoPlayerFireAndFastResetsDoNotLeakKeys() {
        GameModel model = new GameModel(List.of(), 80, 656);
        for (int match = 0; match < 12; match++) {
            model.startNewGame();
            model.pressFire(0);
            model.pressFire(1);
            assertTrue(model.bomb(0).isActive());
            assertTrue(model.bomb(1).isActive());
            model.pause();
            int pausedTime = model.timeRemainingMs();
            model.tick(2000);
            assertEquals(pausedTime, model.timeRemainingMs());
            model.resume();
            model.tick(3000);
            assertEquals(1, model.canyonStrikes(0));
            assertEquals(1, model.canyonStrikes(1));
            model.startNewGame();
            assertFalse(model.bomb(0).isActive());
            assertFalse(model.bomb(1).isActive());
            model.pressFire(1); // previous key state must not survive restart
            assertTrue(model.bomb(1).isActive());
            assertFalse(model.bomb(0).isActive());
        }
    }

    @Test void immediateFourthStrikeCannotBeOverriddenByTime() {
        GameModel model = new GameModel(List.of(), 80, 656);
        model.startNewGame();
        for (int i = 0; i < 4; i++) {
            model.pressFire(0);
            model.tick(3_000);
            model.releaseFire(0);
        }
        assertEquals(GameState.RESULTS, model.state());
        assertEquals(GameModel.EndReason.CANYON_STRIKES, model.endReason());
        assertEquals(1, model.winningPlayer());
        assertTrue(model.timeRemainingMs() > 0);
        model.tick(GameRules.MATCH_DURATION_MS);
        assertEquals(GameModel.EndReason.CANYON_STRIKES, model.endReason());
    }
}
