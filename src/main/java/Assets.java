import javax.swing.ImageIcon;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Shared, classpath-only graphics and fonts. All resources are loaded at most
 * once per path, including when the player starts consecutive rounds.
 */
public final class Assets {
    private static final ConcurrentMap<String, Image> IMAGES = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, Font> FONTS = new ConcurrentHashMap<>();

    private Assets() {}

    public static URL url(String path) {
        URL result = Assets.class.getResource(path);
        if (result == null) {
            throw new IllegalStateException("Missing bundled game asset: " + path);
        }
        return result;
    }

    public static Image image(String path) {
        return IMAGES.computeIfAbsent(path, key -> {
            ImageIcon icon = new ImageIcon(url(key));
            if (icon.getIconWidth() <= 0 || icon.getIconHeight() <= 0) {
                throw new IllegalStateException("Unable to decode game image: " + key);
            }
            return icon.getImage();
        });
    }

    public static Font font(String path, float size) {
        if (!(size > 0) || !Float.isFinite(size)) {
            throw new IllegalArgumentException("Font size must be positive and finite");
        }
        Font base = FONTS.computeIfAbsent(path, Assets::loadFont);
        return base.deriveFont(size);
    }

    private static Font loadFont(String path) {
        try (InputStream stream = url(path).openStream()) {
            return Font.createFont(Font.TRUETYPE_FONT, stream);
        } catch (FontFormatException | IOException e) {
            throw new IllegalStateException("Unable to load game font: " + path, e);
        }
    }

    static int cachedImageCount() { return IMAGES.size(); }
    static int cachedFontCount() { return FONTS.size(); }
}
