import org.junit.jupiter.api.Test;
import java.awt.Image;
import static org.junit.jupiter.api.Assertions.*;

class AssetsTest {
    @Test
    void allCoreResourcesAreBundled() {
        String[] resources = {
            "/Resource/General/background.png",
            "/Resource/General/FLY_.gif",
            "/Resource/Window/gameIcon.jpg",
            "/Resource/Font/Pixel.ttf",
            "/Resource/Bomb/Blue.png",
            "/Resource/Bomb/Orange.png",
            "/Resource/Target/1.png",
            "/Resource/Target/2.png",
            "/Resource/Target/3.png",
            "/Resource/Target/4.png"
        };
        for (String resource : resources) {
            assertNotNull(Assets.url(resource), resource);
        }
        for (String team : new String[] {"BLUE", "ORANGE"}) {
            for (int variant = 1; variant <= 5; variant++) {
                assertNotNull(Assets.url("/Resource/Player/" + team + "_TEAM/option" + variant + ".png"));
            }
        }
    }

    @Test
    void imagesAndFontAreReadableInPackagedClasspath() {
        Image background = Assets.image("/Resource/General/background.png");
        assertTrue(background.getWidth(null) > 0);
        assertTrue(background.getHeight(null) > 0);
        assertNotNull(Assets.font("/Resource/Font/Pixel.ttf", 30f));
    }

    @Test
    void missingAssetProducesActionableError() {
        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> Assets.url("/Resource/General/missing.png"));
        assertTrue(error.getMessage().contains("missing.png"));
    }
}
