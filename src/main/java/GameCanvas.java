import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.util.Objects;
import java.util.Random;

/**
 * Pixel-independent drawing surface. The model always uses an 800x800 logical
 * arena; resizing letterboxes the whole arena instead of distorting sprites
 * or shifting target collision coordinates.
 */
public final class GameCanvas extends JComponent {
    public record Viewport(int x, int y, int width, int height, double scale) {}

    private final GameModel model;
    private final Image background = Assets.image("/Resource/General/background.png");
    private final Image[] planeImages = new Image[2];
    private final Image[] bombs = {
            Assets.image("/Resource/Bomb/Blue.png"),
            Assets.image("/Resource/Bomb/Orange.png")
    };
    private final Image[] targets = new Image[4];
    private final Font hud = Assets.font("/Resource/Font/Pixel.ttf", 26f);

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

            g.setFont(hud.deriveFont(23f));
            g.setColor(new Color(255, 169, 47));
            g.drawString("Orange: " + model.score(1), 24, 92);
            g.setFont(hud.deriveFont(18f));
            g.drawString("Strikes: " + model.canyonStrikes(1)
                    + "/" + GameRules.MAX_CANYON_STRIKES, 24, 122);
            g.setFont(hud.deriveFont(23f));
            g.setColor(new Color(98, 183, 255));
            g.drawString("Blue: " + model.score(0), 556, 92);
            g.setFont(hud.deriveFont(18f));
            g.drawString("Strikes: " + model.canyonStrikes(0)
                    + "/" + GameRules.MAX_CANYON_STRIKES, 556, 122);
            g.setFont(hud.deriveFont(26f));
            g.setColor(Color.WHITE);
            g.drawString("Time: " + ((model.timeRemainingMs() + 999) / 1000), 338, 130);

            if (model.state() == GameState.PAUSED) {
                g.setColor(new Color(0, 0, 0, 180));
                g.fillRect(80, 290, 640, 140);
                g.setColor(Color.WHITE);
                String message = "Paused - press P to resume";
                Font overlayFont = hud.deriveFont(26f);
                g.setFont(overlayFont);
                int advance = g.getFontMetrics().stringWidth(message);
                g.drawString(message, (GameRules.WIDTH - advance) / 2, 370);
            }
        } finally {
            g.dispose();
        }
    }
}
