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

/** The integrated home card: Start, Rules and Exit are immediately visible. */
public final class GameOpening extends JPanel {
    private final JButton startButton = new JButton("Start");

    public GameOpening(Runnable onStart, Runnable onRules, Runnable onExit) {
        setLayout(new BorderLayout(12, 12));
        setBackground(new Color(12, 26, 43));
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JLabel heading = new JLabel("AIR RAID", SwingConstants.CENTER);
        heading.setForeground(Color.WHITE);
        heading.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 42));
        add(heading, BorderLayout.NORTH);

        JLabel animation = new JLabel(new ImageIcon(Assets.url("/Resource/General/FLY_.gif")));
        animation.setHorizontalAlignment(SwingConstants.CENTER);
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
        startButton.getAccessibleContext().setAccessibleDescription("Begin a new two-player match");
        JButton rules = new JButton("Rules");
        rules.addActionListener(event -> onRules.run());
        JButton exit = new JButton("Exit");
        exit.addActionListener(event -> onExit.run());
        actions.add(startButton);
        actions.add(rules);
        actions.add(exit);
        add(actions, BorderLayout.SOUTH);

        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "start");
        getActionMap().put("start", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent event) { onStart.run(); }
        });
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_H, 0), "rules");
        getActionMap().put("rules", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent event) { onRules.run(); }
        });
        getAccessibleContext().setAccessibleName("AirRaid main menu");
    }

    public JButton startButton() { return startButton; }
    public void focusStartButton() { startButton.requestFocusInWindow(); }
}
