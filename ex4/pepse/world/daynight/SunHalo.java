package pepse.world.daynight;

import danogl.GameObject;
import danogl.components.CoordinateSpace;
import danogl.gui.rendering.OvalRenderable;
import danogl.util.Vector2;

import java.awt.*;
/**
    * Represents the sun halo in the game world.
 **/
public class SunHalo {
    private static final Color HALO_COLOR = new Color(255, 255, 0, 20);
    private static final OvalRenderable yellowOval = new OvalRenderable(HALO_COLOR);
    private static final float MULT_FACTOR = 1.5f;
    /**
    * Creates a sun halo object that spans the entire window.
     */
    public static GameObject create(GameObject sun) {
        GameObject sunHalo = new GameObject(
                sun.getCenter(), sun.getDimensions().mult(MULT_FACTOR),
                yellowOval);
        sunHalo.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        sunHalo.setTag("sunHalo");
        sunHalo.addComponent(deltaTime -> sunHalo.setCenter(sun.getCenter()));
        return sunHalo;
    }
}
