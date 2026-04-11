package pepse.world.trees;

import danogl.GameObject;
import danogl.components.ScheduledTask;
import danogl.components.Transition;
import danogl.gui.rendering.RectangleRenderable;
import pepse.util.HelpingFunctions;
import pepse.world.Block;
import danogl.util.Vector2;
import danogl.gui.rendering.Renderable;

import java.awt.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

/**
 * Represents a Tree object in the game world. The Tree class handles the creation of log blocks, leaf blocks,
 * and fruits, including their positioning and behaviors such as wind sway for the leaves.
 */
public class Tree {

    private static final Renderable logRenderable = new RectangleRenderable(new Color(100, 50, 20));
    private static final Renderable leafRenderable = new RectangleRenderable(new Color(50, 200, 30));
    private LogBlock[] logBlocks; // Array of log blocks forming the trunk of the tree
    private LeafBlock[] leafBlocks; // Array of leaf blocks forming the tree's foliage
    private ArrayList<Fruit> fruits = new ArrayList<>(); // List of fruits generated on the tree

    /**
     * Constructs a tree with specified position, height, and width.
     *
     * @param x The x-coordinate of the base of the tree.
     * @param y The y-coordinate of the base of the tree.
     * @param height The number of blocks in the tree trunk.
     * @param width The width of the foliage (in blocks).
     */
    Tree(int x, int y, int height, int width) {
        logBlocks = new LogBlock[height];
        int amountOfLeafBlocks = HelpingFunctions.getTriangleAmount(width); // Total blocks in the triangle
        leafBlocks = new LeafBlock[amountOfLeafBlocks];

        // Create log blocks
        for (int i = 0; i < height; i++) {
            logBlocks[i] = new LogBlock(new Vector2(x, y - i * Block.SIZE));
        }

        // Create leaf blocks (forming a triangle, bottom-up)
        int leafIndex = 0;
        for (int i = 0; i < width; i++) { // For each row of the triangle
            int rowWidth = width - i; // Number of blocks in the current row
            int startX = x - ((rowWidth - 1) * Block.SIZE) / 2; // Center-align the row
            for (int j = 0; j < rowWidth; j++) { // Place blocks in the current row
                int leafX = startX + j * Block.SIZE;
                int leafY = y - (height * Block.SIZE) - (i * Block.SIZE);
                leafBlocks[leafIndex++] = new LeafBlock(new Vector2(leafX, leafY));
            }
        }

    }

    /**
     * Returns the log blocks of the tree.
     *
     * @return An array of log blocks.
     */
    public GameObject[] getTreeLogBlocks() {
        return logBlocks;
    }

    /**
     * Returns the leaf blocks of the tree.
     *
     * @return An array of leaf blocks.
     */
    public GameObject[] getTreeLeafBlocks() {
        return leafBlocks;
    }

    /**
     * Returns the list of fruits on the tree.
     *
     * @return An ArrayList of fruits.
     */
    public ArrayList<Fruit> getFruits() {
        return fruits;
    }

    /**
     * Represents a single block of the tree trunk (log).
     */
    private class LogBlock extends Block {
        /**
         * Constructs a log block at the specified top-left corner position.
         *
         * @param topLeftCorner The top-left corner position of the log block.
         */
        public LogBlock(Vector2 topLeftCorner) {
            super(topLeftCorner, logRenderable);
        }
    }

    /**
     * Represents a single block of the tree's foliage (leaf).
     * Includes behaviors such as swaying and potential fruit generation.
     */
    private class LeafBlock extends Block {
        private static final float ANGLE_MIN = -30f; // Minimum angle for leaf sway
        private static final float ANGLE_MAX = 30f;  // Maximum angle for leaf sway
        private static final float PROBABILITY_FOR_FRUIT = 0.2f; // Probability of a fruit being generated
        private static final float FRUIT_OFFSET_POS = 5; // Offset to center the fruit on the leaf block
        private static final float SWAY_CYCLE_TIME_BASE = 5f; // Base time for a full sway cycle
        private static final float SWAY_CYCLE_TIME_VARIANCE = 2f; // Variance in sway cycle time
        private static final float START_DELAY_VARIANCE = 1f; // Variance in start delay

        /**
         * Constructs a leaf block at the specified top-left corner position.
         *
         * @param topLeftCorner The top-left corner position of the leaf block.
         */
        public LeafBlock(Vector2 topLeftCorner) {
            super(topLeftCorner, leafRenderable);

            // Use the sine wind implementation for realistic sway
            WindMovement windMovement = WindMovement.sineWind();

            // Randomize the cycle time and start delay for realistic behavior
            Random random = new Random(Objects.hash(topLeftCorner.x(), topLeftCorner.y()));
            float swayCycleTime = SWAY_CYCLE_TIME_BASE + random.nextFloat() * SWAY_CYCLE_TIME_VARIANCE;
            float startDelay = random.nextFloat() * START_DELAY_VARIANCE;

            // Schedule a task to add swaying transition with a delay
            new ScheduledTask(this, startDelay, false, () -> {
                new Transition<>(
                        this,
                        angle -> renderer().setRenderableAngle(angle), // Adjust angle
                        ANGLE_MIN,
                        ANGLE_MAX,
                        windMovement::apply, // Use the sine realistic wind movement
                        swayCycleTime,
                        Transition.TransitionType.TRANSITION_BACK_AND_FORTH,
                        null
                );
            });

            // Randomly decide if a fruit should be added to this leaf block
            if (random.nextDouble() < PROBABILITY_FOR_FRUIT) {
                fruits.add(new Fruit(topLeftCorner.add(Vector2.ONES.mult(FRUIT_OFFSET_POS))));
            }
        }
    }
}
