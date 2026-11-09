import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.Toolkit;
import java.awt.HeadlessException;
import java.awt.datatransfer.StringSelection;
import java.util.function.Consumer;

/**
 * Desktop game flow: no absolute-positioned controls, independent keyboard
 * bindings, a scalable arena and a separate round-results card.
 */
public final class Gameplay extends JPanel {
    private static final String ARENA = "arena";
    private static final String RESULTS = "results";

    private final GameModel model = new GameModel();
    private final GameCanvas canvas = new GameCanvas(model);
    private final CardLayout centerNavigation = new CardLayout();
    private final JPanel centerCards = new JPanel(centerNavigation);
    /** Six concise status cells remain readable in a 620px-wide window. */
    private final JLabel[] hudLabels = new JLabel[6];
    private final JPanel hud = new JPanel(new GridLayout(2, 3, 6, 3));
    private final JButton pauseButton = new JButton("Pause");
    private final JButton soundButton = new JButton("Sound: Off");
    private final SoundEffects sound = new SoundEffects();
    private final ResultsScreen results;
    private final GameController controller;
    private final Runnable onMenu;
    private final Consumer<UserPreferences> onPreferenceChange;
    private UserPreferences preferences;
    private boolean showingResults;

    public Gameplay(Runnable onMenu, Runnable onRules, Runnable onSettings,
                    Runnable onChecklist, UserPreferences initialPreferences,
                    Consumer<UserPreferences> onPreferenceChange) {
        this.onMenu = onMenu;
        this.onPreferenceChange = onPreferenceChange;
        this.preferences = initialPreferences;
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(11, 19, 37));
        setFocusable(true);

        JButton menuButton = new JButton("Menu");
        JButton restartButton = new JButton("Restart");
        JButton rulesButton = new JButton("Rules");
        JButton settingsButton = new JButton("Settings");
        JButton qaButton = new JButton("QA checklist");
        qaButton.setToolTipText("Pause and open the manual beta test checklist (Q)");
        qaButton.addActionListener(event -> onChecklist.run());
        settingsButton.setToolTipText("Open local accessibility and audio settings (S)");
        settingsButton.getAccessibleContext().setAccessibleDescription(
                "Open settings; active match pauses until explicitly resumed");
        menuButton.addActionListener(event -> onMenu.run());
        pauseButton.addActionListener(event -> togglePause());
        restartButton.addActionListener(event -> restartGame());
        rulesButton.addActionListener(event -> onRules.run());
        settingsButton.addActionListener(event -> onSettings.run());
        soundButton.addActionListener(event -> toggleSound());
        soundButton.setToolTipText("Optional original audio feedback; off by default (M)");
        soundButton.getAccessibleContext().setAccessibleDescription(
                "Toggle synthesized game sound. Disabled by default.");

        // These buttons are reachable by labelled shortcuts; keeping them out of
        // the tab focus path avoids Swing consuming Space/Enter as button clicks
        // instead of the two players' fire commands.
        for (JButton button : new JButton[] {
                menuButton, pauseButton, restartButton, rulesButton,
                settingsButton, qaButton, soundButton
        }) {
            button.setFocusable(false);
            button.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
        }
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEADING, 8, 5));
        toolbar.setBackground(new Color(21, 38, 60));
        toolbar.add(menuButton);
        toolbar.add(pauseButton);
        toolbar.add(restartButton);
        toolbar.add(rulesButton);
        toolbar.add(settingsButton);
        toolbar.add(qaButton);
        toolbar.add(soundButton);
        add(toolbar, BorderLayout.NORTH);

        results = new ResultsScreen(this::restartGame, onMenu, this::copyQaReport);
        centerCards.add(canvas, ARENA);
        centerCards.add(results, RESULTS);
        add(centerCards, BorderLayout.CENTER);

        hud.setBackground(new Color(21, 38, 60));
        hud.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        String[] names = {"Blue points and strikes", "Orange points and strikes",
                "Remaining match time", "Match state",
                "Sound and reduced motion status", "Development build version"};
        for (int index = 0; index < hudLabels.length; index++) {
            JLabel label = new JLabel("", JLabel.CENTER);
            label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
            label.setForeground(index == 0 ? new Color(129, 205, 255)
                    : index == 1 ? new Color(255, 185, 123) : Color.WHITE);
            label.getAccessibleContext().setAccessibleName(names[index]);
            hudLabels[index] = label;
            hud.add(label);
        }
        add(hud, BorderLayout.SOUTH);

        controller = new GameController(model, this::refresh, this::onRoundFinished);
        installBindings(onRules, onSettings, onChecklist);
        applyPreferences(initialPreferences);
    }

    private void bind(int key, boolean released, String name, Runnable callback) {
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(key, 0, released), name);
        getActionMap().put(name, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent event) { callback.run(); }
        });
    }

    private void installBindings(Runnable onRules, Runnable onSettings, Runnable onChecklist) {
        // Enter must never accidentally clear a finished round when the
        // player continues holding their blue-fire key at the results screen.
        // Replay requires the explicit Play again button or R shortcut.
        bind(KeyEvent.VK_ENTER, false, "blue-down", () -> model.pressFire(0));
        bind(KeyEvent.VK_ENTER, true, "blue-up", () -> model.releaseFire(0));
        bind(KeyEvent.VK_SPACE, false, "orange-down", () -> model.pressFire(1));
        bind(KeyEvent.VK_SPACE, true, "orange-up", () -> model.releaseFire(1));
        bind(KeyEvent.VK_P, false, "pause-toggle", this::togglePause);
        bind(KeyEvent.VK_R, false, "restart", this::restartGame);
        bind(KeyEvent.VK_H, false, "rules", onRules);
        bind(KeyEvent.VK_S, false, "settings", onSettings);
        bind(KeyEvent.VK_Q, false, "qa-checklist", onChecklist);
        bind(KeyEvent.VK_ESCAPE, false, "menu", onMenu);
        bind(KeyEvent.VK_M, false, "sound", this::toggleSound);
    }

    public void startGame() { restartGame(); }

    public void restartGame() {
        controller.startNewGame();
        canvas.clearFeedback();
        showingResults = false;
        centerNavigation.show(centerCards, ARENA);
        pauseButton.setText("Pause");
        refresh();
        focusGame();
    }

    private void refresh() {
        // The model publishes only small immutable feedback events. No extra
        // simulation thread, sound file or render-time disk I/O is introduced.
        canvas.advanceFeedback();
        for (GameModel.Feedback feedback : model.drainFeedback()) {
            canvas.showFeedback(feedback);
            if (feedback.type() == GameModel.FeedbackType.SHOT) {
                sound.play(SoundEffects.Cue.SHOT);
            } else if (feedback.type() == GameModel.FeedbackType.TARGET_HIT) {
                sound.play(SoundEffects.Cue.HIT);
            } else if (feedback.type() == GameModel.FeedbackType.CANYON_MISS) {
                sound.play(SoundEffects.Cue.MISS);
            } else if (feedback.type() == GameModel.FeedbackType.ROUND_FINISHED) {
                sound.play(SoundEffects.Cue.ROUND_END);
            }
        }
        setHud(0, "BLUE " + model.score(0) + " pts  /  "
                + model.canyonStrikes(0) + " strikes");
        setHud(1, "ORANGE " + model.score(1) + " pts  /  "
                + model.canyonStrikes(1) + " strikes");
        setHud(2, "TIME " + ((model.timeRemainingMs() + 999) / 1000) + "s");
        setHud(3, "STATE " + model.state());
        setHud(4, "SOUND " + (sound.isEnabled() ? "ON" : "OFF")
                + (preferences.reducedMotion() ? "  /  LOW MOTION" : ""));
        setHud(5, "BETA " + AppVersion.NUMBER);
        canvas.repaint();
    }

    private void setHud(int index, String value) {
        if (!value.equals(hudLabels[index].getText())) hudLabels[index].setText(value);
    }

    /** Returns the visible text of a single scoreboard cell for accessibility tests. */
    String hudCell(int index) { return hudLabels[index].getText(); }

    /** Preferences are mirrored immediately, but only Window writes to disk. */
    public void applyPreferences(UserPreferences next) {
        preferences = next;
        canvas.setReducedMotion(next.reducedMotion());
        sound.setVolume(next.volumePercent());
        if (!next.soundEnabled()) {
            sound.setEnabled(false);
            soundButton.setText("Sound: Off");
        } else if (sound.setEnabled(true)) {
            soundButton.setText("Sound: On");
        } else {
            soundButton.setText("Audio unavailable");
            soundButton.setToolTipText(
                    "Audio output not available; settings remain saved");
        }
        refresh();
    }

    private void toggleSound() {
        onPreferenceChange.accept(preferences.withSound(!preferences.soundEnabled()));
    }

    private boolean copyQaReport(String report) {
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                    new StringSelection(report), null);
            return true;
        } catch (HeadlessException | IllegalStateException | SecurityException unavailable) {
            // Do not log local clipboard contents or abort the round.
            return false;
        }
    }

    private void onRoundFinished() {
        results.showResult(model, preferences);
        centerNavigation.show(centerCards, RESULTS);
        showingResults = true;
        pauseButton.setText("Pause");
        refresh();
        results.focusReplay();
    }

    public void pauseGame() {
        controller.pause();
        if (model.state() == GameState.PAUSED) pauseButton.setText("Resume");
    }

    public void resumeGame() {
        controller.resume();
        if (model.state() == GameState.RUNNING) {
            pauseButton.setText("Pause");
            focusGame();
        }
    }

    public void togglePause() {
        if (model.state() == GameState.RUNNING) pauseGame();
        else if (model.state() == GameState.PAUSED) resumeGame();
    }

    public void focusGame() {
        if (showingResults) results.focusReplay();
        else requestFocusInWindow();
    }

    public void disposeGame() {
        controller.dispose();
        sound.close();
        canvas.clearFeedback();
    }
    public GameModel model() { return model; }
    public boolean timerRunning() { return controller.timerRunning(); }
    public boolean resultsVisible() { return showingResults; }
    public String resultText() { return results.outcomeText(); }
    public GameCanvas canvas() { return canvas; }
    public boolean soundEnabled() { return sound.isEnabled(); }
    public UserPreferences appliedPreferences() { return preferences; }
    public String completedQaReport() { return results.reportText(); }
}
