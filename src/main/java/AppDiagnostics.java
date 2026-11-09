import java.awt.GraphicsEnvironment;
import javax.swing.JOptionPane;

/** Privacy-safe, structured startup diagnostics (no path or exception payloads). */
public final class AppDiagnostics {
    public enum FailureCode { STARTUP, RESOURCES, SETTINGS }

    private AppDiagnostics() {}

    /** Stable one-line diagnostic identifier, without paths or personal data. */
    public static String diagnostic(FailureCode code) {
        if (code == null) throw new IllegalArgumentException("Failure code required");
        return "AIRRAID_ERROR code=" + code.name();
    }

    public static String userMessage(FailureCode code) {
        return switch (code) {
            case STARTUP -> "AirRaid could not start. Check Java 21 and the game files.";
            case RESOURCES -> "One or more game resources are missing or unreadable.";
            case SETTINGS -> "Settings are unavailable. Defaults will be used.";
        };
    }

    /** Never prints exceptions or stack traces: their messages can contain home paths. */
    public static void report(FailureCode code) {
        System.err.println(diagnostic(code));
        System.err.println(userMessage(code));
    }

    public static void reportStartup() {
        report(FailureCode.STARTUP);
        if (!GraphicsEnvironment.isHeadless()) {
            try {
                JOptionPane.showMessageDialog(null, userMessage(FailureCode.STARTUP),
                        "AirRaid startup error", JOptionPane.ERROR_MESSAGE);
            } catch (RuntimeException ignored) {
                // A failure displaying a dialog must not leak diagnostic details.
            }
        }
    }
}
