package pepse.world.trees;

import pepse.util.HelpingFunctions;
import pepse.world.Block;
import danogl.util.Vector2;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;

/**
 * Manages flora generation (e.g., trees).
 */
public class Flora {
    private final Function<Float, Float> groundHeightFunction;
    private final int seed;
    private final float TREE_PLANT_PROB = 0.35f;
    private final int TREE_MAX_HEIGHT_ADDITION = 3;
    private final int TREE_MIN_ADDITION_HEIGHT = 2;
    private final int TREE_MAX_WIDTH_ADDITION = 4;
    private final int TREE_MIN_ADDITION_WIDTH = 2;

    /**
     * Constructor for Flora.
     *
     * @param groundHeightFunction Function to determine ground height at a given X.
     * @param seed                 Unique seed for flora generation.
     */
    public Flora(Function<Float, Float> groundHeightFunction, int seed) {
        this.groundHeightFunction = groundHeightFunction;
        this.seed=seed;
    }

    /**
     * Generates a list of trees within the specified X range.
     *
     * @param minX The minimum X-coordinate.
     * @param maxX The maximum X-coordinate.
     * @return A list of Tree objects.
     */
    public List<Tree> createInRange(int minX, int maxX) {
        List<Tree> floraTrees = new ArrayList<>();
        int currentX = minX;
        for(int i = minX ; i <= maxX ; i+=Block.SIZE) {
            Random ranodmized = new Random(Objects.hash(currentX, seed));
            if (ranodmized.nextDouble() < TREE_PLANT_PROB) {
                // Align ground height to the nearest block size
                float alignedHeight = groundHeightFunction.apply((float) currentX) - Block.SIZE;
                // Create a new Tree instance with original constructor
                int treeHeight = ranodmized.nextInt(TREE_MAX_HEIGHT_ADDITION) + TREE_MIN_ADDITION_HEIGHT;
                int treeWidth = ranodmized.nextInt(TREE_MAX_WIDTH_ADDITION) + TREE_MIN_ADDITION_WIDTH;
                floraTrees.add(new Tree(currentX, (int) alignedHeight, treeHeight, treeWidth));
                currentX += (treeWidth + 1) * Block.SIZE; //1 TO CREATE A BARRIER
            }
        }

        return floraTrees;
    }
}
