import org.junit.jupiter.api.Test;
import javax.swing.SwingUtilities;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.JPanel;
import javax.swing.JLabel;
import static org.junit.jupiter.api.Assertions.*;

class ResponsiveHudTest {
    @Test void hudAlwaysExposesIndependentAccessibleScoreTimeAndStateCells() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Gameplay game = new Gameplay(() -> {}, () -> {}, () -> {}, () -> {},
                    UserPreferences.defaults(), next -> {});
            game.setSize(620, 480);
            game.doLayout();
            game.startGame();
            assertEquals("BLUE 0 pts  /  0 strikes", game.hudCell(0));
            assertEquals("ORANGE 0 pts  /  0 strikes", game.hudCell(1));
            assertEquals("TIME 60s", game.hudCell(2));
            assertEquals("STATE RUNNING", game.hudCell(3));
            assertEquals("SOUND OFF", game.hudCell(4));
            assertEquals("BETA " + AppVersion.NUMBER, game.hudCell(5));
            JPanel hud = (JPanel) game.getComponent(2); // BorderLayout.SOUTH
            assertTrue(hud.getLayout() instanceof GridLayout);
            assertEquals(2, ((GridLayout) hud.getLayout()).getRows());
            assertEquals(3, ((GridLayout) hud.getLayout()).getColumns());
            assertEquals(6, hud.getComponentCount());
            for (java.awt.Component child : hud.getComponents()) {
                assertNotNull(((JLabel) child).getAccessibleContext().getAccessibleName());
            }
            game.pauseGame();
            assertEquals("STATE PAUSED", game.hudCell(3));
            assertFalse(game.timerRunning());
            game.resumeGame();
            assertEquals("STATE RUNNING", game.hudCell(3));
            game.disposeGame();
            assertFalse(game.timerRunning());
        });
    }

    @Test void holdingBlueFireAtRoundEndCannotAccidentallyRestartTheRound() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Gameplay game = new Gameplay(() -> {}, () -> {}, () -> {}, () -> {},
                    UserPreferences.defaults(), next -> {});
            game.startGame();
            game.model().tick(GameRules.MATCH_DURATION_MS);
            assertEquals(GameState.RESULTS, game.model().state());
            Object binding = game.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                    .get(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false));
            assertNotNull(binding);
            for (int repeat = 0; repeat < 12; repeat++) {
                game.getActionMap().get(binding).actionPerformed(
                        new ActionEvent(game, ActionEvent.ACTION_PERFORMED, "held-blue"));
                assertEquals(GameState.RESULTS, game.model().state(),
                        "A held fire key must never dismiss the final score");
            }
            game.restartGame();
            assertEquals(GameState.RUNNING, game.model().state(),
                    "An explicit replay must still work");
            game.disposeGame();
        });
    }
}
