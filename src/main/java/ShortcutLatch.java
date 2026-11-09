import java.util.HashSet;
import java.util.Set;

/**
 * A single action for each physical key press until its key-release event.
 * Swing receives auto-repeat KEY_PRESSED events while keys are held; this
 * latch prevents repeated Pause / Restart / Mute actions.
 *
 * All access is on the Swing EDT. It does not capture, log or transmit keys.
 */
public final class ShortcutLatch {
    private final Set<Integer> heldKeys = new HashSet<>();

    /** Returns true only on the first key-down of a press/release cycle. */
    public boolean press(int keyCode) {
        return heldKeys.add(keyCode);
    }

    public void release(int keyCode) {
        heldKeys.remove(keyCode);
    }

    /** Use after focus loss or returning from another screen. */
    public void clear() {
        heldKeys.clear();
    }

    public boolean isHeld(int keyCode) {
        return heldKeys.contains(keyCode);
    }
}
