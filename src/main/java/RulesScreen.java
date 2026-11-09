import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.KeyStroke;
import javax.swing.JComponent;
import javax.swing.AbstractAction;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/** Integrated, keyboard-accessible rules card rather than a detached dialog. */
public final class RulesScreen extends JPanel {
    private final JButton backButton = new JButton("Back");

    public RulesScreen(Runnable onBack) {
        setLayout(new BorderLayout(12, 12));
        setBackground(new Color(13, 27, 46));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel heading = new JLabel("HOW TO PLAY", SwingConstants.CENTER);
        heading.setForeground(Color.WHITE);
        heading.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 32));
        add(heading, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        String[] paragraphs = {
                "TWO PLAYERS, ONE KEYBOARD",
                "Blue plane: ENTER to drop a bomb. Orange plane: SPACE to drop a bomb.",
                "Planes move automatically. Each player may have only one active bomb.",
                "POINTS",
                "Targets award 1, 2, 3 or 4 points. A destroyed target cannot score again.",
                "CANYON STRIKES",
                "A bomb leaving the arena without a target hit adds one strike.",
                "Four strikes lose the round. Simultaneous fourth strikes are a draw.",
                "TIME LIMIT",
                "If nobody reaches four strikes, the higher score wins at 60 seconds.",
                "Equal scores after 60 seconds are a tie.",
                "GAME CONTROLS",
                "P: Pause / resume. R: Restart the current match (once per press).",
                "Holding P, R or M never repeats the toggle/reset. Release to use again.",
                "H: Rules (pauses the match). S: Settings (also pauses).",
                "M: Sound on/off. Sound begins off by default. Volume and motion can be changed in Settings.",
                "ESC: Return to main menu during play; Back returns from Rules or Settings.",
                "Returning from Rules or Settings keeps the match paused until you press P."
        };
        for (int i = 0; i < paragraphs.length; i++) {
            boolean headingText = paragraphs[i].equals(paragraphs[i].toUpperCase())
                    && !paragraphs[i].contains(":");
            JLabel label = new JLabel("<html><div style='width: 520px'>"
                    + paragraphs[i] + "</div></html>");
            label.setFont(new Font(Font.SANS_SERIF,
                    headingText ? Font.BOLD : Font.PLAIN, headingText ? 18 : 16));
            label.setForeground(headingText ? new Color(103, 191, 255) : Color.WHITE);
            label.setAlignmentX(LEFT_ALIGNMENT);
            body.add(label);
            body.add(Box.createRigidArea(new Dimension(0, headingText ? 14 : 8)));
        }

        JScrollPane scroller = new JScrollPane(body);
        scroller.setBorder(null);
        scroller.getViewport().setBackground(getBackground());
        scroller.getVerticalScrollBar().setUnitIncrement(18);
        add(scroller, BorderLayout.CENTER);

        backButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        backButton.addActionListener(event -> onBack.run());
        backButton.getAccessibleContext().setAccessibleDescription(
                "Return to the game or main menu");
        JPanel controls = new JPanel();
        controls.setOpaque(false);
        controls.add(backButton);
        add(controls, BorderLayout.SOUTH);

        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "back");
        getActionMap().put("back", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent event) { onBack.run(); }
        });
        getAccessibleContext().setAccessibleName("AirRaid rules screen");
    }

    public JButton backButton() { return backButton; }

    public void focusBackButton() {
        backButton.requestFocusInWindow();
    }
}
