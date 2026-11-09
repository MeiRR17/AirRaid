import org.junit.jupiter.api.Test;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import static org.junit.jupiter.api.Assertions.*;

class ShortcutLatchTest {
    @Test void firstPressOnlyUntilPhysicalRelease() {
        ShortcutLatch latch = new ShortcutLatch();
        assertTrue(latch.press(KeyEvent.VK_P));
        for (int i = 0; i < 30; i++) {
            assertFalse(latch.press(KeyEvent.VK_P),
                    "OS key repeats must not retrigger an action");
        }
        assertTrue(latch.press(KeyEvent.VK_R));
        assertTrue(latch.isHeld(KeyEvent.VK_P));
        latch.release(KeyEvent.VK_P);
        assertTrue(latch.press(KeyEvent.VK_P));
        assertFalse(latch.press(KeyEvent.VK_R));
        latch.clear();
        assertFalse(latch.isHeld(KeyEvent.VK_R));
        assertTrue(latch.press(KeyEvent.VK_R));
    }

    private static void send(Gameplay game, int keyCode, boolean release) {
        Object actionName = game.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .get(KeyStroke.getKeyStroke(keyCode, 0, release));
        assertNotNull(actionName, "Missing " + (release ? "release" : "press")
                + " binding for " + keyCode);
        var action = game.getActionMap().get(actionName);
        assertNotNull(action);
        action.actionPerformed(new ActionEvent(game, ActionEvent.ACTION_PERFORMED,
                release ? "released" : "pressed"));
    }

    @Test void heldPauseKeyCannotRapidlyToggleGameState() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Gameplay game = new Gameplay(() -> {}, () -> {}, () -> {}, () -> {},
                    UserPreferences.defaults(), next -> {});
            try {
                game.startGame();
                send(game, KeyEvent.VK_P, false);
                assertEquals(GameState.PAUSED, game.model().state());
                assertFalse(game.timerRunning());
                int initial = game.model().timeRemainingMs();
                for (int i = 0; i < 12; i++) {
                    send(game, KeyEvent.VK_P, false);
                    assertEquals(GameState.PAUSED, game.model().state());
                }
                assertEquals(initial, game.model().timeRemainingMs());
                send(game, KeyEvent.VK_P, true);
                send(game, KeyEvent.VK_P, false);
                assertEquals(GameState.RUNNING, game.model().state());
                assertTrue(game.timerRunning());
                for (int i = 0; i < 12; i++) {
                    send(game, KeyEvent.VK_P, false);
                    assertEquals(GameState.RUNNING, game.model().state());
                }
            } finally {
                game.disposeGame();
            }
        });
    }

    @Test void heldRestartKeyCannotContinuallyWipeMatchTime() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Gameplay game = new Gameplay(() -> {}, () -> {}, () -> {}, () -> {},
                    UserPreferences.defaults(), next -> {});
            try {
                game.startGame();
                game.model().tick(2500);
                send(game, KeyEvent.VK_R, false);
                assertEquals(GameRules.MATCH_DURATION_MS,
                        game.model().timeRemainingMs());
                game.model().tick(1400);
                for (int i = 0; i < 12; i++) {
                    send(game, KeyEvent.VK_R, false);
                    assertTrue(game.model().timeRemainingMs() < GameRules.MATCH_DURATION_MS,
                            "Key repeat must not keep resetting a round");
                }
                send(game, KeyEvent.VK_R, true);
                send(game, KeyEvent.VK_R, false);
                assertEquals(GameRules.MATCH_DURATION_MS,
                        game.model().timeRemainingMs());
                game.resetShortcutLatches(); // lost release after focus switch
                game.model().tick(1400);
                send(game, KeyEvent.VK_R, false);
                assertEquals(GameRules.MATCH_DURATION_MS,
                        game.model().timeRemainingMs());
            } finally {
                game.disposeGame();
            }
        });
    }
}
