import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

class SecurityRegressionTest {
    @TempDir Path sandbox;

    @Test void diagnosticsUseStableCodesAndNeverIncludeSensitiveDetails() {
        for (AppDiagnostics.FailureCode code : AppDiagnostics.FailureCode.values()) {
            String diagnostic = AppDiagnostics.diagnostic(code);
            assertTrue(diagnostic.startsWith("AIRRAID_ERROR code="));
            assertFalse(diagnostic.contains(System.getProperty("user.home", "/invalid")));
            assertFalse(diagnostic.contains("Exception"));
            assertFalse(AppDiagnostics.userMessage(code).contains("\\"));
        }
        assertThrows(IllegalArgumentException.class, () -> AppDiagnostics.diagnostic(null));
    }

    @Test void preferencesRefuseSymlinkReadAndWriteWithoutChangingTarget() throws Exception {
        Path outside = sandbox.resolve("important-user-file.txt");
        Files.writeString(outside, "DO NOT OVERWRITE");
        Path candidate = sandbox.resolve("preferences.properties");
        try {
            Files.createSymbolicLink(candidate, outside);
        } catch (UnsupportedOperationException | java.nio.file.FileSystemException failure) {
            // This particular symlink regression needs filesystem link support.
            return;
        }
        PreferencesStore store = new PreferencesStore(candidate);
        assertTrue(store.load().recovered());
        String warning = store.save(UserPreferences.defaults());
        assertNotNull(warning);
        assertEquals("DO NOT OVERWRITE", Files.readString(outside));
        assertTrue(Files.isSymbolicLink(candidate));
    }

    @Test void missingAssetHasHelpfulExceptionWithoutSilentFallback() {
        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> Assets.image("/Resource/Missing/private-photo.png"));
        assertTrue(error.getMessage().contains("Missing bundled game asset"));
        assertThrows(IllegalStateException.class,
                () -> Assets.font("/Resource/Missing/font.ttf", 14f));
    }
}
