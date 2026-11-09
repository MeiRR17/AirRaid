import javax.swing.SwingUtilities;

/** Desktop entry point: all UI creation is confined to the Swing EDT. */
public final class Main {
    private Main() {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Window().showWindow());
    }
}
