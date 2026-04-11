package pepse.world.daynight;

import danogl.GameObject;
import danogl.components.Transition;
import danogl.gui.rendering.OvalRenderable;
import danogl.util.Vector2;
import pepse.util.HelpingFunctions;
import pepse.world.Terrain;

import java.awt.*;
import java.math.BigDecimal;
/**
    * Represents the Sun in the game world.
    * The Sun serves as a light source and moves in a circular path in the sky.
 **/
abstract public class Sun {
    private static final OvalRenderable sunRenderable = new OvalRenderable(Color.YELLOW);
    private static final float SUN_SIZE = 70 ;

    /**
     * Creates a GameObject representing the Sun and makes it move in a circular path in the sky.
     * @param windowDimensions The dimensions of the game window.
     * @param cycleLength The length of one full day-night cycle in seconds.
     * @return The Sun GameObject.
     */
    public static GameObject create(Vector2 windowDimensions, float cycleLength) {
        // Initial position of the sun
        Vector2 initialSunCenter = new Vector2(windowDimensions.x() / 2, windowDimensions.y() / 2);
        // Center point of the circular orbit (at ground height)
        Vector2 cycleCenter = new Vector2(windowDimensions.x() / 2,
                Terrain.getGroundHeightAtX0()); // Example ground height
        // Create the Sun GameObject
        GameObject sun = new GameObject(
                initialSunCenter,
                new Vector2(SUN_SIZE, SUN_SIZE),
                sunRenderable
        );
        sun.setCoordinateSpace(danogl.components.CoordinateSpace.CAMERA_COORDINATES);
        // Add the Transition for circular movement
        new Transition<>(
                sun, // The GameObject being updated
                (Float angle) -> sun.setCenter(
                        initialSunCenter.subtract(cycleCenter) // Distance vector from cycle center
                                .rotated(angle) // Rotate by the current angle
                                .add(cycleCenter) // Add the cycle center to complete rotation
                ),
                0f, // Start angle (degrees)
                360f, // End angle (degrees)
                Transition.LINEAR_INTERPOLATOR_FLOAT, // Interpolation type
                cycleLength, // Time to complete a full cycle
                Transition.TransitionType.TRANSITION_LOOP, // Repeat indefinitely
                null // No callback when transition completes
        );
        return sun;
    }
}
