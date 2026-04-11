
package pepse.world.trees;

import danogl.GameObject;
import danogl.collisions.Collision;
import danogl.gui.rendering.OvalRenderable;
import danogl.util.Vector2;
import pepse.util.ColorSupplier;
import java.awt.*;
    /**
    * Represents a fruit in the game world.
    *  Fruits can restore health to the player upon interaction.
    **/
public class Fruit extends GameObject {
    /**
     * The size of the fruit (width and height).
     */
    public static final float FRUIT_SIZE = 15;

    /**
     * The default color of the fruit.
     */
    public static final Color FRUIT_COLOR = Color.RED;

    /**
     * A static OvalRenderable instance for rendering fruits with the specified color.
     */
    public static final OvalRenderable FRUIT_RENDERABLE
            = new OvalRenderable(ColorSupplier.approximateColor(FRUIT_COLOR));

    /**
     * The default health restoration value of the fruit.
     */
    private float restoreHealthVal = 10;

    /**
     * The position of the fruit in the game world.
     */
    private Vector2 pos;

    /**
     * Constructs a new Fruit instance.
     *
     * @param pos The position of the fruit in the game world.
     */
    public Fruit(Vector2 pos) {
        super(pos, Vector2.ONES.mult(FRUIT_SIZE), FRUIT_RENDERABLE);
        this.pos = pos;
        setTag("Fruit");
    }

    /**
     * Retrieves the health restoration value of the fruit.
     *
     * @return The health restoration value.
     */
    public float getRestoreHealthVal() {
        return restoreHealthVal;
    }

    /**
     * Retrieves the position of the fruit in the game world.
     *
     * @return The position of the fruit.
     */
    public Vector2 getPos() {
        return this.pos;
    }

    }
