/**
 * Canonical AirRaid v0.3.0 gameplay contract.
 *
 * A "canyon strike" is a launched bomb exiting the world boundary without
 * destroying a still-intact target. Four strikes by one player cause an
 * immediate loss; simultaneous fourth strikes are a draw. A strike-limit
 * result takes precedence over the timer on the same simulation step.
 */
public final class GameRules {
    public static final int WIDTH = 800;
    public static final int HEIGHT = 800;
    public static final int MATCH_DURATION_MS = 60_000;
    public static final int MAX_CANYON_STRIKES = 4;
    public static final int SIMULATION_STEP_MS = 16;
    public static final int TARGET_ROWS = 8;
    public static final int TARGET_COLUMNS = 38;

    private GameRules() { }

    /**
     * Swept axis-aligned box versus a target, using the moving bomb's top-left
     * point against the target expanded by its dimensions (Minkowski sum).
     * Returns earliest intersection fraction in [0,1], or +infinity if none.
     */
    static double contactTime(double x0, double y0, double x1, double y1,
                              int bombSize, Target target) {
        double minX = target.x() - bombSize;
        double maxX = target.x() + target.width();
        double minY = target.y() - bombSize;
        double maxY = target.y() + target.height();
        double dx = x1 - x0;
        double dy = y1 - y0;
        double enter = 0.0;
        double leave = 1.0;
        if (dx == 0.0) {
            if (x0 < minX || x0 > maxX) return Double.POSITIVE_INFINITY;
        } else {
            double a = (minX - x0) / dx;
            double b = (maxX - x0) / dx;
            enter = Math.max(enter, Math.min(a, b));
            leave = Math.min(leave, Math.max(a, b));
        }
        if (dy == 0.0) {
            if (y0 < minY || y0 > maxY) return Double.POSITIVE_INFINITY;
        } else {
            double a = (minY - y0) / dy;
            double b = (maxY - y0) / dy;
            enter = Math.max(enter, Math.min(a, b));
            leave = Math.min(leave, Math.max(a, b));
        }
        return enter <= leave && leave >= 0.0 && enter <= 1.0
                ? Math.max(0.0, enter)
                : Double.POSITIVE_INFINITY;
    }

    /**
     * Earliest boundary crossing for a travelling bomb in this substep.
     * The bomb misses after fully leaving a horizontal edge or crossing the
     * lower edge. The top edge is not a miss boundary (bombs travel down).
     */
    static double exitTime(double x0, double y0, double x1, double y1, int bombSize) {
        double exit = Double.POSITIVE_INFINITY;
        if (y1 >= HEIGHT && y1 > y0) {
            exit = Math.min(exit, Math.max(0.0, (HEIGHT - y0) / (y1 - y0)));
        }
        if (x1 >= WIDTH && x1 > x0) {
            exit = Math.min(exit, Math.max(0.0, (WIDTH - x0) / (x1 - x0)));
        }
        if (x1 + bombSize <= 0 && x1 < x0) {
            exit = Math.min(exit, Math.max(0.0, (-bombSize - x0) / (x1 - x0)));
        }
        return exit;
    }
}
