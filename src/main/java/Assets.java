import javax.swing.ImageIcon;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/** Classpath-only resource loader: works from the runnable JAR on all operating systems. */
public final class Assets {
    private Assets() {}

    public static URL url(String path) {
        URL result = Assets.class.getResource(path);
        if (result == null) {
            throw new IllegalStateException("Missing bundled game asset: " + path);
        }
        return result;
    }

    public static Image image(String path) {
        ImageIcon icon = new ImageIcon(url(path));
        if (icon.getIconWidth() <= 0 || icon.getIconHeight() <= 0) {
            throw new IllegalStateException("Unable to decode game image: " + path);
        }
        return icon.getImage();
    }

    public static Font font(String path, float size) {
        try (InputStream stream = url(path).openStream()) {
            return Font.createFont(Font.TRUETYPE_FONT, stream).deriveFont(size);
        } catch (FontFormatException | IOException e) {
            throw new IllegalStateException("Unable to load game font: " + path, e);
        }
    }
}
