/**
 * Represents the terrain in the game world. The terrain consists of layers of blocks
 * with varying colors and depths, generated using noise for realistic height variation.
 */
package pepse.world;

import danogl.GameObject;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;
import pepse.util.ColorSupplier;
import pepse.util.HelpingFunctions;
import pepse.util.NoiseGenerator;
import pepse.util.NoiseGeneratorSingleton;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
/**
    * Represents the terrain in the game world.
    * The terrain consists of layers of blocks
 **/
public class Terrain {

    /** The depth of the terrain in number of block layers. */
    private static final int TERRAIN_DEPTH = 20;

    /** Base color of the ground. */
    private static final Color BASE_GROUND_COLOR = new Color(212, 123, 74);

    /** Color of the middle ground layer. */
    private static final Color MIDDLE_GROUND_COLOR = Color.GRAY;

    /** Color of the deepest ground layer. */
    private static final Color CEIL_GROUND_COLOR = Color.BLACK;

    /** Percentage of terrain depth occupied by the first layer. */
    private static final float GROUND_FIRST_LAYER_PERCENTAGE = 0.6f;

    /** Percentage of terrain depth occupied by the second layer. */
    private static final float GROUND_SECOND_LAYER_PERCENTAGE = 0.9f;

    /** Offset used to determine the initial ground height. */
    private static final float OFFSET_FOR_HEIGHT = (float) 2 / 3;

    /** Factor for scaling the noise generator values. */
    private static final int FACTOR_TO_NOISE_GENERATOR = 6;

    /** Initial ground height at x=0. */
    private static float groundHeightAtX0;

    /** Noise generator for terrain height variation (singleton). */
    private final NoiseGenerator noiseGenerator;

    /**
     * Constructs a new Terrain instance.
     *
     * @param windowDimensions The dimensions of the game window.
     * @param seed The seed for the noise generator, ensuring consistent terrain generation.
     */
    public Terrain(Vector2 windowDimensions, int seed) {
        groundHeightAtX0 = windowDimensions.y() * OFFSET_FOR_HEIGHT;
        noiseGenerator = NoiseGeneratorSingleton.getInstance(seed, (int) groundHeightAtX0);
    }

    /**
     * Calculates the ground height at a specific x-coordinate.
     *
     * @param x The x-coordinate for which to calculate the ground height.
     * @return The aligned ground height at the specified x-coordinate.
     */
    public float groundHeightAt(float x) {
        if (x == 0) {
            return groundHeightAtX0;
        }
        float noise = (float) noiseGenerator.noise(x, Block.SIZE * FACTOR_TO_NOISE_GENERATOR);
        return HelpingFunctions.alignHeight(groundHeightAtX0 + noise, Block.SIZE);
    }

    /**
     * Creates terrain blocks within a specified range of x-coordinates.
     *
     * @param minX The minimum x-coordinate (inclusive).
     * @param maxX The maximum x-coordinate (exclusive).
     * @return A list of GameObjects representing the terrain blocks.
     */
    public List<GameObject> createInRange(int minX, int maxX) {
        List<GameObject> blocks = new ArrayList<>();

        for (int x = minX; x < maxX; x += Block.SIZE) {
            // Calculate and align the ground height
            float groundHeight = groundHeightAt((float) x);
            int alignedHeight = HelpingFunctions.alignHeight(groundHeight, Block.SIZE);

            // Add terrain blocks
            for (int i = 0; i < TERRAIN_DEPTH; i++) {
                Vector2 blockPosition = new Vector2(x, alignedHeight + i * Block.SIZE);

                // Determine the block color based on the layer depth
                Color blockColor;
                if (i < TERRAIN_DEPTH * GROUND_FIRST_LAYER_PERCENTAGE) {
                    blockColor = ColorSupplier.approximateColor(BASE_GROUND_COLOR);
                } else if (i < TERRAIN_DEPTH * GROUND_SECOND_LAYER_PERCENTAGE) {
                    blockColor = ColorSupplier.approximateColor(MIDDLE_GROUND_COLOR);
                } else {
                    blockColor = ColorSupplier.approximateColor(CEIL_GROUND_COLOR);
                }

                // Create the block with the calculated color
                RectangleRenderable blockRenderable = new RectangleRenderable(blockColor);
                Block block = new Block(blockPosition, blockRenderable);
                blocks.add(block);
            }
        }
        return blocks;
    }

    /**
     * Retrieves the initial ground height at x=0.
     *
     * @return The initial ground height at x=0.
     */
    public static float getGroundHeightAtX0() {
        return groundHeightAtX0;
    }
}
