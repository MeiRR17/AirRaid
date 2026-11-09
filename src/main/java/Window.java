import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.HeadlessException;
import java.awt.datatransfer.StringSelection;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Integrated and resize-safe menu, rules, settings, game, and results routing.
 * A settings/rules visit pauses the active game until explicitly resumed.
 */
public final class Window extends JFrame {
    private static final String MENU = "menu";
    private static final String GAME = "game";
    private static final String RULES = "rules";
    private static final String SETTINGS = "settings";
    private static final String QA = "qa";

    private final CardLayout navigation = new CardLayout();
    private final JPanel screens = new JPanel(navigation);
    private final PreferencesStore preferencesStore;
    private UserPreferences preferences;
    private final GameOpening opening;
    private final RulesScreen rules;
    private final SettingsScreen settings;
    private final QaChecklistScreen checklist;
    private Gameplay gameplay;
    private String activeCard = MENU;
    private boolean rulesReturnToGame;
    private boolean settingsReturnToGame;
    private boolean checklistReturnToGame;

    public Window() {
        this(PreferencesStore.forCurrentUser());
    }

    /** Injectable persistence location keeps CI from writing real user preferences. */
    Window(PreferencesStore store) {
        super("Air Raid");
        this.preferencesStore = store;
        PreferencesStore.LoadResult loaded = store.load();
        preferences = loaded.preferences();

        setIconImage(Assets.image("/Resource/Window/gameIcon.jpg"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        opening = new GameOpening(this::startGame, this::showRules,
                                  this::showSettings, this::showChecklist, this::exitGame);
        opening.setReducedMotion(preferences.reducedMotion());
        rules = new RulesScreen(this::returnFromRules);
        settings = new SettingsScreen(this::updatePreferences, this::returnFromSettings);
        checklist = new QaChecklistScreen(this::returnFromChecklist, this::copyChecklist);
        settings.setPreferences(preferences);
        settings.showWarning(loaded.warning());
        screens.add(opening, MENU);
        screens.add(rules, RULES);
        screens.add(settings, SETTINGS);
        screens.add(checklist, QA);
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
                if (gameplay != null && GAME.equals(activeCard)) {
                    gameplay.pauseGame();
                    gameplay.resetShortcutLatches();
                }
            }
        });
    }

    private void showCard(String card) {
        activeCard = card;
        navigation.show(screens, card);
        screens.revalidate();
        screens.repaint();
    }

    private void updatePreferences(UserPreferences next) {
        preferences = next;
        String warning = preferencesStore.save(next);
        settings.setPreferences(next);
        settings.showWarning(warning);
        opening.setReducedMotion(next.reducedMotion());
        if (gameplay != null) gameplay.applyPreferences(next);
    }

    public void startGame() {
        if (gameplay != null) {
            gameplay.disposeGame();
            screens.remove(gameplay);
        }
        gameplay = new Gameplay(this::returnToMenu, this::showRules,
                this::showSettings, this::showChecklist,
                preferences, this::updatePreferences);
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
            gameplay.resetShortcutLatches();
            gameplay.focusGame();
        } else {
            showCard(MENU);
            opening.focusStartButton();
        }
        rulesReturnToGame = false;
    }

    public void showSettings() {
        settingsReturnToGame = GAME.equals(activeCard) && gameplay != null;
        if (settingsReturnToGame) gameplay.pauseGame();
        settings.setPreferences(preferences);
        showCard(SETTINGS);
        settings.focusFirstControl();
    }

    public void returnFromSettings() {
        if (settingsReturnToGame && gameplay != null) {
            showCard(GAME);
            gameplay.resetShortcutLatches();
            gameplay.focusGame();
        } else {
            showCard(MENU);
            opening.focusStartButton();
        }
        settingsReturnToGame = false;
    }

    /**
     * Checking a step does not authenticate a tester. Copying is optional and
     * stays entirely local; no evidence file or network request is generated.
     */
    private boolean copyChecklist(String report) {
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                    new StringSelection(report), null);
            return true;
        } catch (HeadlessException | IllegalStateException | SecurityException unavailable) {
            return false;
        }
    }

    public void showChecklist() {
        checklistReturnToGame = GAME.equals(activeCard) && gameplay != null;
        if (checklistReturnToGame) gameplay.pauseGame();
        showCard(QA);
        checklist.focusFirstControl();
    }

    public void returnFromChecklist() {
        if (checklistReturnToGame && gameplay != null) {
            showCard(GAME);
            gameplay.resetShortcutLatches();
            gameplay.focusGame();
        } else {
            showCard(MENU);
            opening.focusStartButton();
        }
        checklistReturnToGame = false;
    }

    public void returnToMenu() {
        if (gameplay != null) {
            gameplay.disposeGame();
            screens.remove(gameplay);
            gameplay = null;
        }
        rulesReturnToGame = false;
        settingsReturnToGame = false;
        checklistReturnToGame = false;
        showCard(MENU);
        opening.focusStartButton();
    }

    public void exitGame() {
        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }

    public Gameplay activeGame() { return gameplay; }
    public String currentScreen() { return activeCard; }
    public SettingsScreen settingsScreen() { return settings; }
    public QaChecklistScreen checklistScreen() { return checklist; }
    public UserPreferences preferences() { return preferences; }
    public boolean menuReducedMotion() { return opening.reducedMotionShown(); }

    public void showWindow() {
        setVisible(true);
        opening.focusStartButton();
    }
}
