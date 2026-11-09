import org.junit.jupiter.api.Test;
import javax.swing.SwingUtilities;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

class AccessibilityPreferencesTest {
    @Test void settingsSupportsKeyboardFocusAndImmediateUpdates() throws Exception {
        AtomicReference<UserPreferences> saved = new AtomicReference<>(
                UserPreferences.defaults());
        AtomicReference<SettingsScreen> holder = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            SettingsScreen ui = new SettingsScreen(next -> {
                saved.set(next);
                holder.get().setPreferences(next);
            }, () -> {});
            holder.set(ui);
            assertTrue(ui.soundToggle().isFocusable());
            assertTrue(ui.reducedMotionToggle().isFocusable());
            assertTrue(ui.volumeSlider().isFocusable());
            assertEquals(60, ui.volumeSlider().getValue());
            assertNotNull(ui.soundToggle().getAccessibleContext().getAccessibleName());

            ui.soundToggle().doClick();
            assertTrue(saved.get().soundEnabled());
            ui.reducedMotionToggle().doClick();
            assertTrue(saved.get().reducedMotion());
            ui.volumeSlider().setValue(25);
            assertEquals(25, saved.get().volumePercent());
            ui.showWarning("Saved settings could not be read");
            assertTrue(ui.statusText().contains("could not"));
            ui.setPreferences(UserPreferences.defaults());
            assertEquals(60, ui.volumeSlider().getValue());
            assertFalse(ui.soundToggle().isSelected());
        });
    }

    @Test void reducedMotionSkipsTransientVisualFeedback() {
        GameModel model = new GameModel();
        model.startNewGame();
        GameCanvas canvas = new GameCanvas(model);
        GameModel.Feedback hit = new GameModel.Feedback(
                GameModel.FeedbackType.TARGET_HIT, 0, 400, 500, 4);
        canvas.showFeedback(hit);
        assertEquals(1, canvas.visibleEffectCount());
        canvas.setReducedMotion(true);
        assertTrue(canvas.isReducedMotion());
        assertEquals(0, canvas.visibleEffectCount());
        canvas.showFeedback(hit);
        assertEquals(0, canvas.visibleEffectCount());
        canvas.setReducedMotion(false);
        canvas.showFeedback(hit);
        assertEquals(1, canvas.visibleEffectCount());
    }

    @Test void PCMVolumeIsAppliedWithoutAudioDevice() {
        byte[] silent = SoundEffects.synthesize(SoundEffects.Cue.HIT, 0);
        for (byte value : silent) assertEquals(0, value);
        byte[] loud = SoundEffects.synthesize(SoundEffects.Cue.HIT, 100);
        byte[] quiet = SoundEffects.synthesize(SoundEffects.Cue.HIT, 30);
        assertEquals(loud.length, quiet.length);
        assertEquals(loud.length, silent.length);
        assertTrue(java.util.stream.IntStream.range(0, loud.length)
                .anyMatch(i -> loud[i] != quiet[i]));
        assertThrows(IllegalArgumentException.class,
                () -> SoundEffects.synthesize(SoundEffects.Cue.HIT, -1));
        assertThrows(IllegalArgumentException.class,
                () -> SoundEffects.synthesize(SoundEffects.Cue.HIT, 101));

        SoundEffects sound = new SoundEffects();
        assertFalse(sound.isEnabled());
        assertEquals(60, sound.volumePercent());
        sound.setVolume(38); // must not open a mixer while muted
        assertEquals(38, sound.volumePercent());
        assertFalse(sound.isEnabled());
        sound.close();
    }
}
