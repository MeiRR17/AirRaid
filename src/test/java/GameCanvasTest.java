import org.junit.jupiter.api.Test;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import static org.junit.jupiter.api.Assertions.*;

/** Headless-safe checks for responsive logical-canvas scaling and results. */
class GameCanvasTest {
    @Test void landscapeLetterboxCentersArena() {
        GameCanvas.Viewport view = GameCanvas.viewportFor(1000, 600);
        assertEquals(200, view.x());
        assertEquals(0, view.y());
        assertEquals(600, view.width());
        assertEquals(600, view.height());
        assertEquals(0.75, view.scale(), 1e-10);
    }

    @Test void portraitLetterboxCentersArena() {
        GameCanvas.Viewport view = GameCanvas.viewportFor(600, 1000);
        assertEquals(0, view.x());
        assertEquals(200, view.y());
        assertEquals(600, view.width());
        assertEquals(600, view.height());
        assertEquals(0.75, view.scale(), 1e-10);
    }

    @Test void fullSizeAndZeroDimensionsAreSafe() {
        GameCanvas.Viewport full = GameCanvas.viewportFor(800, 800);
        assertEquals(800, full.width());
        assertEquals(800, full.height());
        assertEquals(1, full.scale(), 1e-10);
        assertEquals(0, GameCanvas.viewportFor(0, 400).width());
        assertEquals(0, GameCanvas.viewportFor(-100, 400).height());
        assertEquals(0, GameCanvas.viewportFor(400, 0).scale(), 1e-10);
    }

    @Test void offscreenRenderingPreservesSideBarsAndDoesNotThrow() {
        GameModel model = new GameModel();
        model.startNewGame();
        GameCanvas canvas = new GameCanvas(model);
        canvas.setSize(1000, 600);
        BufferedImage output = new BufferedImage(1000, 600,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = output.createGraphics();
        try {
            canvas.paint(graphics);
        } finally {
            graphics.dispose();
        }
        assertEquals(new Color(11, 19, 37).getRGB(), output.getRGB(0, 300));
        assertEquals(new Color(11, 19, 37).getRGB(), output.getRGB(999, 300));
    }

    @Test void resultsScreenShowsModelScoresAfterTimerEnds() {
        GameModel model = new GameModel();
        model.startNewGame();
        model.tick(GameModel.MATCH_DURATION_MS);
        ResultsScreen results = new ResultsScreen(() -> {}, () -> {}, report -> true);
        results.showResult(model, UserPreferences.defaults());
        assertEquals("It's a tie!", results.outcomeText());
    }
}
