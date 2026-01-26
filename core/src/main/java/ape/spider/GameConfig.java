package ape.spider;

/**
 * Configuration for a Spider Solitaire game.
 */
public class GameConfig {
    public enum GameMode {
        SOLO_PRACTICE,
        DAILY_GRIND
    }

    private final GameMode mode;
    private final int numSuits;
    private final long seed;

    public GameConfig(GameMode mode, int numSuits, long seed) {
        this.mode = mode;
        this.numSuits = Math.max(1, Math.min(6, numSuits)); // Clamp between 1-6
        this.seed = seed;
    }

    public GameMode getMode() {
        return mode;
    }

    public int getNumSuits() {
        return numSuits;
    }

    public long getSeed() {
        return seed;
    }

    // Factory methods for convenience
    public static GameConfig soloPractice(int numSuits) {
        return new GameConfig(GameMode.SOLO_PRACTICE, numSuits, System.currentTimeMillis());
    }

    public static GameConfig dailyGrind(int numSuits, long dailySeed) {
        return new GameConfig(GameMode.DAILY_GRIND, numSuits, dailySeed);
    }
}
