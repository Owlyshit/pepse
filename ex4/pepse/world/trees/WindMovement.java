package pepse.world.trees;




/**
 * Represents a Tree object in the game world. The Tree class handles the creation of log blocks, leaf blocks,
 * and fruits, including their positioning and behaviors such as wind sway for the leaves.
 */
@FunctionalInterface
public interface WindMovement {
    /**
     * Applies the wind movement to a range of values.
     *
     * @param n1 The lower bound of the range.
     * @param n2 The upper bound of the range.
     * @param t  The time value to apply the wind movement.
     * @return The value after applying the wind movement.
     */
    float apply(float n1, float n2, float t);
    /**
     * Creates a wind movement that oscillates between two
     * values using a sine function.
     * @return A wind movement that oscillates between two values.
     */
    static WindMovement sineWind() {
        return (n1, n2, t) -> {
            float amplitude = (n2 - n1) / 2;
            float midpoint = (n1 + n2) / 2;
            return (float) (midpoint + amplitude * Math.sin(t * Math.PI * 2));
        };
    }
}
