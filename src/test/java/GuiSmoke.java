import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Standalone Xvfb smoke: verify screen flow, resizable game canvas,
 * keyboard bindings, focus-sensitive pause, restart, results, and disposal.
 */
public final class GuiSmoke {
    private GuiSmoke() {}

    private static JButton button(Component root, String text) {
        if (root instanceof JButton b && text.equals(b.getText())) return b;
        if (root instanceof Container container) {
            for (Component child : container.getComponents()) {
                JButton found = button(child, text);
                if (found != null) return found;
            }
        }
        return null;
    }

    private static void action(Gameplay game, int keyCode) {
        Object binding = game.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .get(KeyStroke.getKeyStroke(keyCode, 0, false));
        if (binding == null || game.getActionMap().get(binding) == null) {
            throw new AssertionError("Missing gameplay key: " + keyCode);
        }
        game.getActionMap().get(binding).actionPerformed(
                new ActionEvent(game, ActionEvent.ACTION_PERFORMED, "smoke"));
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    /** Capture the actual Swing game frame in the CI virtual display. */
    private static void captureScreenshot(Window app, String screen) {
        BufferedImage capture = new BufferedImage(app.getWidth(), app.getHeight(),
                BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = capture.createGraphics();
        try {
            app.paint(graphics);
        } finally {
            graphics.dispose();
        }
        File output = new File("target/airraid-v070-ci-" + screen + ".png");
        if (!output.getParentFile().exists() && !output.getParentFile().mkdirs()) {
            throw new AssertionError("Unable to create capture directory");
        }
        try {
            if (!ImageIO.write(capture, "png", output)) {
                throw new AssertionError("PNG encoder unavailable");
            }
        } catch (IOException error) {
            throw new AssertionError("Unable to capture GUI smoke screenshot", error);
        }
    }

    public static void main(String[] args) throws Exception {
        // Isolate all test preferences from runner and developer home directories.
        Path testProfile = Files.createTempDirectory("airraid-v070-gui-profile-");
        System.setProperty("user.home", testProfile.toString());
        CountDownLatch resultsChecked = new CountDownLatch(1);
        AtomicReference<Throwable> deferredError = new AtomicReference<>();
        Main.main(new String[0]);
        SwingUtilities.invokeAndWait(() -> {
            Window frame = null;
            for (java.awt.Window candidate : java.awt.Window.getWindows()) {
                if (candidate instanceof Window app && app.isShowing()) {
                    frame = app;
                    break;
                }
            }
            check(frame != null && "Air Raid".equals(frame.getTitle()), "Main menu not visible");
            final Window app = frame;
            check(app.isResizable(), "Window must be resizable");
            check("menu".equals(app.currentScreen()), "Must start at menu");
            JButton start = button(app.getContentPane(), "Start");
            check(start != null && button(app.getContentPane(), "Rules") != null
                    && button(app.getContentPane(), "Settings") != null
                    && button(app.getContentPane(), "Exit") != null,
                    "Menu buttons missing");

            app.setSize(730, 580);
            app.validate();
            button(app.getContentPane(), "Rules").doClick();
            check("rules".equals(app.currentScreen()), "Integrated rules view missing");
            JButton back = button(app.getContentPane(), "Back");
            check(back != null, "Rules Back action missing");
            back.doClick();
            check("menu".equals(app.currentScreen()), "Rules must return to menu");

            button(app.getContentPane(), "Settings").doClick();
            check("settings".equals(app.currentScreen()), "Settings must open from menu");
            SettingsScreen settings = app.settingsScreen();
            check(!settings.soundToggle().isSelected()
                    && !settings.reducedMotionToggle().isSelected()
                    && settings.volumeSlider().getValue() == 60,
                    "First launch must have safe settings defaults");
            settings.volumeSlider().setValue(37);
            settings.reducedMotionToggle().doClick();
            check(app.preferences().reducedMotion()
                    && app.preferences().volumePercent() == 37
                    && !app.preferences().soundEnabled(),
                    "User preferences must apply on the EDT");
            captureScreenshot(app, "settings");
            settings.backButton().doClick();
            check("menu".equals(app.currentScreen()) && app.menuReducedMotion(),
                    "Returning from settings must show static menu animation");

            start.doClick();
            Gameplay game = app.activeGame();
            check(game != null && "game".equals(app.currentScreen())
                    && game.model().state() == GameState.RUNNING
                    && game.timerRunning(), "Start must activate one simulation");
            GameCanvas.Viewport viewport = GameCanvas.viewportFor(
                    game.canvas().getWidth(), game.canvas().getHeight());
            check(viewport.width() > 0 && viewport.height() > 0
                    && Math.abs(viewport.width() - viewport.height()) <= 1,
                    "Arena must remain square on resized window");
            app.setSize(960, 800);
            app.validate();
            captureScreenshot(app, "gameplay");
            check(button(app.getContentPane(), "Sound: Off") != null
                    && !game.soundEnabled() && game.canvas().isReducedMotion()
                    && game.appliedPreferences().volumePercent() == 37,
                    "Persisted options must affect the active match");

            action(game, KeyEvent.VK_ENTER);
            action(game, KeyEvent.VK_SPACE);
            check(game.model().bomb(0).isActive() && game.model().bomb(1).isActive(),
                    "Enter and Space must fire independently");

            app.showRules();
            check("rules".equals(app.currentScreen())
                    && game.model().state() == GameState.PAUSED
                    && !game.timerRunning(), "Rules must pause an active game");
            button(app.getContentPane(), "Back").doClick();
            check("game".equals(app.currentScreen())
                    && game.model().state() == GameState.PAUSED,
                    "Returning from rules must not silently resume");
            action(game, KeyEvent.VK_P);
            check(game.model().state() == GameState.RUNNING && game.timerRunning(),
                    "P must resume after viewing rules");

            action(game, KeyEvent.VK_S);
            check("settings".equals(app.currentScreen())
                    && game.model().state() == GameState.PAUSED
                    && !game.timerRunning(),
                    "Settings shortcut must pause a running match");
            settings.backButton().doClick();
            check("game".equals(app.currentScreen())
                    && game.model().state() == GameState.PAUSED,
                    "Return from settings must preserve the paused match");
            action(game, KeyEvent.VK_P);
            check(game.model().state() == GameState.RUNNING,
                    "P must explicitly resume after settings");

            action(game, KeyEvent.VK_R);
            check(game.model().score(0) == 0 && game.model().canyonStrikes(1) == 0
                    && !game.model().bomb(0).isActive(), "R must reset the round");
            for (int i = 0; i < 10; i++) {
                app.returnToMenu();
                check(!game.timerRunning() && game.model().state() == GameState.STOPPED,
                        "Old game must not leak a timer");
                app.startGame();
                game = app.activeGame();
                check(game != null && game.timerRunning(), "Restart cycle " + i);
            }

            final Gameplay finishedGame = game;
            finishedGame.model().tick(GameModel.MATCH_DURATION_MS);
            check(finishedGame.model().state() == GameState.RESULTS,
                    "Time limit must produce results");
            Timer deferred = new Timer(120, event -> {
                ((Timer) event.getSource()).stop();
                try {
                    check(finishedGame.resultsVisible(), "Results card did not appear");
                    check(!finishedGame.timerRunning(), "Timer remained active after results");
                    check("It's a tie!".equals(finishedGame.resultText()), "Tie summary missing");
                    JButton replay = button(finishedGame, "Play again");
                    check(replay != null, "Play again action missing");
                    replay.doClick();
                    check(finishedGame.model().state() == GameState.RUNNING
                            && finishedGame.timerRunning()
                            && !finishedGame.resultsVisible(),
                            "Replay must reset to arena");
                    action(finishedGame, KeyEvent.VK_ESCAPE);
                    check("menu".equals(app.currentScreen())
                            && app.activeGame() == null
                            && !finishedGame.timerRunning(),
                            "Escape must stop round and return to menu");
                } catch (Throwable failure) {
                    deferredError.set(failure);
                } finally {
                    app.dispose();
                    resultsChecked.countDown();
                }
            });
            deferred.setRepeats(false);
            deferred.start();
        });
        check(resultsChecked.await(5, TimeUnit.SECONDS), "Timed out awaiting results UI");
        if (deferredError.get() != null) {
            throw new AssertionError("Results/replay GUI smoke failed", deferredError.get());
        }
        PreferencesStore.LoadResult persisted = PreferencesStore.forCurrentUser().load();
        check(!persisted.recovered()
                && persisted.preferences().equals(new UserPreferences(false, 37, true)),
                "Settings must persist after leaving and reopening the profile store");
        System.out.println("AIRRAID_GUI_SMOKE_PASS");
        System.exit(0);
    }
}
