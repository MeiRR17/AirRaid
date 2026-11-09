import java.awt.geom.Rectangle2D;

/** One fixed target. A successful hit can award points only once. */
public final class Target {
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final int value;
    private boolean destroyed;

    public Target(int x, int y, int width, int height, int value) {
        if (value < 1 || value > 4) {
            throw new IllegalArgumentException("Target value must be 1–4");
        }
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.value = value;
    }

    /** Returns the awarded points, or zero after a previous hit. */
    int hit() {
        if (destroyed) {
            return 0;
        }
        destroyed = true;
        return value;
    }

    public Rectangle2D bounds() { return new Rectangle2D.Double(x, y, width, height); }
    public int x() { return x; }
    public int y() { return y; }
    public int width() { return width; }
    public int height() { return height; }
    public int value() { return value; }
    public boolean isDestroyed() { return destroyed; }
}
