import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.BasicStroke;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Pixel-independent drawing surface. The model always uses an 800x800 logical
 * arena; resizing letterboxes the whole arena instead of distorting sprites
 * or shifting target collision coordinates.
 */
public final class GameCanvas extends JPanel {
    public record Viewport(int x, int y, int width, int height, double scale) {}

    private final GameModel model;
    private final Image background = Assets.image("/Resource/General/background.png");
    private final Image[] planeImages = new Image[2];
    private final Image[] bombs = {
            Assets.image("/Resource/Bomb/Blue.png"),
            Assets.image("/Resource/Bomb/Orange.png")
    };
    private final Image[] targets = new Image[4];
    private final Font hudSmall = Assets.font("/Resource/Font/Pixel.ttf", 18f);
    private final Font hudMedium = Assets.font("/Resource/Font/Pixel.ttf", 23f);
    private final Font hudLarge = Assets.font("/Resource/Font/Pixel.ttf", 26f);

    private static final int EFFECT_LIFETIME_TICKS = 24;
    private final List<Effect> effects = new ArrayList<>();
    private boolean reducedMotion;

    public void setReducedMotion(boolean reduced) {
        reducedMotion = reduced;
        if (reduced) clearFeedback();
        repaint();
    }

    public boolean isReducedMotion() { return reducedMotion; }

    private static final class Effect {
        final GameModel.FeedbackType type;
        final int player;
        final int points;
        final double x;
        final double y;
        int ticks = EFFECT_LIFETIME_TICKS;

        Effect(GameModel.Feedback event) {
            type = event.type();
            player = event.player();
            points = event.points();
            x = event.x();
            y = event.y();
        }
    }

    /**
     * Short-lived hit/miss/shot indicators; no extra Swing Timer or OS threads.
     * Effects are UI-only and cannot alter any score or collision decision.
     */
    public void showFeedback(GameModel.Feedback event) {
        if (reducedMotion || event.type() == GameModel.FeedbackType.ROUND_FINISHED) return;
        if (effects.size() >= 16) effects.remove(0);
        effects.add(new Effect(event));
    }

    public void advanceFeedback() {
        for (Iterator<Effect> it = effects.iterator(); it.hasNext();) {
            if (--it.next().ticks <= 0) it.remove();
        }
    }

    public void clearFeedback() { effects.clear(); }
    public int visibleEffectCount() { return effects.size(); }

    public GameCanvas(GameModel model) {
        this.model = Objects.requireNonNull(model);
        setOpaque(true);
        setBackground(new Color(11, 19, 37));
        setFocusable(false);
        setToolTipText("AirRaid arena. Blue fires Enter; Orange fires Space; P pauses.");
        getAccessibleContext().setAccessibleName("AirRaid game arena");
        Random random = new Random();
        planeImages[0] = Assets.image("/Resource/Player/BLUE_TEAM/option"
                + (random.nextInt(5) + 1) + ".png");
        planeImages[1] = Assets.image("/Resource/Player/ORANGE_TEAM/option"
                + (random.nextInt(5) + 1) + ".png");
        for (int i = 0; i < targets.length; i++) {
            targets[i] = Assets.image("/Resource/Target/" + (i + 1) + ".png");
        }
    }

    /** Compute an undistorted, centered viewport for any component dimensions. */
    public static Viewport viewportFor(int width, int height) {
        int w = Math.max(0, width);
        int h = Math.max(0, height);
        if (w == 0 || h == 0) return new Viewport(0, 0, 0, 0, 0.0);
        double scale = Math.min(w / (double) GameRules.WIDTH,
                                h / (double) GameRules.HEIGHT);
        int renderedWidth = (int) Math.floor(scale * GameRules.WIDTH);
        int renderedHeight = (int) Math.floor(scale * GameRules.HEIGHT);
        return new Viewport((w - renderedWidth) / 2, (h - renderedHeight) / 2,
                            renderedWidth, renderedHeight, scale);
    }

    private void paintEffects(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setStroke(new BasicStroke(2.3f));
        for (Effect effect : effects) {
            double progress = 1.0 - effect.ticks / (double) EFFECT_LIFETIME_TICKS;
            int alpha = Math.max(0, Math.min(255, (int) (220 * (1.0 - progress))));
            Color color = switch (effect.type) {
                case TARGET_HIT -> new Color(255, 221, 90, alpha);
                case CANYON_MISS -> new Color(255, 100, 84, alpha);
                case SHOT -> effect.player == 0
                        ? new Color(105, 200, 255, alpha)
                        : new Color(255, 164, 55, alpha);
                default -> new Color(255, 255, 255, alpha);
            };
            g.setColor(color);
            int x = (int) Math.max(32, Math.min(GameRules.WIDTH - 32, effect.x));
            int y = (int) Math.max(32, Math.min(GameRules.HEIGHT - 32, effect.y));
            int radius = (int) (8 + 23 * progress);
            g.drawOval(x - radius, y - radius, 2 * radius, 2 * radius);
            if (effect.type == GameModel.FeedbackType.TARGET_HIT) {
                g.setFont(hudSmall);
                g.drawString("+" + effect.points, x - 12, y - 24 - (int) (24 * progress));
            } else if (effect.type == GameModel.FeedbackType.CANYON_MISS) {
                g.setFont(hudSmall);
                g.drawString("MISS", x - 25, y - 25 - (int) (16 * progress));
            }
        }
    }

    private static void drawCentered(Graphics2D g, String message, Font font,
                                     Color color, int baseline) {
        g.setFont(font);
        g.setColor(color);
        int width = g.getFontMetrics().stringWidth(message);
        g.drawString(message, (GameRules.WIDTH - width) / 2, baseline);
    }

    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
            Viewport viewport = viewportFor(getWidth(), getHeight());
            if (viewport.width() == 0 || viewport.height() == 0) return;
            g.translate(viewport.x(), viewport.y());
            g.scale(viewport.scale(), viewport.scale());
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                               RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(background, 0, 0, GameRules.WIDTH, GameRules.HEIGHT, this);

            for (Target target : model.targets()) {
                if (!target.isDestroyed()) {
                    g.drawImage(targets[target.value() - 1],
                                target.x(), target.y(),
                                target.width(), target.height(), this);
                }
            }
            for (int i = 0; i < 2; i++) {
                Player plane = model.player(i);
                g.drawImage(planeImages[i], (int) plane.x(), (int) plane.y(),
                            plane.width(), plane.height(), this);
                Bomb bomb = model.bomb(i);
                if (bomb.isActive()) {
                    g.drawImage(bombs[i], (int) bomb.x(), (int) bomb.y(),
                                bomb.size(), bomb.size(), this);
                }
            }

            paintEffects(g);
            g.setFont(hudMedium);
            g.setColor(new Color(255, 169, 47));
            g.drawString("Orange: " + model.score(1), 24, 92);
            g.setFont(hudSmall);
            g.drawString("Strikes: " + model.canyonStrikes(1)
                    + "/" + GameRules.MAX_CANYON_STRIKES, 24, 122);
            g.setFont(hudMedium);
            g.setColor(new Color(98, 183, 255));
            g.drawString("Blue: " + model.score(0), 556, 92);
            g.setFont(hudSmall);
            g.drawString("Strikes: " + model.canyonStrikes(0)
                    + "/" + GameRules.MAX_CANYON_STRIKES, 556, 122);
            g.setFont(hudLarge);
            g.setColor(Color.WHITE);
            g.drawString("Time: " + ((model.timeRemainingMs() + 999) / 1000), 338, 130);

            if (model.state() == GameState.PAUSED) {
                // Fixed-size overlay in logical arena coordinates: no timer,
                // flashing, animation or input interception.
                g.setColor(new Color(7, 18, 33, 218));
                g.fillRoundRect(104, 269, 592, 216, 24, 24);
                g.setColor(new Color(111, 205, 255));
                g.setStroke(new BasicStroke(3f));
                g.drawRoundRect(104, 269, 592, 216, 24, 24);
                drawCentered(g, "MATCH PAUSED", hudLarge.deriveFont(37f),
                        Color.WHITE, 345);
                drawCentered(g, "Press P or click Resume", hudMedium,
                        new Color(170, 220, 255), 397);
                drawCentered(g, "Match clock stops while paused", hudSmall,
                        new Color(224, 237, 249), 441);
            }
        } finally {
            g.dispose();
        }
    }
}
