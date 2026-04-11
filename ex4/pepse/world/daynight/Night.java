package pepse.world.daynight;

import danogl.GameObject;
import danogl.components.Transition;
import danogl.gui.rendering.RectangleRenderable;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;

import java.awt.*;
/**
    * Represents the night in the game world.
 **/
abstract public class Night  {
    /**
    * The opacity of the night object.
     **/
    private static final Float MIDNIGHT_OPACITY = 0.5f;
    private static final Float NIGHT_START =  0f;
/**
    * Creates a night object that spans the entire window.
    *
    * @param windowDimensions The dimensions of the game window.
    * @param cycleLength The length of the day-night cycle in seconds.
    * @return A GameObject representing the night.
 */
    public static GameObject create(Vector2 windowDimensions,
                                    float cycleLength){
        Renderable nightBlock = new RectangleRenderable(Color.BLACK);
        GameObject nightSettings = new GameObject(Vector2.ZERO, windowDimensions,nightBlock);
        nightSettings.setCoordinateSpace(danogl.components.CoordinateSpace.CAMERA_COORDINATES);
        nightSettings.setTag("night");
        new Transition<>(//<Float was in the text> a WRAPPER
                nightSettings,// the game object being changed
                nightSettings.renderer()::setOpaqueness, // the method to call
                NIGHT_START, // initial transition value
                MIDNIGHT_OPACITY, // final transition value
                Transition.CUBIC_INTERPOLATOR_FLOAT, //use a cubic interpolator
                cycleLength/2, // transition fully over half a day
                Transition.TransitionType.TRANSITION_BACK_AND_FORTH, // enum
                null); // callback to be called when we reach the final value
        return nightSettings;
    }
}
