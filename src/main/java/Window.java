import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * One resizable application frame, with integrated menu, rules and game cards.
 * Returning from in-game rules leaves that round paused, never silently resumed.
 */
public final class Window extends JFrame {
    private static final String MENU = "menu";
    private static final String GAME = "game";
    private static final String RULES = "rules";

    private final CardLayout navigation = new CardLayout();
    private final JPanel screens = new JPanel(navigation);
    private final GameOpening opening;
    private final RulesScreen rules;
    private Gameplay gameplay;
    private String activeCard = MENU;
    private boolean rulesReturnToGame;

    public Window() {
        super("Air Raid");
        setIconImage(Assets.image("/Resource/Window/gameIcon.jpg"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        opening = new GameOpening(this::startGame, this::showRules, this::exitGame);
        rules = new RulesScreen(this::returnFromRules);
        screens.add(opening, MENU);
        screens.add(rules, RULES);
        setContentPane(screens);
        setPreferredSize(new Dimension(960, 800));
        setMinimumSize(new Dimension(620, 480));
        pack();
        setResizable(true);
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent event) {
                if (gameplay != null) gameplay.disposeGame();
            }
        });
        addWindowFocusListener(new WindowAdapter() {
            @Override public void windowLostFocus(WindowEvent event) {
                if (gameplay != null && GAME.equals(activeCard)) gameplay.pauseGame();
            }
        });
    }

    private void showCard(String card) {
        activeCard = card;
        navigation.show(screens, card);
        screens.revalidate();
        screens.repaint();
    }

    public void startGame() {
        if (gameplay != null) {
            gameplay.disposeGame();
            screens.remove(gameplay);
        }
        gameplay = new Gameplay(this::returnToMenu, this::showRules);
        screens.add(gameplay, GAME);
        showCard(GAME);
        gameplay.startGame();
        gameplay.focusGame();
    }

    public void showRules() {
        rulesReturnToGame = GAME.equals(activeCard) && gameplay != null;
        if (rulesReturnToGame) gameplay.pauseGame();
        showCard(RULES);
        rules.focusBackButton();
    }

    public void returnFromRules() {
        if (rulesReturnToGame && gameplay != null) {
            showCard(GAME);
            gameplay.focusGame();
        } else {
            showCard(MENU);
            opening.focusStartButton();
        }
        rulesReturnToGame = false;
    }

    public void returnToMenu() {
        if (gameplay != null) {
            gameplay.disposeGame();
            screens.remove(gameplay);
            gameplay = null;
        }
        rulesReturnToGame = false;
        showCard(MENU);
        opening.focusStartButton();
    }

    public void exitGame() {
        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }

    public Gameplay activeGame() { return gameplay; }
    public String currentScreen() { return activeCard; }

    public void showWindow() {
        setVisible(true);
        opening.focusStartButton();
    }
}
