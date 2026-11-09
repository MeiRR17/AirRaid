import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/** Model events and generated audio are deterministic and headless-safe. */
class FeedbackAndAudioTest {
    @Test void eventsDrainOnceAndRestartClearsOldFeedback() {
        GameModel model = new GameModel(List.of(), 80, 656);
        model.startNewGame();
        assertTrue(model.drainFeedback().isEmpty());
        model.pressFire(0);
        List<GameModel.Feedback> fired = model.drainFeedback();
        assertEquals(1, fired.size());
        assertEquals(GameModel.FeedbackType.SHOT, fired.get(0).type());
        assertEquals(0, fired.get(0).player());
        assertTrue(model.drainFeedback().isEmpty());

        model.tick(3_000);
        List<GameModel.Feedback> missed = model.drainFeedback();
        assertEquals(1, missed.size());
        assertEquals(GameModel.FeedbackType.CANYON_MISS, missed.get(0).type());
        assertEquals(1, model.canyonStrikes(0));

        model.pressFire(0);
        model.startNewGame();
        assertTrue(model.drainFeedback().isEmpty());
        assertEquals(0, model.canyonStrikes(0));
    }

    @Test void targetHitAndRoundEndProduceCorrectEvents() {
        GameModel model = new GameModel(
                List.of(new Target(390, 550, 20, 20, 3)), 132, 596);
        model.startNewGame();
        model.pressFire(0);
        model.drainFeedback();
        model.tick(1_400);
        List<GameModel.Feedback> impact = model.drainFeedback();
        assertEquals(1, impact.size());
        assertEquals(GameModel.FeedbackType.TARGET_HIT, impact.get(0).type());
        assertEquals(3, impact.get(0).points());
        assertEquals(3, model.score(0));

        model.tick(70_000);
        List<GameModel.Feedback> finish = model.drainFeedback();
        assertEquals(1, finish.size());
        assertEquals(GameModel.FeedbackType.ROUND_FINISHED, finish.get(0).type());
        assertEquals(0, finish.get(0).player());
        assertEquals(GameState.RESULTS, model.state());
    }

    @Test void allOriginalSynthesizedCuesHaveValidPcmAndFadeToSilence() {
        for (SoundEffects.Cue cue : SoundEffects.Cue.values()) {
            byte[] samples = SoundEffects.synthesize(cue);
            assertTrue(samples.length >= 4_000);
            assertEquals(0, samples.length % 2);
            assertEquals(0, samples[0]);
            assertEquals(0, samples[1]);
            assertEquals(0, samples[samples.length - 1]);
            assertEquals(0, samples[samples.length - 2]);
            assertTrue(java.util.stream.IntStream.range(10, samples.length - 10)
                    .anyMatch(i -> samples[i] != 0),
                    "Generated " + cue + " must not be silent");
        }
    }

    @Test void audioOpensNoDeviceUnlessExplicitlyEnabled() {
        SoundEffects effects = new SoundEffects();
        assertFalse(effects.isEnabled());
        effects.play(SoundEffects.Cue.SHOT); // safe without mixer
        assertTrue(effects.setEnabled(false));
        assertFalse(effects.isEnabled());
        effects.close();
        assertThrows(IllegalArgumentException.class,
                () -> SoundEffects.synthesize(null));
    }

    @Test void visualEffectsExpireAndDoNotChangeScore() {
        GameModel model = new GameModel();
        model.startNewGame();
        GameCanvas canvas = new GameCanvas(model);
        GameModel.Feedback hit = new GameModel.Feedback(
                GameModel.FeedbackType.TARGET_HIT, 0, 400, 600, 4);
        canvas.showFeedback(hit);
        assertEquals(1, canvas.visibleEffectCount());
        for (int i = 0; i < 24; i++) canvas.advanceFeedback();
        assertEquals(0, canvas.visibleEffectCount());
        assertEquals(0, model.score(0));
        canvas.showFeedback(new GameModel.Feedback(
                GameModel.FeedbackType.ROUND_FINISHED, -1, 400, 400, 0));
        assertEquals(0, canvas.visibleEffectCount());
        canvas.showFeedback(hit);
        canvas.clearFeedback();
        assertEquals(0, canvas.visibleEffectCount());
    }

    @Test void imageAndFontLoadsReuseOneResource() {
        String path = "/Resource/Target/1.png";
        assertSame(Assets.image(path), Assets.image(path));
        assertEquals(Assets.font("/Resource/Font/Pixel.ttf", 20f).getFamily(),
                Assets.font("/Resource/Font/Pixel.ttf", 28f).getFamily());
        assertThrows(IllegalArgumentException.class,
                () -> Assets.font("/Resource/Font/Pixel.ttf", 0));
        assertThrows(IllegalArgumentException.class,
                () -> Assets.font("/Resource/Font/Pixel.ttf", Float.NaN));
        assertTrue(Assets.cachedImageCount() >= 1);
        assertTrue(Assets.cachedFontCount() >= 1);
    }
}
