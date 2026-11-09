import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/** Owns one game screen at a time and disposes the previous match on navigation. */
public final class Window extends JFrame {
    private static final String MENU = "menu";
    private static final String GAME = "game";
    private final CardLayout navigation = new CardLayout();
    private final JPanel screens = new JPanel(navigation);
    private Gameplay gameplay;

    public Window() {
        super("Air Raid");
        setIconImage(Assets.image("/Resource/Window/gameIcon.jpg"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        screens.add(new GameOpening(this::startGame), MENU);
        setContentPane(screens);
        setPreferredSize(new Dimension(800, 800));
        pack();
        setResizable(false);
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent event) {
                if (gameplay != null) {
                    gameplay.disposeGame();
                }
            }
        });

        addWindowFocusListener(new WindowAdapter() {
            @Override public void windowLostFocus(WindowEvent event) {
                if (gameplay != null) {
                    gameplay.pauseGame();
                }
            }
        });
    }

    public void startGame() {
        if (gameplay != null) {
            gameplay.disposeGame();
            screens.remove(gameplay);
        }
        gameplay = new Gameplay(this::returnToMenu);
        screens.add(gameplay, GAME);
        navigation.show(screens, GAME);
        screens.revalidate();
        gameplay.startGame();
        gameplay.requestFocusInWindow();
    }

    public void returnToMenu() {
        if (gameplay != null) {
            gameplay.disposeGame();
            screens.remove(gameplay);
            gameplay = null;
        }
        navigation.show(screens, MENU);
        screens.revalidate();
        screens.repaint();
    }

    public Gameplay activeGame() { return gameplay; }

    public void showWindow() {
        setVisible(true);
    }
}
