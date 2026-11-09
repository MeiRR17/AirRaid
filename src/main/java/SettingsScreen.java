import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;

/**
 * Keyboard-operable, in-app local settings. It never writes to disk itself:
 * Window coordinates persistence and propagates snapshots to gameplay.
 */
public final class SettingsScreen extends JPanel {
    private final JCheckBox soundToggle = new JCheckBox("Enable game sounds");
    private final JCheckBox reducedMotion = new JCheckBox("Reduce animation and visual effects");
    private final JSlider volumeSlider = new JSlider(0, 100, UserPreferences.DEFAULT_VOLUME);
    private final JLabel volumeLabel = new JLabel();
    private final JLabel warning = new JLabel(" ", SwingConstants.CENTER);
    private final JButton backButton = new JButton("Back");
    private UserPreferences current = UserPreferences.defaults();
    private boolean updating;

    public SettingsScreen(Consumer<UserPreferences> onUpdate, Runnable onBack) {
        setLayout(new BorderLayout(16, 16));
        setBackground(new Color(12, 26, 43));
        setBorder(BorderFactory.createEmptyBorder(24, 30, 24, 30));

        JLabel title = new JLabel("SETTINGS", SwingConstants.CENTER);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 34));
        title.setForeground(Color.WHITE);
        add(title, BorderLayout.NORTH);

        JPanel controls = new JPanel();
        controls.setOpaque(false);
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        for (JCheckBox box : new JCheckBox[] {soundToggle, reducedMotion}) {
            box.setOpaque(false);
            box.setForeground(Color.WHITE);
            box.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 18));
            box.setAlignmentX(LEFT_ALIGNMENT);
            box.setFocusPainted(true);
            controls.add(box);
            controls.add(Box.createRigidArea(new Dimension(0, 22)));
        }

        volumeLabel.setForeground(Color.WHITE);
        volumeLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        volumeLabel.setAlignmentX(LEFT_ALIGNMENT);
        controls.add(volumeLabel);
        controls.add(Box.createRigidArea(new Dimension(0, 8)));

        volumeSlider.setOpaque(false);
        volumeSlider.setMajorTickSpacing(25);
        volumeSlider.setPaintTicks(true);
        volumeSlider.setPaintLabels(true);
        volumeSlider.setForeground(Color.WHITE);
        volumeSlider.getAccessibleContext().setAccessibleName("Sound volume, 0 to 100 percent");
        volumeSlider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        volumeSlider.setAlignmentX(LEFT_ALIGNMENT);
        controls.add(volumeSlider);
        controls.add(Box.createRigidArea(new Dimension(0, 30)));

        JLabel keyHelp = new JLabel("<html><div style='width: 420px'>"
                + "KEYBOARD HELP: Tab / Shift+Tab moves between controls. "
                + "Space toggles the focused option. Arrow keys adjust volume. "
                + "Escape returns to the previous screen."
                + "<br><br>During play: Enter = Blue fire; Space = Orange fire; "
                + "P = Pause; R = Restart; H = Rules; S = Settings; M = Sound; "
                + "Escape = Menu.</div></html>");
        keyHelp.setForeground(new Color(215, 228, 242));
        keyHelp.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        keyHelp.setAlignmentX(LEFT_ALIGNMENT);
        controls.add(keyHelp);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(controls, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(10, 12));
        bottom.setOpaque(false);
        warning.setForeground(new Color(255, 205, 125));
        warning.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        warning.getAccessibleContext().setAccessibleName("Settings status");
        bottom.add(warning, BorderLayout.NORTH);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        actions.setOpaque(false);
        JButton reset = new JButton("Restore defaults");
        reset.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 17));
        backButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
        reset.addActionListener(e -> onUpdate.accept(UserPreferences.defaults()));
        backButton.addActionListener(e -> onBack.run());
        actions.add(reset);
        actions.add(backButton);
        bottom.add(actions, BorderLayout.SOUTH);
        add(bottom, BorderLayout.SOUTH);

        soundToggle.addActionListener(e -> {
            if (!updating) onUpdate.accept(current.withSound(soundToggle.isSelected()));
        });
        reducedMotion.addActionListener(e -> {
            if (!updating) onUpdate.accept(current.withReducedMotion(reducedMotion.isSelected()));
        });
        volumeSlider.addChangeListener(e -> {
            volumeLabel.setText("Sound volume: " + volumeSlider.getValue() + "%");
            if (!updating && !volumeSlider.getValueIsAdjusting()) {
                onUpdate.accept(current.withVolume(volumeSlider.getValue()));
            }
        });
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "back");
        getActionMap().put("back", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent event) { onBack.run(); }
        });
        getAccessibleContext().setAccessibleName("AirRaid settings screen");
        setPreferences(current);
    }

    public void setPreferences(UserPreferences next) {
        updating = true;
        try {
            current = next;
            soundToggle.setSelected(next.soundEnabled());
            reducedMotion.setSelected(next.reducedMotion());
            volumeSlider.setValue(next.volumePercent());
            volumeLabel.setText("Sound volume: " + next.volumePercent() + "%");
        } finally {
            updating = false;
        }
    }

    public void showWarning(String text) {
        warning.setText(text == null ? "Preferences are saved locally on this device." : text);
        warning.setToolTipText(text);
    }

    public void focusFirstControl() { soundToggle.requestFocusInWindow(); }
    public JButton backButton() { return backButton; }
    public JCheckBox soundToggle() { return soundToggle; }
    public JCheckBox reducedMotionToggle() { return reducedMotion; }
    public JSlider volumeSlider() { return volumeSlider; }
    public String statusText() { return warning.getText(); }
}
