import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.Image;
import java.util.Random;

/**
 * A renderer and keyboard adapter only. All game mutations are performed by the
 * model on Swing's EDT; no custom simulation threads are launched.
 */
public final class Gameplay extends JPanel {
    private final GameModel model = new GameModel();
    private final GameController controller = new GameController(model, this::repaint, this::repaint);
    private final Image background = Assets.image("/Resource/General/background.png");
    private final Image[] planes = new Image[2];
    private final Image[] bombImages = {
            Assets.image("/Resource/Bomb/Blue.png"), Assets.image("/Resource/Bomb/Orange.png")
    };
    private final Image[] targetImages = new Image[4];
    private final Font hudFont = Assets.font("/Resource/Font/Pixel.ttf", 26f);
    private final JButton pause = new JButton("Pause");

    public Gameplay(Runnable onMenu) {
        setBackground(new Color(11, 19, 37));
        setFocusable(true);
        setLayout(null);

        Random random = new Random();
        planes[0] = Assets.image("/Resource/Player/BLUE_TEAM/option" + (random.nextInt(5) + 1) + ".png");
        planes[1] = Assets.image("/Resource/Player/ORANGE_TEAM/option" + (random.nextInt(5) + 1) + ".png");
        for (int i = 0; i < targetImages.length; i++) {
            targetImages[i] = Assets.image("/Resource/Target/" + (i + 1) + ".png");
        }

        JButton menu = new JButton("Menu");
        JButton restart = new JButton("Restart");
        menu.setBounds(10, 10, 90, 34);
        pause.setBounds(108, 10, 100, 34);
        restart.setBounds(216, 10, 100, 34);
        menu.addActionListener(event -> onMenu.run());
        pause.addActionListener(event -> togglePause());
        restart.addActionListener(event -> {
            controller.startNewGame();
            pause.setText("Pause");
            requestFocusInWindow();
        });
        // Focused buttons intercept the fire keys before panel bindings.
        menu.setFocusable(false);
        pause.setFocusable(false);
        restart.setFocusable(false);
        add(menu);
        add(pause);
        add(restart);

        installBindings();
    }

    private void bind(int key, boolean released, String name, Runnable callback) {
        InputMap input = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actions = getActionMap();
        input.put(KeyStroke.getKeyStroke(key, 0, released), name);
        actions.put(name, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent event) {
                callback.run();
            }
        });
    }

    private void installBindings() {
        bind(KeyEvent.VK_ENTER, false, "blue-down", () -> model.pressFire(0));
        bind(KeyEvent.VK_ENTER, true, "blue-up", () -> model.releaseFire(0));
        bind(KeyEvent.VK_SPACE, false, "orange-down", () -> model.pressFire(1));
        bind(KeyEvent.VK_SPACE, true, "orange-up", () -> model.releaseFire(1));
        bind(KeyEvent.VK_P, false, "pause-toggle", this::togglePause);
        bind(KeyEvent.VK_R, false, "restart", () -> {
            controller.startNewGame();
            pause.setText("Pause");
        });
    }

    public void startGame() {
        controller.startNewGame();
    }

    public void pauseGame() {
        controller.pause();
        if (model.state() == GameState.PAUSED) {
            pause.setText("Resume");
        }
    }

    public void resumeGame() {
        controller.resume();
        if (model.state() == GameState.RUNNING) {
            pause.setText("Pause");
            requestFocusInWindow();
        }
    }

    public void togglePause() {
        if (model.state() == GameState.RUNNING) {
            pauseGame();
        } else if (model.state() == GameState.PAUSED) {
            resumeGame();
        }
    }

    public void disposeGame() { controller.dispose(); }
    public GameModel model() { return model; }
    public boolean timerRunning() { return controller.timerRunning(); }

    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
            for (Target target : model.targets()) {
                if (!target.isDestroyed()) {
                    g.drawImage(targetImages[target.value() - 1],
                            target.x(), target.y(), target.width(), target.height(), this);
                }
            }
            for (int i = 0; i < 2; i++) {
                Player player = model.player(i);
                g.drawImage(planes[i], (int) player.x(), (int) player.y(),
                        player.width(), player.height(), this);
                Bomb bomb = model.bomb(i);
                if (bomb.isActive()) {
                    g.drawImage(bombImages[i], (int) bomb.x(), (int) bomb.y(),
                            bomb.size(), bomb.size(), this);
                }
            }

            g.setFont(hudFont.deriveFont(23f));
            g.setColor(new Color(255, 169, 47));
            g.drawString("Orange: " + model.score(1), 24, 92);
            g.setFont(hudFont.deriveFont(18f));
            g.drawString("Strikes: " + model.canyonStrikes(1) + "/" + GameRules.MAX_CANYON_STRIKES, 24, 122);
            g.setFont(hudFont.deriveFont(23f));
            g.setColor(new Color(98, 183, 255));
            g.drawString("Blue: " + model.score(0), 556, 92);
            g.setFont(hudFont.deriveFont(18f));
            g.drawString("Strikes: " + model.canyonStrikes(0) + "/" + GameRules.MAX_CANYON_STRIKES, 556, 122);
            g.setFont(hudFont.deriveFont(26f));
            g.setColor(Color.WHITE);
            g.drawString("Time: " + ((model.timeRemainingMs() + 999) / 1000), 338, 130);

            if (model.state() == GameState.PAUSED || model.state() == GameState.RESULTS) {
                g.setColor(new Color(0, 0, 0, 170));
                g.fillRect(80, 290, 640, 130);
                g.setColor(Color.WHITE);
                String message = model.state() == GameState.PAUSED ? "Paused" : model.resultText();
                float fontSize = 30f;
                g.setFont(hudFont.deriveFont(fontSize));
                while (fontSize > 16f && g.getFontMetrics().stringWidth(message) > 610) {
                    fontSize -= 2f;
                    g.setFont(hudFont.deriveFont(fontSize));
                }
                int offset = g.getFontMetrics().stringWidth(message) / 2;
                g.drawString(message, 400 - offset, 370);
            }
        } finally {
            g.dispose();
        }
    }
}
