package pepse.util;

public class NoiseGeneratorSingleton {

    // The single instance of NoiseGenerator
    private static NoiseGenerator instance;

    // Private constructor to prevent instantiation
    private NoiseGeneratorSingleton() {}

    /**
     * Returns the single instance of NoiseGenerator.
     * If it doesn't exist, it creates a new one.
     *
     * @param seed        The seed for the noise generator.
     * @param startPoint  The initial starting point for noise generation.
     * @return The singleton instance of NoiseGenerator.
     */
    public static NoiseGenerator getInstance(double seed, int startPoint) {
        if (instance == null) {
            instance = new NoiseGenerator(seed, startPoint);
        }
        return instance;
    }
}
