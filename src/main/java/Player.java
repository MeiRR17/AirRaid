import java.awt.geom.Rectangle2D;

/** Mutable plane position, owned exclusively by GameModel (no worker threads). */
public final class Player {
    private static final double SPEED_PX_PER_SECOND = 100.0;
    private final int width;
    private final int height;
    private final int direction;
    private double x;
    private final double y;

    public Player(double x, double y, int width, int height, int direction) {
        if (direction != -1 && direction != 1) {
            throw new IllegalArgumentException("direction must be -1 or +1");
        }
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.direction = direction;
    }

    void advance(double seconds, int worldWidth) {
        x += direction * SPEED_PX_PER_SECOND * seconds;
        if (direction > 0 && x > worldWidth) {
            x = -width;
        } else if (direction < 0 && x + width < 0) {
            x = worldWidth;
        }
    }

    public double x() { return x; }
    public double y() { return y; }
    public int width() { return width; }
    public int height() { return height; }
    public int direction() { return direction; }

    public Rectangle2D bounds() {
        return new Rectangle2D.Double(x, y, width, height);
    }
}
