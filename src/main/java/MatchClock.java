import java.util.Objects;
import java.util.function.LongSupplier;

/**
 * Monotonic, millisecond-accurate clock for an EDT-driven match.
 * A delayed/coalesced Swing Timer must not stretch a 60-second match.
 * Fractional milliseconds are retained; pause and restart reset the baseline.
 */
public final class MatchClock {
    private static final long NANOS_PER_MS = 1_000_000L;
    private final LongSupplier nanoTime;
    private long previousNanos;
    private long fractionalNanos;

    public MatchClock(LongSupplier nanoTime) {
        this.nanoTime = Objects.requireNonNull(nanoTime);
        reset();
    }

    public void reset() {
        previousNanos = nanoTime.getAsLong();
        fractionalNanos = 0;
    }

    /** Nonnegative elapsed real milliseconds since last poll, with no wall-clock use. */
    public int elapsedMilliseconds() {
        long now = nanoTime.getAsLong();
        long delta = now - previousNanos;
        previousNanos = now;
        if (delta < 0) {
            fractionalNanos = 0;
            return 0;
        }
        long millis = delta / NANOS_PER_MS;
        fractionalNanos += delta % NANOS_PER_MS;
        millis += fractionalNanos / NANOS_PER_MS;
        fractionalNanos %= NANOS_PER_MS;
        return (int) Math.min(millis, Integer.MAX_VALUE);
    }
}
