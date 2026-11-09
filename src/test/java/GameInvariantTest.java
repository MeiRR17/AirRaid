import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

/** Bounded deterministic model campaign: invariants, replay, timer and scoring. */
class GameInvariantTest {
    @Test void seededTwoPlayerCampaignPreservesScoreTimeAndProjectileInvariants() {
        for (int seed = 0; seed < 24; seed++) {
            Random commands = new Random(seed * 17L + 9);
            GameModel game = new GameModel();
            game.startNewGame();
            int previousTime = GameRules.MATCH_DURATION_MS;
            int previousDestroyed = 0;
            for (int frame = 0; frame < 175; frame++) {
                if (game.state() != GameState.RUNNING) break;
                for (int player = 0; player < 2; player++) {
                    int roll = commands.nextInt(8);
                    if (roll == 0) game.pressFire(player);
                    else if (roll == 1) game.releaseFire(player);
                }
                game.tick(GameRules.SIMULATION_STEP_MS);
                assertTrue(game.timeRemainingMs() <= previousTime);
                assertTrue(game.timeRemainingMs() >= 0);
                previousTime = game.timeRemainingMs();
                assertTrue(game.canyonStrikes(0) <= GameRules.MAX_CANYON_STRIKES);
                assertTrue(game.canyonStrikes(1) <= GameRules.MAX_CANYON_STRIKES);
                List<Target> targets = game.targets();
                int destroyed = (int) targets.stream().filter(Target::isDestroyed).count();
                assertTrue(destroyed >= previousDestroyed);
                previousDestroyed = destroyed;
                int points = targets.stream().filter(Target::isDestroyed)
                        .mapToInt(Target::value).sum();
                assertEquals(points, game.score(0) + game.score(1),
                        "every destroyed target must be credited exactly once");
                for (int player = 0; player < 2; player++) {
                    Bomb bomb = game.bomb(player);
                    assertTrue(Double.isFinite(bomb.x()));
                    assertTrue(Double.isFinite(bomb.y()));
                    assertTrue(Double.isFinite(game.player(player).x()));
                }
            }
            game.stop();
            assertEquals(GameState.STOPPED, game.state());
            game.startNewGame();
            assertEquals(GameRules.MATCH_DURATION_MS, game.timeRemainingMs());
            assertEquals(0, game.score(0) + game.score(1));
            assertEquals(0, game.canyonStrikes(0) + game.canyonStrikes(1));
            assertTrue(game.targets().stream().noneMatch(Target::isDestroyed));
        }
    }

    @Test void sameSeedAndInputScheduleHaveEquivalentResults() {
        for (int seed = 0; seed < 12; seed++) {
            Random random = new Random(seed + 1);
            GameModel left = new GameModel();
            GameModel right = new GameModel();
            left.startNewGame();
            right.startNewGame();
            for (int i = 0; i < 240; i++) {
                int command = random.nextInt(12);
                if (command < 2) {
                    left.pressFire(command);
                    right.pressFire(command);
                } else if (command < 4) {
                    left.releaseFire(command - 2);
                    right.releaseFire(command - 2);
                }
                left.tick(16);
                right.tick(16);
                assertEquals(left.score(0), right.score(0));
                assertEquals(left.score(1), right.score(1));
                assertEquals(left.canyonStrikes(0), right.canyonStrikes(0));
                assertEquals(left.canyonStrikes(1), right.canyonStrikes(1));
                assertEquals(left.bomb(0).isActive(), right.bomb(0).isActive());
                assertEquals(left.bomb(1).isActive(), right.bomb(1).isActive());
                assertEquals(left.timeRemainingMs(), right.timeRemainingMs());
                assertEquals(left.state(), right.state());
            }
        }
    }

    @Test void expiryIsFinalAndCannotDuplicateGameOverEvent() {
        GameModel game = new GameModel(List.of(), 80, 656);
        game.startNewGame();
        game.tick(60_000);
        assertEquals(GameState.RESULTS, game.state());
        assertEquals(0, game.timeRemainingMs());
        long firstEvents = game.drainFeedback().stream()
                .filter(f -> f.type() == GameModel.FeedbackType.ROUND_FINISHED).count();
        assertEquals(1, firstEvents);
        game.tick(5_000);
        game.pressFire(0);
        assertTrue(game.drainFeedback().isEmpty());
        game.startNewGame();
        assertEquals(GameState.RUNNING, game.state());
        assertEquals("", game.resultText());
    }

    @Test void boundaryContactMethodsRejectNoRealImpact() {
        Target target = new Target(300, 550, 20, 20, 2);
        assertEquals(Double.POSITIVE_INFINITY,
                GameRules.contactTime(0, 0, 100, 100, 12, target));
        assertEquals(Double.POSITIVE_INFINITY,
                GameRules.exitTime(80, 200, 100, 250, 12));
        assertTrue(Double.isFinite(
                GameRules.contactTime(305, 560, 305, 560, 12, target)));
    }
}
