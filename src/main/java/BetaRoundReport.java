import java.util.Objects;

/**
 * An explicitly user-invoked, privacy-minimal summary of one completed round.
 * No networking, persistence, user ID, clock timestamp, OS path or telemetry.
 * This record is NOT evidence that a human playtest took place.
 */
public final class BetaRoundReport {
    private BetaRoundReport() { }

    public static String format(GameModel model, UserPreferences preferences) {
        Objects.requireNonNull(model, "model");
        Objects.requireNonNull(preferences, "preferences");
        if (model.state() != GameState.RESULTS) {
            throw new IllegalStateException("Round summary is available only after a completed match");
        }
        String winner = switch (model.winningPlayer()) {
            case 0 -> "BLUE";
            case 1 -> "ORANGE";
            default -> "DRAW";
        };
        return "AirRaid " + AppVersion.NUMBER + " / " + AppVersion.CHANNEL + "\n"
                + "Report type: local game result (NOT verified human playtest evidence)\n"
                + "State: RESULTS\n"
                + "End reason: " + model.endReason() + "\n"
                + "Winner: " + winner + "\n"
                + "Blue score: " + model.score(0) + "\n"
                + "Orange score: " + model.score(1) + "\n"
                + "Blue shots launched: " + model.shotsLaunched(0) + "\n"
                + "Orange shots launched: " + model.shotsLaunched(1) + "\n"
                + "Blue targets hit: " + model.targetsHit(0) + "\n"
                + "Orange targets hit: " + model.targetsHit(1) + "\n"
                + "Blue hit rate: " + model.hitRatePercent(0) + "%\n"
                + "Orange hit rate: " + model.hitRatePercent(1) + "%\n"
                + "Blue strikes: " + model.canyonStrikes(0) + "\n"
                + "Orange strikes: " + model.canyonStrikes(1) + "\n"
                + "Time remaining ms: " + model.timeRemainingMs() + "\n"
                + "Sound preference: " + (preferences.soundEnabled() ? "enabled" : "disabled") + "\n"
                + "Reduced motion: " + (preferences.reducedMotion() ? "enabled" : "disabled") + "\n"
                + "No player identity, paths, timestamps or network transmission.\n";
    }
}
