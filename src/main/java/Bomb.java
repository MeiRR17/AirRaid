import java.awt.geom.Rectangle2D;

/** One active projectile per team; state is exclusively owned by GameModel. */
public final class Bomb {
    private static final double FALL_SPEED_PX_PER_SECOND = 260.0;
    private static final double SIDE_SPEED_PX_PER_SECOND = 160.0;
    public static final int SIZE = 12;
    private final int direction;
    private boolean active;
    private double x;
    private double y;

    public Bomb(int direction) {
        if (direction != -1 && direction != 1) {
            throw new IllegalArgumentException("direction must be -1 or +1");
        }
        this.direction = direction;
    }

    void attach(Player plane) {
        if (active) return;
        x = direction > 0 ? plane.x() + plane.width() : plane.x() - SIZE;
        y = plane.y() + plane.height() / 2.0;
    }

    boolean launch(Player plane) {
        if (active) return false;
        attach(plane);
        active = true;
        return true;
    }

    void advance(double seconds) {
        if (!active) return;
        x += direction * SIDE_SPEED_PX_PER_SECOND * seconds;
        y += FALL_SPEED_PX_PER_SECOND * seconds;
    }

    void reset(Player plane) {
        active = false;
        attach(plane);
    }

    public Rectangle2D bounds() {
        return new Rectangle2D.Double(x, y, SIZE, SIZE);
    }

    public double x() { return x; }
    public double y() { return y; }
    public boolean isActive() { return active; }
    public int size() { return SIZE; }
}
