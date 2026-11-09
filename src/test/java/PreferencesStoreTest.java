import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

class PreferencesStoreTest {
    @TempDir Path temp;

    @Test void missingProfileDefaultsToMutedVolume60AndNormalMotion() {
        PreferencesStore store = new PreferencesStore(temp.resolve("local/settings.properties"));
        PreferencesStore.LoadResult loaded = store.load();
        assertEquals(UserPreferences.defaults(), loaded.preferences());
        assertNull(loaded.warning());
        assertFalse(loaded.preferences().soundEnabled());
        assertEquals(60, loaded.preferences().volumePercent());
    }

    @Test void preferencesPersistAcrossFreshStoreInstances() throws Exception {
        Path file = temp.resolve("profile/preferences.properties");
        PreferencesStore store = new PreferencesStore(file);
        UserPreferences choice = new UserPreferences(true, 37, true);
        assertNull(store.save(choice));
        PreferencesStore.LoadResult recovered = new PreferencesStore(file).load();
        assertEquals(choice, recovered.preferences());
        assertFalse(recovered.recovered());
        String raw = Files.readString(file);
        assertTrue(raw.contains("version=1"));
        assertTrue(raw.contains("volume=37"));
        assertFalse(raw.toLowerCase().contains("password"));
        try (Stream<Path> files = Files.list(file.getParent())) {
            assertEquals(1, files.count(), "Temporary atomic-save file must be cleaned up");
        }
    }

    @Test void brokenValuesAreNotAppliedAndCanBeReplaced() throws Exception {
        Path file = temp.resolve("prefs.properties");
        PreferencesStore store = new PreferencesStore(file);
        String[] invalid = {
            "version=2\nsound=true\nvolume=44\nreducedMotion=false\n",
            "version=1\nsound=maybe\nvolume=44\nreducedMotion=false\n",
            "version=1\nsound=true\nvolume=101\nreducedMotion=false\n",
            "version=1\nsound=true\nvolume=-1\nreducedMotion=false\n",
            "version=1\nsound=true\nvolume=x\nreducedMotion=false\n",
            "version=1\nsound=true\nvolume=25\nreducedMotion=WRONG\n"
        };
        for (String bad : invalid) {
            Files.writeString(file, bad);
            PreferencesStore.LoadResult state = store.load();
            assertEquals(UserPreferences.defaults(), state.preferences());
            assertNotNull(state.warning());
        }
        assertNull(store.save(UserPreferences.defaults()));
        assertNull(store.load().warning());
    }

    @Test void oversizedOrNonfileProfileFallsBackSafely() throws Exception {
        Path file = temp.resolve("preferences");
        Files.writeString(file, "x".repeat(5_000));
        assertTrue(new PreferencesStore(file).load().recovered());
        Files.delete(file);
        Files.createDirectory(file);
        assertTrue(new PreferencesStore(file).load().recovered());
        assertNotNull(new PreferencesStore(file).save(UserPreferences.defaults()),
                "Unwritable destination must not crash the application");
    }

    @Test void recordValidationAndReset() {
        assertThrows(IllegalArgumentException.class,
                () -> new UserPreferences(false, -1, false));
        assertThrows(IllegalArgumentException.class,
                () -> new UserPreferences(true, 101, true));
        assertEquals(new UserPreferences(false, 12, true),
                UserPreferences.defaults().withVolume(12).withReducedMotion(true));
        assertFalse(UserPreferences.defaults().withSound(true).withSound(false).soundEnabled());
    }
}
