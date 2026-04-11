package pepse.world;

import danogl.GameManager;
import danogl.GameObject;
import danogl.components.CoordinateSpace;
import danogl.components.ScheduledTask;
import danogl.components.Transition;
import danogl.gui.rendering.RectangleRenderable;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;
import pepse.PepseGameManager;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Represents a cloud in the game world.
 * The Cloud class provides functionality for
 * creating and animating cloud objects and generating rain from them.
 */
public class Cloud {
    private static final Color BASE_CLOUD_COLOR = new Color(255, 255, 255);
    private static ArrayList<GameObject> cloudBlocks = new ArrayList<>();
    private static final int MINTEARS = 3;
    private static final int MAX_ADDITIONAL_TEARS = 5;
    private static final float OPACITY_OF_TEAR_START = 1;
    private static final float OPACITY_OF_TEAR_END = 0;
    private static final float TRANSITION_TEAR_TIME = 3;
    private static final float CLOUD_STARTING_X = 0;
    private static final float CLOUD_CYCLE_LENGTH = 10f;
    private static final int RAIN_DROP_SIZE = 10;
    private static final Vector2 TEAR_FALLING_DIRECTION = new Vector2(0, 50);
    private static final List<List<Integer>> cloudPattern = Arrays.asList(
            Arrays.asList(0, 1, 0, 1, 0),
            Arrays.asList(1, 1, 1, 1, 1),
            Arrays.asList(0, 1, 1, 1, 0)
    );
    /**
     * Factory method to create a cloud at a specified position.
     *
     * @param x The x-coordinate of the cloud's starting position.
     * @param y The y-coordinate of the cloud's starting position.
     * @return A new Cloud instance.
     */
    public static Cloud create(float x, float y) {
        Cloud cloud = new Cloud();
        cloud.generateCloud(x, y);
        return cloud;
    }

    /**
     * Generates the cloud structure and initializes cloud blocks at the specified position.
     *
     * @param x The x-coordinate of the cloud's starting position.
     * @param y The y-coordinate of the cloud's starting position.
     */
    private void generateCloud(float x, float y) {
        for (int i = 0; i < cloudPattern.size(); i++) {
            for (int j = 0; j < cloudPattern.get(i).size(); j++) {
                if (cloudPattern.get(i).get(j) == 1) {
                    // Calculate initial position for each block
                    Vector2 initialPosition = new Vector2(x + j * Block.SIZE, y + i * Block.SIZE);

                    // Create the block
                    Block cloudBlock = new Block(initialPosition, new RectangleRenderable(BASE_CLOUD_COLOR));
                    cloudBlock.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
                    cloudBlocks.add(cloudBlock);

                    // Add a transition for this specific block
                    new Transition<>(
                            cloudBlock, // Each block has its own transition
                            (Float offsetX) -> cloudBlock.setTopLeftCorner(
                                    initialPosition.add(new Vector2(offsetX, 0))
                            ),
                            CLOUD_STARTING_X, // Start offset
                            PepseGameManager.WINDOW_SIZE.x(), // End offset
                            Transition.LINEAR_INTERPOLATOR_FLOAT,
                            CLOUD_CYCLE_LENGTH, // Time for one-way movement
                            Transition.TransitionType.TRANSITION_LOOP,
                            null // No callback needed
                    );
                }
            }
        }
    }

    /**
     * Creates rain by generating tear drops from random cloud blocks.
     *
     * @param removeObjectCallback A callback to remove the tear drop
     *                            after its transition completes.
     * @return A list of rain drop GameObjects.
     */
    public static List<GameObject> createRain(Consumer<GameObject> removeObjectCallback) {
        Random random = new Random();
        Set<GameObject> randomElements = new HashSet<>();
        List<GameObject> rainDrops = new ArrayList<>();
        Random moreTearsRandomizer = new Random();
        int moreTears = moreTearsRandomizer.nextInt(MAX_ADDITIONAL_TEARS);

        // Randomly select cloud blocks
        while (randomElements.size() < moreTears + MINTEARS) {
            int randomIndex = random.nextInt(cloudBlocks.size());
            randomElements.add(cloudBlocks.get(randomIndex));
        }

        // Create tear drops for selected cloud blocks
        for (GameObject elem : randomElements) {
            rainDrops.add(createRainTearDrop(elem.getTopLeftCorner(), removeObjectCallback));
        }

        return rainDrops;
    }

    /**
     * Creates a single rain drop and defines its behavior (falling and fading out).
     *
     * @param position             The starting position of the rain drop.
     * @param removeObjectCallback A callback to remove the rain
     *                             drop after its transition completes.
     * @return The rain drop GameObject.
     */
    private static GameObject createRainTearDrop
    (Vector2 position, Consumer<GameObject> removeObjectCallback) {
        Renderable tearDropRenderable = new RectangleRenderable(Color.BLUE);
        GameObject tearDrop =
                new GameObject(position, Vector2.ONES.mult(RAIN_DROP_SIZE), tearDropRenderable);
        tearDrop.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);

        new Transition<>(
                tearDrop,
                tearDrop.renderer()::setOpaqueness,
                OPACITY_OF_TEAR_START,
                OPACITY_OF_TEAR_END,
                Transition.LINEAR_INTERPOLATOR_FLOAT,
                TRANSITION_TEAR_TIME,
                Transition.TransitionType.TRANSITION_ONCE,
                () -> removeObjectCallback.accept(tearDrop) // Inform GameManager to delete
        );
        tearDrop.transform().setAcceleration(TEAR_FALLING_DIRECTION); // Gravity
        return tearDrop;
    }

    /**
     * Returns a list of all cloud blocks in the current cloud instance.
     *
     * @return An ArrayList of GameObject representing the cloud blocks.
     */
    public ArrayList<GameObject> getCloudBlocks() {
        return cloudBlocks;
    }
}
