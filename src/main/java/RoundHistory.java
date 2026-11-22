import java.util.Objects;

/**
 * In-memory match record for the current local game-screen session only.
 * No files, dates, player identifiers, telemetry or network requests.
 *
 * The owner (Gameplay) lives on Swing's Event Dispatch Thread, so this class
 * is intentionally not synchronized. A new Gameplay instance resets history.
 */
public final class RoundHistory {
    /** Immutable record presented on the results screen. */
    public record Summary(
            int rounds,
            int blueWins,
            int orangeWins,
            int draws,
            int bestBlueScore,
            int bestOrangeScore) {
        public Summary {
            if (rounds < 0 || blueWins < 0 || orangeWins < 0 || draws < 0
                    || blueWins + orangeWins + draws != rounds
                    || bestBlueScore < 0 || bestOrangeScore < 0) {
                throw new IllegalArgumentException("Invalid session summary");
            }
        }

        public String compactLabel() {
            return "SESSION " + rounds + " rounds  |  Blue " + blueWins
                    + " wins  |  Orange " + orangeWins
                    + " wins  |  Draw " + draws;
        }

        public String bestScoreLabel() {
            return "Session-best scores: Blue " + bestBlueScore
                    + ", Orange " + bestOrangeScore
                    + ". Session history is memory-only and resets on menu exit.";
        }
    }

    private int rounds;
    private int blueWins;
    private int orangeWins;
    private int draws;
    private int bestBlueScore;
    private int bestOrangeScore;

    /**
     * Register a completed round once. The caller prevents duplicate
     * callbacks; a non-finished model is rejected rather than recorded.
     */
    public Summary record(GameModel model) {
        Objects.requireNonNull(model, "model");
        if (model.state() != GameState.RESULTS) {
            throw new IllegalStateException("Cannot record an unfinished round");
        }
        int winner = model.winningPlayer();
        if (winner == 0) {
            blueWins++;
        } else if (winner == 1) {
            orangeWins++;
        } else if (winner == -1) {
            draws++;
        } else {
            throw new IllegalStateException("Unknown winner: " + winner);
        }
        rounds++;
        bestBlueScore = Math.max(bestBlueScore, model.score(0));
        bestOrangeScore = Math.max(bestOrangeScore, model.score(1));
        return summary();
    }

    public Summary summary() {
        return new Summary(rounds, blueWins, orangeWins, draws,
                bestBlueScore, bestOrangeScore);
    }
}
