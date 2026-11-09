import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

/** Start screen only; no game simulation is allocated until Start is clicked. */
public final class GameOpening extends JPanel {
    private final JButton startButton = new JButton("Start");

    public GameOpening(Runnable onStart) {
        setBackground(new Color(12, 26, 43));
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel heading = new JLabel("AIR RAID", SwingConstants.CENTER);
        heading.setForeground(Color.WHITE);
        heading.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 48));
        add(heading, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        JLabel gif = new JLabel(new ImageIcon(Assets.url("/Resource/General/FLY_.gif")));
        gif.setAlignmentX(CENTER_ALIGNMENT);
        gif.setHorizontalAlignment(SwingConstants.CENTER);
        center.add(gif);
        center.add(Box.createRigidArea(new Dimension(0, 20)));

        JLabel rules = new JLabel("<html><div style='text-align:center'>Blue: Enter &nbsp; | &nbsp; Orange: Space<br>"
                + "Targets: 1–4 points. A missed bomb counts as a canyon strike.<br>"
                + "Four strikes lose the round. Otherwise, the high score wins at 60 seconds.</div></html>",
                SwingConstants.CENTER);
        rules.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 17));
        rules.setForeground(Color.WHITE);
        rules.setAlignmentX(CENTER_ALIGNMENT);
        center.add(rules);
        add(center, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER));
        actions.setOpaque(false);
        startButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        startButton.addActionListener(event -> onStart.run());
        actions.add(startButton);
        add(actions, BorderLayout.SOUTH);
    }

    public JButton startButton() { return startButton; }
}
