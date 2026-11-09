import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.Window;

/**
 * Minimal graphical smoke test: run with a display (e.g. xvfb-run), not by JUnit in headless mode.
 * The legacy game still starts background threads, so explicitly exit after the window check.
 */
public final class GuiSmoke {
    private GuiSmoke() {}

    public static void main(String[] args) throws Exception {
        Main.main(new String[0]);
        SwingUtilities.invokeAndWait(() -> {
            boolean mainWindowVisible = false;
            boolean rulesWindowVisible = false;
            for (Window window : Window.getWindows()) {
                if (window instanceof JFrame frame && window.isShowing()) {
                    mainWindowVisible |= "Air Raid".equals(frame.getTitle());
                    rulesWindowVisible |= "Rules Before The Gaming / RBTG".equals(frame.getTitle());
                }
            }
            if (!mainWindowVisible || !rulesWindowVisible) {
                throw new AssertionError("Main or rules window did not become visible");
            }
            System.out.println("AIRRAID_GUI_SMOKE_PASS");
            for (Window window : Window.getWindows()) {
                window.dispose();
            }
        });
        // Current pre-v0.2.0 game threads are not lifecycle-safe; terminate the test process.
        System.exit(0);
    }
}
