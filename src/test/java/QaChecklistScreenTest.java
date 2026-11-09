import org.junit.jupiter.api.Test;
import javax.swing.SwingUtilities;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

class QaChecklistScreenTest {
    @Test void checkboxesAreManualAndReportsNeverAutoTransmit() throws Exception {
        AtomicReference<String> copy = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            QaChecklistScreen qa = new QaChecklistScreen(() -> {},
                    text -> { copy.set(text); return true; });
            assertEquals(8, QaChecklistScreen.STEPS.length);
            assertEquals(0, qa.completedCount());
            assertNull(copy.get());
            assertTrue(qa.step(0).isFocusable());
            assertNotNull(qa.step(0).getAccessibleContext().getAccessibleDescription());
            qa.step(0).doClick();
            qa.step(1).doClick();
            assertEquals(2, qa.completedCount());
            assertNull(copy.get(), "no implicit clipboard write or telemetry");
            String report = qa.reportText();
            assertTrue(report.contains("AirRaid " + AppVersion.NUMBER));
            assertTrue(report.contains("NOT verified human playtest evidence"));
            assertTrue(report.contains("Checked: 2/8"));
            assertTrue(report.contains("[x] " + QaChecklistScreen.STEPS[0]));
            assertTrue(report.contains("[ ] " + QaChecklistScreen.STEPS[7]));
            assertFalse(report.contains(System.getProperty("user.home", "/nonexistent")));
            assertFalse(report.contains("https://"));
            qa.copyButton().doClick();
            assertEquals(report, copy.get());
            assertTrue(qa.statusText().contains("copied"));
        });
    }

    @Test void clipboardFailureShowsWarningAndDoesNotLoseChecks() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            QaChecklistScreen qa = new QaChecklistScreen(() -> {}, report -> false);
            qa.step(2).doClick();
            qa.copyButton().doClick();
            assertTrue(qa.statusText().contains("unavailable"));
            assertEquals(1, qa.completedCount());
        });
    }

    @Test void checklistExistsOnlyForCurrentSession() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            QaChecklistScreen first = new QaChecklistScreen(() -> {}, report -> true);
            first.step(4).doClick();
            QaChecklistScreen second = new QaChecklistScreen(() -> {}, report -> true);
            assertEquals(1, first.completedCount());
            assertEquals(0, second.completedCount());
        });
    }

    @Test void keyboardEscapeNavigatesBack() throws Exception {
        AtomicInteger called = new AtomicInteger();
        SwingUtilities.invokeAndWait(() -> {
            QaChecklistScreen qa = new QaChecklistScreen(called::incrementAndGet,
                    report -> true);
            Object binding = qa.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW)
                    .get(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0));
            assertNotNull(binding);
            qa.getActionMap().get(binding).actionPerformed(
                    new java.awt.event.ActionEvent(qa, 0, "escape"));
            assertEquals(1, called.get());
        });
    }
}
