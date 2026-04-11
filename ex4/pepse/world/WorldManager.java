package pepse.world;

import danogl.GameObject;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;
import pepse.util.HelpingFunctions;
import pepse.world.daynight.DayCycleFacade;
import pepse.world.trees.Flora;
import pepse.world.trees.Tree;

import java.util.List;

/**
 * Manages the game world components, including terrain generation, day-night cycle, and flora.
 */
public class WorldManager {
    /**
     * The size of each chunk in the world.
     */
    public static final int CHUNK_SIZE = 600;

    /**
     * The length of the day-night cycle in seconds.
     */
    public static final int CYCLE_LENGTH = 30;

    /**
     * Base seed for terrain and flora generation, initialized using the current system time.
     */
    private static final int BASE_SEED = (int) System.currentTimeMillis() * 100;

    private final Terrain terrain;
    private final DayCycleFacade dayCycleFacade;
    private final Flora flora;

    /**
     * Constructs a WorldManager instance and initializes terrain, day-night cycle, and flora.
     *
     * @param windowDimensions The dimensions of the game window.
     */
    public WorldManager(Vector2 windowDimensions) {
        this.terrain = new Terrain(windowDimensions, BASE_SEED);
        this.dayCycleFacade = new DayCycleFacade(windowDimensions, CYCLE_LENGTH);
        this.flora = new Flora(terrain::groundHeightAt, BASE_SEED);
    }

    /**
     * Generates terrain blocks for a specific chunk based on its index.
     *
     * @param chunkIndex The index of the chunk to generate.
     * @return A list of GameObjects representing the terrain blocks in the chunk.
     */
    public List<GameObject> generateTerrainBlocks(int chunkIndex) {
        int minX = chunkIndex * CHUNK_SIZE;
        int maxX = (chunkIndex + 1) * CHUNK_SIZE;
        return terrain.createInRange(HelpingFunctions.alignHeight(minX, Block.SIZE),
                HelpingFunctions.alignHeight(maxX, Block.SIZE));
    }

    /**
     * Generates trees for a specific chunk based on its index.
     *
     * @param chunkIndex The index of the chunk to generate.
     * @return A list of Tree objects representing the trees in the chunk.
     */
    public List<Tree> generateTrees(int chunkIndex) {
        int minX = chunkIndex * CHUNK_SIZE;
        int maxX = (chunkIndex + 1) * CHUNK_SIZE;
        return flora.createInRange(
                HelpingFunctions.alignHeight(minX, Block.SIZE),
                HelpingFunctions.alignHeight(maxX, Block.SIZE));
    }

    /**
     * Retrieves the DayCycleFacade object, which manages the day-night cycle.
     *
     * @return The DayCycleFacade object.
     */
    public DayCycleFacade getDayCycleFacade() {
        return dayCycleFacade;
    }

    /**
     * Calculates the starting Y position for a given X-coordinate based on terrain height.
     *
     * @param x The X-coordinate.
     * @return The Y-coordinate of the ground.
     */
    public float getStartingPositionY(float x) {
        return terrain.groundHeightAt(x);
    }
}
