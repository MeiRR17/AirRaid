import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
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
import java.io.IOException;

/** Home menu: Start, Rules, Settings, Exit. Reduced motion uses a static GIF frame. */
public final class GameOpening extends JPanel {
    private final JButton startButton = new JButton("Start");
    private final JLabel animation = new JLabel();
    private final ImageIcon animatedIcon =
            new ImageIcon(Assets.url("/Resource/General/FLY_.gif"));
    private ImageIcon stillIcon;

    public GameOpening(Runnable onStart, Runnable onRules,
                       Runnable onSettings, Runnable onChecklist, Runnable onExit) {
        setLayout(new BorderLayout(12, 12));
        setBackground(new Color(12, 26, 43));
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JLabel heading = new JLabel("AIR RAID", SwingConstants.CENTER);
        heading.setForeground(Color.WHITE);
        heading.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 42));
        JPanel titles = new JPanel(new BorderLayout());
        titles.setOpaque(false);
        titles.add(heading, BorderLayout.CENTER);
        JLabel betaLabel = new JLabel(AppVersion.MENU_LABEL,
                SwingConstants.CENTER);
        betaLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        betaLabel.setForeground(new Color(190, 216, 237));
        betaLabel.getAccessibleContext().setAccessibleName(
                "Beta version " + AppVersion.NUMBER + ", internal playtest build");
        titles.add(betaLabel, BorderLayout.SOUTH);
        add(titles, BorderLayout.NORTH);

        animation.setIcon(animatedIcon);
        animation.setHorizontalAlignment(SwingConstants.CENTER);
        animation.setForeground(Color.WHITE);
        JScrollPane scroller = new JScrollPane(animation);
        scroller.setBorder(null);
        scroller.setPreferredSize(new Dimension(650, 320));
        scroller.getViewport().setBackground(getBackground());
        scroller.getVerticalScrollBar().setUnitIncrement(18);

        JPanel middle = new JPanel(new BorderLayout(12, 12));
        middle.setOpaque(false);
        middle.add(scroller, BorderLayout.CENTER);
        JLabel summary = new JLabel("<html><div style='text-align:center'>"
                + "Blue: Enter &nbsp;&nbsp; Orange: Space &nbsp;&nbsp; P: Pause &nbsp;&nbsp; R: Restart"
                + "<br>Collect 1–4 points per target. Four missed shots lose the round."
                + "<br>Otherwise, the higher score wins after 60 seconds."
                + "</div></html>", SwingConstants.CENTER);
        summary.setForeground(Color.WHITE);
        summary.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        middle.add(summary, BorderLayout.SOUTH);
        add(middle, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 6));
        actions.setOpaque(false);
        startButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        startButton.addActionListener(event -> onStart.run());
        startButton.getAccessibleContext().setAccessibleDescription(
                "Begin a new two-player match");
        JButton rules = new JButton("Rules");
        rules.addActionListener(event -> onRules.run());
        JButton settings = new JButton("Settings");
        settings.addActionListener(event -> onSettings.run());
        JButton checklist = new JButton("QA checklist");
        checklist.setToolTipText("Manually track offline beta test steps (Q)");
        checklist.addActionListener(event -> onChecklist.run());
        JButton exit = new JButton("Exit");
        exit.addActionListener(event -> onExit.run());
        actions.add(startButton);
        actions.add(rules);
        actions.add(settings);
        actions.add(checklist);
        actions.add(exit);
        add(actions, BorderLayout.SOUTH);

        bind(KeyEvent.VK_ENTER, "start", onStart);
        bind(KeyEvent.VK_H, "rules", onRules);
        bind(KeyEvent.VK_S, "settings", onSettings);
        bind(KeyEvent.VK_Q, "qa-checklist", onChecklist);
        getAccessibleContext().setAccessibleName("AirRaid main menu");
    }

    private void bind(int key, String name, Runnable action) {
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(key, 0), name);
        getActionMap().put(name, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent event) { action.run(); }
        });
    }

    public void setReducedMotion(boolean reduced) {
        if (reduced) {
            if (stillIcon == null) {
                try {
                    var firstFrame = ImageIO.read(Assets.url("/Resource/General/FLY_.gif"));
                    if (firstFrame != null) stillIcon = new ImageIcon(firstFrame);
                } catch (IOException ignored) {
                    // If a GIF decoder is unavailable, show static text instead.
                }
            }
            animation.setIcon(stillIcon);
            animation.setText(stillIcon == null ? "Reduced motion enabled" : "");
        } else {
            animation.setIcon(animatedIcon);
            animation.setText("");
        }
    }

    public boolean reducedMotionShown() { return animation.getIcon() != animatedIcon; }
    public JButton startButton() { return startButton; }
    public void focusStartButton() { startButton.requestFocusInWindow(); }
}
