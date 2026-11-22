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
import java.awt.GridLayout;
import java.awt.Font;
import java.util.function.Predicate;

/** Dedicated, keyboard-focusable results view for completed rounds. */
public final class ResultsScreen extends JPanel {
    private final JLabel outcome = new JLabel("", SwingConstants.CENTER);
    private final JLabel summary = new JLabel("", SwingConstants.CENTER);
    private final JLabel sessionSummary = new JLabel("ROUND STATISTICS", SwingConstants.CENTER);
    private final JLabel bluePerformance = new JLabel("", SwingConstants.CENTER);
    private final JLabel orangePerformance = new JLabel("", SwingConstants.CENTER);
    private final JButton replay = new JButton("Play again");
    private final JButton copyReport = new JButton("Copy QA report");
    private final JLabel reportStatus = new JLabel("No report is sent automatically.",
            SwingConstants.CENTER);
    private String reportText;

    public ResultsScreen(Runnable onReplay, Runnable onMenu,
                         Predicate<String> onCopyReport) {
        setBackground(new Color(12, 26, 43));
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 18, 12, 18));

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("ROUND COMPLETE", SwingConstants.CENTER);
        title.setForeground(new Color(103, 191, 255));
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 30));
        title.setAlignmentX(CENTER_ALIGNMENT);
        center.add(title);
        center.add(Box.createRigidArea(new Dimension(0, 14)));

        outcome.setForeground(Color.WHITE);
        outcome.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        outcome.setAlignmentX(CENTER_ALIGNMENT);
        center.add(outcome);
        center.add(Box.createRigidArea(new Dimension(0, 10)));

        summary.setForeground(new Color(208, 220, 235));
        summary.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 17));
        summary.setAlignmentX(CENTER_ALIGNMENT);
        center.add(summary);
        center.add(Box.createRigidArea(new Dimension(0, 8)));

        sessionSummary.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        sessionSummary.setForeground(new Color(173, 206, 233));
        sessionSummary.setAlignmentX(CENTER_ALIGNMENT);
        sessionSummary.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        sessionSummary.getAccessibleContext().setAccessibleName(
                "Completed-round session wins, losses and draws");
        center.add(sessionSummary);
        center.add(Box.createRigidArea(new Dimension(0, 5)));

        JPanel stats = new JPanel(new GridLayout(1, 2, 12, 0));
        stats.setOpaque(false);
        stats.setAlignmentX(CENTER_ALIGNMENT);
        stats.setPreferredSize(new Dimension(550, 82));
        stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 84));
        stats.add(statCard(bluePerformance, new Color(25, 61, 93),
                "Blue player shot and hit statistics"));
        stats.add(statCard(orangePerformance, new Color(94, 53, 31),
                "Orange player shot and hit statistics"));
        center.add(stats);
        center.add(Box.createRigidArea(new Dimension(0, 10)));

        reportStatus.setForeground(new Color(190, 218, 238));
        reportStatus.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        reportStatus.setAlignmentX(CENTER_ALIGNMENT);
        // The status changes after a user copies a report. Give BoxLayout
        // enough horizontal space for the longer confirmation message.
        reportStatus.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
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
            reportStatus.revalidate();
            reportStatus.repaint();
        });
        controls.add(replay);
        controls.add(copyReport);
        controls.add(menu);
        add(controls, BorderLayout.SOUTH);
        getAccessibleContext().setAccessibleName("AirRaid round results");
    }

    private static JPanel statCard(JLabel label, Color background,
                                   String accessibleName) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(background);
        card.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        label.setForeground(Color.WHITE);
        label.getAccessibleContext().setAccessibleName(accessibleName);
        card.add(label, BorderLayout.CENTER);
        return card;
    }

    public void showResult(GameModel model, UserPreferences preferences) {
        showResult(model, preferences, null);
    }

    /**
     * Show the completed match plus optional session-only performance summary.
     * Old callers still render the per-round statistics without a session.
     */
    public void showResult(GameModel model, UserPreferences preferences,
                           RoundHistory.Summary session) {
        reportText = BetaRoundReport.format(model, preferences);
        reportStatus.setText("Hit rate = hits / shots. QA report stays local.");
        if (session == null) {
            sessionSummary.setText("ROUND STATISTICS");
            sessionSummary.setToolTipText(null);
        } else {
            sessionSummary.setText(session.compactLabel());
            sessionSummary.setToolTipText(session.bestScoreLabel());
            sessionSummary.getAccessibleContext().setAccessibleDescription(
                    session.bestScoreLabel());
        }
        outcome.setText(model.resultText());
        summary.setText("<html><div style='text-align:center'>Blue "
                + model.score(0) + " pts / " + model.canyonStrikes(0) + " strikes"
                + "<br>Orange " + model.score(1) + " pts / "
                + model.canyonStrikes(1) + " strikes</div></html>");
        bluePerformance.setText(statText("BLUE", model, 0));
        orangePerformance.setText(statText("ORANGE", model, 1));
    }

    private static String statText(String name, GameModel model, int player) {
        return "<html><div style='text-align:center'><b>" + name + "</b>"
                + "<br>Shots " + model.shotsLaunched(player)
                + "  /  Hits " + model.targetsHit(player)
                + "<br>Hit rate " + model.hitRatePercent(player)
                + "%</div></html>";
    }

    public void focusReplay() { replay.requestFocusInWindow(); }
    public String outcomeText() { return outcome.getText(); }
    public String reportText() { return reportText; }
    public String reportStatusText() { return reportStatus.getText(); }
    public String sessionSummaryText() { return sessionSummary.getText(); }
    public JButton reportButton() { return copyReport; }
    public String playerStatText(int player) {
        if (player == 0) return bluePerformance.getText();
        if (player == 1) return orangePerformance.getText();
        throw new IllegalArgumentException("player index must be 0 or 1");
    }
}
