import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.function.Predicate;

/** Dedicated, keyboard-focusable results view for completed rounds. */
public final class ResultsScreen extends JPanel {
    private final JLabel outcome = new JLabel("", SwingConstants.CENTER);
    private final JLabel summary = new JLabel("", SwingConstants.CENTER);
    private final JButton replay = new JButton("Play again");
    private final JButton copyReport = new JButton("Copy QA report");
    private final JLabel reportStatus = new JLabel("No report is sent automatically.",
            SwingConstants.CENTER);
    private String reportText;

    public ResultsScreen(Runnable onReplay, Runnable onMenu,
                         Predicate<String> onCopyReport) {
        setBackground(new Color(12, 26, 43));
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(32, 24, 32, 24));

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("ROUND COMPLETE", SwingConstants.CENTER);
        title.setForeground(new Color(103, 191, 255));
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 32));
        title.setAlignmentX(CENTER_ALIGNMENT);
        center.add(title);
        center.add(Box.createRigidArea(new Dimension(0, 28)));

        outcome.setForeground(Color.WHITE);
        outcome.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        outcome.setAlignmentX(CENTER_ALIGNMENT);
        center.add(outcome);
        center.add(Box.createRigidArea(new Dimension(0, 18)));

        summary.setForeground(new Color(208, 220, 235));
        summary.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 17));
        summary.setAlignmentX(CENTER_ALIGNMENT);
        center.add(summary);
        center.add(Box.createRigidArea(new Dimension(0, 24)));
        reportStatus.setForeground(new Color(190, 218, 238));
        reportStatus.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        reportStatus.setAlignmentX(CENTER_ALIGNMENT);
        reportStatus.getAccessibleContext().setAccessibleName("QA report status");
        center.add(reportStatus);
        add(center, BorderLayout.CENTER);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 6));
        controls.setOpaque(false);
        replay.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        replay.addActionListener(event -> onReplay.run());
        replay.getAccessibleContext().setAccessibleDescription("Start a fresh match");
        JButton menu = new JButton("Main menu");
        menu.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 18));
        menu.addActionListener(event -> onMenu.run());
        copyReport.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 17));
        copyReport.setToolTipText("Copy a privacy-minimal offline result. Never uploaded.");
        copyReport.getAccessibleContext().setAccessibleDescription(
                "Copy a local round summary for a beta bug report");
        copyReport.addActionListener(event -> {
            if (reportText == null) return;
            boolean copied = onCopyReport.test(reportText);
            reportStatus.setText(copied
                    ? "Report copied. Paste it into a beta bug report."
                    : "Clipboard unavailable. No report was transmitted.");
        });
        controls.add(replay);
        controls.add(copyReport);
        controls.add(menu);
        add(controls, BorderLayout.SOUTH);
        getAccessibleContext().setAccessibleName("AirRaid round results");
    }

    public void showResult(GameModel model, UserPreferences preferences) {
        reportText = BetaRoundReport.format(model, preferences);
        reportStatus.setText("QA summary is local. No report is transmitted.");
        outcome.setText(model.resultText());
        summary.setText("<html><div style='text-align:center'>Blue "
                + model.score(0) + " pts / " + model.canyonStrikes(0) + " strikes"
                + "<br>Orange " + model.score(1) + " pts / "
                + model.canyonStrikes(1) + " strikes</div></html>");
    }

    public void focusReplay() { replay.requestFocusInWindow(); }
    public String outcomeText() { return outcome.getText(); }
    public String reportText() { return reportText; }
    public String reportStatusText() { return reportStatus.getText(); }
    public JButton reportButton() { return copyReport; }
}
