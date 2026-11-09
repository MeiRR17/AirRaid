import org.junit.jupiter.api.Test;
import javax.swing.SwingUtilities;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

class BetaRoundReportTest {
    @Test void onlyCompletedMatchesCanCreateQaReport() {
        GameModel model = new GameModel();
        assertThrows(IllegalStateException.class,
                () -> BetaRoundReport.format(model, UserPreferences.defaults()));
        model.startNewGame();
        assertThrows(IllegalStateException.class,
                () -> BetaRoundReport.format(model, UserPreferences.defaults()));
    }

    @Test void exportedReportIsDeterministicMinimalAndNotHumanTestEvidence() {
        GameModel model = new GameModel(List.of(), 80, 656);
        model.startNewGame();
        model.tick(GameRules.MATCH_DURATION_MS);
        UserPreferences p = new UserPreferences(false, 25, true);
        String text = BetaRoundReport.format(model, p);
        assertEquals(text, BetaRoundReport.format(model, p));
        assertTrue(text.contains("AirRaid " + AppVersion.NUMBER + " / INTERNAL BETA"));
        assertTrue(text.contains("End reason: TIME_EXPIRED"));
        assertTrue(text.contains("Winner: DRAW"));
        assertTrue(text.contains("Blue strikes: 0"));
        assertTrue(text.contains("Reduced motion: enabled"));
        assertTrue(text.contains("NOT verified human playtest evidence"));
        assertFalse(text.contains(System.getProperty("user.home", "/missing")));
        assertFalse(text.contains("password"));
        assertFalse(text.contains("http://"));
        assertFalse(text.contains("https://"));
        assertFalse(text.contains("2026-"));
    }

    @Test void completedStrikeMatchReportsActualWinnerAndScores() {
        GameModel model = new GameModel(List.of(), 80, 656);
        model.startNewGame();
        for (int i = 0; i < 4; i++) {
            model.pressFire(0);
            model.tick(3000);
            model.releaseFire(0);
        }
        String text = BetaRoundReport.format(model, UserPreferences.defaults());
        assertTrue(text.contains("End reason: CANYON_STRIKES"));
        assertTrue(text.contains("Winner: ORANGE"));
        assertTrue(text.contains("Blue strikes: 4"));
    }

    @Test void resultsScreenCopiesOnlyAfterExplicitAction() throws Exception {
        AtomicReference<String> copied = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            GameModel model = new GameModel();
            model.startNewGame();
            model.tick(GameRules.MATCH_DURATION_MS);
            ResultsScreen ui = new ResultsScreen(() -> {}, () -> {},
                    report -> { copied.set(report); return true; });
            ui.showResult(model, UserPreferences.defaults());
            assertNull(copied.get(), "No telemetry or implicit clipboard operation");
            assertEquals("It's a tie!", ui.outcomeText());
            assertNotNull(ui.reportButton().getAccessibleContext().getAccessibleDescription());
            ui.reportButton().doClick();
            assertEquals(ui.reportText(), copied.get());
            assertTrue(ui.reportStatusText().contains("copied"));
        });
    }

    @Test void clipboardFailureIsNonFatalAndHonest() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            GameModel model = new GameModel();
            model.startNewGame();
            model.tick(GameRules.MATCH_DURATION_MS);
            ResultsScreen ui = new ResultsScreen(() -> {}, () -> {}, text -> false);
            ui.showResult(model, UserPreferences.defaults());
            ui.reportButton().doClick();
            assertTrue(ui.reportStatusText().contains("unavailable"));
        });
    }
}
