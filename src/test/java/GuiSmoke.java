import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;

/**
 * Runs under a virtual display in CI; verifies navigation and safe timer lifecycle.
 * A standalone main rather than JUnit, since ordinary tests execute headlessly.
 */
public final class GuiSmoke {
    private GuiSmoke() {}

    private static JButton button(Component root, String text) {
        if (root instanceof JButton b && b.getText().equals(text)) {
            return b;
        }
        if (root instanceof Container container) {
            for (Component child : container.getComponents()) {
                JButton match = button(child, text);
                if (match != null) {
                    return match;
                }
            }
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        Main.main(new String[0]);
        SwingUtilities.invokeAndWait(() -> {
            Window frame = null;
            for (java.awt.Window candidate : java.awt.Window.getWindows()) {
                if (candidate instanceof Window app && app.isShowing()) {
                    frame = app;
                    break;
                }
            }
            if (frame == null || !"Air Raid".equals(frame.getTitle())) {
                throw new AssertionError("Main Air Raid menu not visible");
            }
            JButton start = button(frame.getContentPane(), "Start");
            if (start == null) {
                throw new AssertionError("Start button missing");
            }
            start.doClick();
            Gameplay gameplay = frame.activeGame();
            if (gameplay == null || gameplay.model().state() != GameState.RUNNING
                    || !gameplay.timerRunning()) {
                throw new AssertionError("Gameplay did not start");
            }
            gameplay.pauseGame();
            if (gameplay.model().state() != GameState.PAUSED || gameplay.timerRunning()) {
                throw new AssertionError("Pause did not stop timer");
            }
            gameplay.resumeGame();
            if (gameplay.model().state() != GameState.RUNNING || !gameplay.timerRunning()) {
                throw new AssertionError("Resume did not restart timer");
            }
            JButton restart = button(gameplay, "Restart");
            if (restart == null) {
                throw new AssertionError("Restart button missing");
            }
            restart.doClick();
            if (gameplay.model().state() != GameState.RUNNING
                    || gameplay.model().score(0) != 0) {
                throw new AssertionError("Restart did not reset match");
            }
            frame.returnToMenu();
            if (gameplay.timerRunning() || gameplay.model().state() != GameState.STOPPED
                    || frame.activeGame() != null) {
                throw new AssertionError("Returning to menu leaked an active simulation");
            }
            frame.dispose();
            System.out.println("AIRRAID_GUI_SMOKE_PASS");
        });
    }
}
