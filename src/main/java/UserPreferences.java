/**
 * Non-sensitive, locally stored UI preferences. Immutable so settings screens
 * and gameplay can share updates without observing partially changed values.
 */
public record UserPreferences(boolean soundEnabled, int volumePercent,
                              boolean reducedMotion) {
    public static final int SCHEMA_VERSION = 1;
    public static final int DEFAULT_VOLUME = 60;

    public UserPreferences {
        if (volumePercent < 0 || volumePercent > 100) {
            throw new IllegalArgumentException("Volume must be between 0 and 100");
        }
    }

    public static UserPreferences defaults() {
        return new UserPreferences(false, DEFAULT_VOLUME, false);
    }

    public UserPreferences withSound(boolean enabled) {
        return new UserPreferences(enabled, volumePercent, reducedMotion);
    }

    public UserPreferences withVolume(int volume) {
        return new UserPreferences(soundEnabled, volume, reducedMotion);
    }

    public UserPreferences withReducedMotion(boolean reduced) {
        return new UserPreferences(soundEnabled, volumePercent, reduced);
    }
}
