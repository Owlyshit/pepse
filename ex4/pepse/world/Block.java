
package pepse.world;

import danogl.*;
import danogl.collisions.Collision;
import danogl.components.GameObjectPhysics;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;
/**
 * Represents a block in the game world. Blocks are immovable objects
 * that act as barriers or platforms within the game.
 */
public class Block extends GameObject {
















    /**
     * The size of the block, used for both width and height.
     */
    public static final int SIZE = 30;

    /**
     * Constructs a new Block instance.
     *
     * @param topLeftCorner The top-left corner of the block's position.
     * @param renderable    The renderable representing the block's appearance.
     */
    public Block(Vector2 topLeftCorner, Renderable renderable) {
        super(topLeftCorner, Vector2.ONES.mult(SIZE), renderable);

        // Prevent the block from being intersected by other objects.
        physics().preventIntersectionsFromDirection(Vector2.ZERO);

        // Set the block's mass to be immovable.
        physics().setMass(GameObjectPhysics.IMMOVABLE_MASS);

        // Assign a tag to the block for identification.
        setTag("Block");
    }
}