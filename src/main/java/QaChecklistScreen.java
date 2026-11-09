import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.function.Predicate;

/**
 * Manual QA helper for internal testers. Ticks are self-reported and transient:
 * no automatic collection, no file storage and no assertion of real playtests.
 */
public final class QaChecklistScreen extends JPanel {
    public static final String[] STEPS = {
        "Start a complete round with Blue (Enter) and Orange (Space)",
        "Verify both players can fire on the same keyboard",
        "Pause (P), open Rules/Settings, then explicitly resume",
        "Verify score, strikes, winner/tie and 60-second limit",
        "Replay the round, then return to the main menu",
        "Verify sound off, volume and reduced motion preferences",
        "Verify play without internet connection",
        "Clean install and uninstall on the actual target OS"
    };

    private final JCheckBox[] checks = new JCheckBox[STEPS.length];
    private final JLabel progress = new JLabel("0 of 8 checked — self-reported", SwingConstants.CENTER);
    private final JLabel status = new JLabel("Nothing is transmitted or saved.", SwingConstants.CENTER);
    private final JButton backButton = new JButton("Back");
    private final JButton copyButton = new JButton("Copy checklist");

    public QaChecklistScreen(Runnable onBack, Predicate<String> clipboard) {
        setLayout(new BorderLayout(10, 12));
        setBackground(new Color(12, 26, 43));
        setBorder(BorderFactory.createEmptyBorder(18, 24, 16, 24));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("INTERNAL QA CHECKLIST", SwingConstants.CENTER);
        title.setAlignmentX(CENTER_ALIGNMENT);
        title.setForeground(Color.WHITE);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 29));
        header.add(title);
        header.add(Box.createRigidArea(new Dimension(0, 6)));
        JLabel warning = new JLabel(
                "Self-reported steps are NOT verified human playtest evidence.",
                SwingConstants.CENTER);
        warning.setAlignmentX(CENTER_ALIGNMENT);
        warning.setForeground(new Color(255, 203, 123));
        warning.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
        header.add(warning);
        header.add(Box.createRigidArea(new Dimension(0, 6)));
        progress.setAlignmentX(CENTER_ALIGNMENT);
        progress.setForeground(new Color(125, 209, 251));
        progress.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        progress.getAccessibleContext().setAccessibleName("QA checklist progress");
        header.add(progress);
        add(header, BorderLayout.NORTH);

        JPanel entries = new JPanel();
        entries.setOpaque(false);
        entries.setLayout(new BoxLayout(entries, BoxLayout.Y_AXIS));
        for (int i = 0; i < STEPS.length; i++) {
            JCheckBox check = new JCheckBox((i + 1) + ". " + STEPS[i]);
            checks[i] = check;
            check.setForeground(Color.WHITE);
            check.setOpaque(false);
            check.setFocusPainted(true);
            check.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
            check.setAlignmentX(LEFT_ALIGNMENT);
            check.getAccessibleContext().setAccessibleDescription(
                    "Manually mark this scenario only after actually checking it");
            check.addActionListener(e -> updateProgress());
            entries.add(Box.createRigidArea(new Dimension(0, 7)));
            entries.add(check);
        }
        entries.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(entries);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        scroll.getAccessibleContext().setAccessibleName("QA testing scenarios");
        add(scroll, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout(4, 4));
        footer.setOpaque(false);
        status.setForeground(new Color(202, 224, 243));
        status.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        status.getAccessibleContext().setAccessibleName("Checklist clipboard status");
        footer.add(status, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttons.setOpaque(false);
        JButton reset = new JButton("Reset");
        reset.addActionListener(e -> {
            for (JCheckBox c : checks) c.setSelected(false);
            status.setText("Checklist cleared locally.");
            updateProgress();
        });
        copyButton.addActionListener(e -> {
            boolean success = clipboard.test(reportText());
            status.setText(success
                    ? "Checklist copied locally. Paste into a QA issue."
                    : "Clipboard unavailable. Nothing was sent.");
        });
        backButton.addActionListener(e -> onBack.run());
        buttons.add(reset);
        buttons.add(copyButton);
        buttons.add(backButton);
        footer.add(buttons, BorderLayout.SOUTH);
        add(footer, BorderLayout.SOUTH);

        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "go-back");
        getActionMap().put("go-back", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { onBack.run(); }
        });
        getAccessibleContext().setAccessibleName("AirRaid internal QA checklist");
    }

    private void updateProgress() {
        progress.setText(completedCount() + " of " + checks.length
                + " checked — self-reported");
    }

    public int completedCount() {
        int count = 0;
        for (JCheckBox c : checks) if (c.isSelected()) count++;
        return count;
    }

    public String reportText() {
        StringBuilder text = new StringBuilder("AirRaid " + AppVersion.NUMBER
                + " — INTERNAL QA CHECKLIST\n");
        text.append("SELF-REPORTED ONLY; NOT verified human playtest evidence.\n");
        for (int i = 0; i < checks.length; i++) {
            text.append(checks[i].isSelected() ? "[x] " : "[ ] ")
                    .append(STEPS[i]).append('\n');
        }
        text.append("Checked: ").append(completedCount())
                .append('/').append(checks.length).append('\n');
        text.append("No names, device IDs, timestamps or automatic transmission.\n");
        return text.toString();
    }

    public JCheckBox step(int index) { return checks[index]; }
    public JButton backButton() { return backButton; }
    public JButton copyButton() { return copyButton; }
    public String statusText() { return status.getText(); }
    public void focusFirstControl() { checks[0].requestFocusInWindow(); }
}
