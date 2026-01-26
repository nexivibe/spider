package ape.spider;

/**
 * Result of a completed Spider Solitaire game.
 */
public class GameResult {
    public enum Outcome {
        WON,
        ABORTED
    }

    private final GameConfig config;
    private final Outcome outcome;
    private final int score;
    private final int moves;
    private final int undos;
    private final float timeSeconds;
    private final int completedSuits;

    public GameResult(GameConfig config, Outcome outcome, int score, int moves, int undos, float timeSeconds, int completedSuits) {
        this.config = config;
        this.outcome = outcome;
        this.score = score;
        this.moves = moves;
        this.undos = undos;
        this.timeSeconds = timeSeconds;
        this.completedSuits = completedSuits;
    }

    public GameConfig getConfig() {
        return config;
    }

    public Outcome getOutcome() {
        return outcome;
    }

    public int getScore() {
        return score;
    }

    public int getMoves() {
        return moves;
    }

    public int getUndos() {
        return undos;
    }

    public float getTimeSeconds() {
        return timeSeconds;
    }

    public int getCompletedSuits() {
        return completedSuits;
    }

    public String getFormattedTime() {
        int totalSeconds = (int) timeSeconds;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    public int getRequiredSuits() {
        int numSuits = config.getNumSuits();
        if (numSuits <= 4) {
            return 8;
        } else if (numSuits == 5) {
            return 10;
        } else {
            return 12;
        }
    }
}
