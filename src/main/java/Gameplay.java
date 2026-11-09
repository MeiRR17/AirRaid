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
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

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
    private final JLabel status = new JLabel();
    private final JButton pauseButton = new JButton("Pause");
    private final ResultsScreen results;
    private final GameController controller;
    private final Runnable onMenu;
    private boolean showingResults;

    public Gameplay(Runnable onMenu, Runnable onRules) {
        this.onMenu = onMenu;
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(11, 19, 37));
        setFocusable(true);

        JButton menuButton = new JButton("Menu");
        JButton restartButton = new JButton("Restart");
        JButton rulesButton = new JButton("Rules");
        menuButton.addActionListener(event -> onMenu.run());
        pauseButton.addActionListener(event -> togglePause());
        restartButton.addActionListener(event -> restartGame());
        rulesButton.addActionListener(event -> onRules.run());

        // These buttons are reachable by labelled shortcuts; keeping them out of
        // the tab focus path avoids Swing consuming Space/Enter as button clicks
        // instead of the two players' fire commands.
        for (JButton button : new JButton[] {
                menuButton, pauseButton, restartButton, rulesButton
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
        add(toolbar, BorderLayout.NORTH);

        results = new ResultsScreen(this::restartGame, onMenu);
        centerCards.add(canvas, ARENA);
        centerCards.add(results, RESULTS);
        add(centerCards, BorderLayout.CENTER);

        status.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        status.setForeground(Color.WHITE);
        status.setBackground(new Color(21, 38, 60));
        status.setOpaque(true);
        status.setBorder(BorderFactory.createEmptyBorder(9, 12, 9, 12));
        status.getAccessibleContext().setAccessibleName("Game state and scores");
        add(status, BorderLayout.SOUTH);

        controller = new GameController(model, this::refresh, this::onRoundFinished);
        installBindings(onRules);
        refresh();
    }

    private void bind(int key, boolean released, String name, Runnable callback) {
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(key, 0, released), name);
        getActionMap().put(name, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent event) { callback.run(); }
        });
    }

    private void installBindings(Runnable onRules) {
        bind(KeyEvent.VK_ENTER, false, "blue-down", () -> {
            if (model.state() == GameState.RESULTS) restartGame();
            else model.pressFire(0);
        });
        bind(KeyEvent.VK_ENTER, true, "blue-up", () -> model.releaseFire(0));
        bind(KeyEvent.VK_SPACE, false, "orange-down", () -> model.pressFire(1));
        bind(KeyEvent.VK_SPACE, true, "orange-up", () -> model.releaseFire(1));
        bind(KeyEvent.VK_P, false, "pause-toggle", this::togglePause);
        bind(KeyEvent.VK_R, false, "restart", this::restartGame);
        bind(KeyEvent.VK_H, false, "rules", onRules);
        bind(KeyEvent.VK_ESCAPE, false, "menu", onMenu);
    }

    public void startGame() { restartGame(); }

    public void restartGame() {
        controller.startNewGame();
        showingResults = false;
        centerNavigation.show(centerCards, ARENA);
        pauseButton.setText("Pause");
        refresh();
        focusGame();
    }

    private void refresh() {
        String message = "BLUE " + model.score(0) + " pts / " + model.canyonStrikes(0)
                + " strikes     ORANGE " + model.score(1) + " pts / "
                + model.canyonStrikes(1) + " strikes     "
                + ((model.timeRemainingMs() + 999) / 1000) + "s     "
                + model.state();
        if (!message.equals(status.getText())) status.setText(message);
        canvas.repaint();
    }

    private void onRoundFinished() {
        results.showResult(model);
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

    public void disposeGame() { controller.dispose(); }
    public GameModel model() { return model; }
    public boolean timerRunning() { return controller.timerRunning(); }
    public boolean resultsVisible() { return showingResults; }
    public String resultText() { return results.outcomeText(); }
    public GameCanvas canvas() { return canvas; }
}
