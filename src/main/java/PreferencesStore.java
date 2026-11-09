import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

/**
 * A minimal, versioned local file with atomic replacement and defensive reads.
 * It writes no credentials, telemetry or user identifiers.
 */
public final class PreferencesStore {
    private static final long MAX_BYTES = 4096;
    private final Path path;

    public record LoadResult(UserPreferences preferences, String warning) {
        public boolean recovered() { return warning != null; }
    }

    public PreferencesStore(Path path) {
        if (path == null) throw new IllegalArgumentException("Preferences path required");
        this.path = path;
    }

    public static PreferencesStore forCurrentUser() {
        return new PreferencesStore(Path.of(System.getProperty("user.home", "."),
                                          ".airraid", "preferences.properties"));
    }

    public LoadResult load() {
        try {
            if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
                return new LoadResult(UserPreferences.defaults(), null);
            }
            // Never read from symlinks, devices or pipes in the preferences slot.
            if (Files.isSymbolicLink(path)
                    || !Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)
                    || Files.size(path) > MAX_BYTES) {
                return recovered();
            }
            Properties properties = new Properties();
            try (InputStream stream = Files.newInputStream(path)) {
                properties.load(stream);
            }
            if (!Integer.toString(UserPreferences.SCHEMA_VERSION)
                    .equals(properties.getProperty("version"))) {
                return recovered();
            }
            String sound = properties.getProperty("sound");
            String motion = properties.getProperty("reducedMotion");
            String volume = properties.getProperty("volume");
            if (!validBoolean(sound) || !validBoolean(motion) || volume == null) {
                return recovered();
            }
            int volumePercent = Integer.parseInt(volume);
            return new LoadResult(new UserPreferences(
                    Boolean.parseBoolean(sound), volumePercent,
                    Boolean.parseBoolean(motion)), null);
        } catch (IOException | SecurityException | IllegalArgumentException error) {
            return recovered();
        }
    }

    private static boolean validBoolean(String value) {
        return "true".equals(value) || "false".equals(value);
    }

    private static LoadResult recovered() {
        return new LoadResult(UserPreferences.defaults(),
                "Saved settings could not be read. Safe defaults were restored; "
              + "use Restore defaults or change a setting to replace the damaged file.");
    }

    /** Returns null on success, otherwise a safe user-visible warning. */
    public String save(UserPreferences value) {
        if (value == null) throw new IllegalArgumentException("Preferences required");
        Path temp = null;
        try {
            Path parent = path.toAbsolutePath().getParent();
            if (parent == null) {
                return "Settings could not be saved; changes apply until the game exits.";
            }
            Files.createDirectories(parent);
            // Do not replace a link or a special file at the expected path:
            // this should only ever be a small local regular settings file.
            if (Files.exists(path, LinkOption.NOFOLLOW_LINKS)
                    && (Files.isSymbolicLink(path)
                        || !Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS))) {
                return "Settings location is not a regular file; no changes were written.";
            }
            temp = Files.createTempFile(parent, "preferences-", ".tmp");
            Properties properties = new Properties();
            properties.setProperty("version", Integer.toString(UserPreferences.SCHEMA_VERSION));
            properties.setProperty("sound", Boolean.toString(value.soundEnabled()));
            properties.setProperty("volume", Integer.toString(value.volumePercent()));
            properties.setProperty("reducedMotion", Boolean.toString(value.reducedMotion()));
            try (OutputStream stream = Files.newOutputStream(temp)) {
                properties.store(stream, "AirRaid local UI preferences");
            }
            try {
                Files.move(temp, path, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException unsupported) {
                Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING);
            }
            return null;
        } catch (IOException | SecurityException error) {
            return "Settings could not be saved; changes apply until the game exits.";
        } finally {
            if (temp != null) {
                try { Files.deleteIfExists(temp); } catch (IOException | SecurityException ignored) {
                    // The preferences file is non-sensitive; cleanup is best effort.
                }
            }
        }
    }
}
