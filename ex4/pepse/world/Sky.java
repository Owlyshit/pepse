
package pepse.world;

import danogl.GameObject;
import danogl.components.CoordinateSpace;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;

import java.awt.*;
    /**
    * Represents the sky in the game world. The sky serves as a background element
     */
public class Sky {
    /**
     * The basic color of the sky.
     */
    private static final Color BASIC_SKY_COLOR = Color.decode("#80C6E5");

    /**
     * Creates a sky object that spans the entire window.
     *
     * @param windowDimensions The dimensions of the game window.
     * @return A GameObject representing the sky.
     */
    public static GameObject create(Vector2 windowDimensions) {
        GameObject sky = new GameObject(
                Vector2.ZERO, windowDimensions,
                new RectangleRenderable(BASIC_SKY_COLOR));

        // Set the sky to be rendered in the camera's coordinate space
        sky.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);

        // Assign a tag to the sky for identification
        sky.setTag("sky");

        return sky;
    }
}
