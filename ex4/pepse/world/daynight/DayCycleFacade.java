package pepse.world.daynight;

import danogl.GameObject;
import danogl.util.Vector2;
import pepse.world.Block;

import java.util.List;

/**
 * A facade class to manage the day-night cycle components including the sun,
 * night overlay, and sun halo.
 * This class provides a unified interface for creating and
 * accessing these components.
 */
public class DayCycleFacade {
    private GameObject sun;
    private GameObject night;
    private GameObject sunHalo;

    /**
     * Constructs a DayCycleFacade object and initializes the sun, night, and sun halo components.
     *
     * @param windowDimensions The dimensions of the game window.
     * @param cycleLength      The length of the complete day-night cycle in seconds.
     */
    public DayCycleFacade(Vector2 windowDimensions, float cycleLength) {
        this.sun = createSun(windowDimensions, cycleLength / 2);
        this.night = createNight(windowDimensions, cycleLength);
        this.sunHalo = createSunHalo();
    }

    /**
     * Factory method for creating the sun object. Can be overridden for customization.
     *
     * @param windowDimensions The dimensions of the game window.
     * @param cycleLength      The length of the sun's cycle in seconds.
     * @return A GameObject representing the sun.
     */
    protected GameObject createSun(Vector2 windowDimensions, float cycleLength) {
        return Sun.create(windowDimensions, cycleLength);
    }

    /**
     * Factory method for creating the night overlay object. Can be overridden for customization.
     *
     * @param windowDimensions The dimensions of the game window.
     * @param cycleLength      The length of the night overlay's cycle in seconds.
     * @return A GameObject representing the night overlay.
     */
    protected GameObject createNight(Vector2 windowDimensions, float cycleLength) {
        return Night.create(windowDimensions, cycleLength / 2);
    }

    /**
     * Factory method for creating the sun halo object.
     * Can be overridden for customization.
     * @return A GameObject representing the sun halo.
     */
    protected GameObject createSunHalo() {
        return SunHalo.create(this.sun);
    }

    /**
     * Gets the sun object.
     *
     * @return A GameObject representing the sun.
     */
    public GameObject getSun() {
        return sun;
    }

    /**
     * Gets the night overlay object.
     *
     * @return A GameObject representing the night overlay.
     */
    public GameObject getNight() {
        return night;
    }

    /**
     * Gets the sun halo object.
     *
     * @return A GameObject representing the sun halo.
     */
    public GameObject getSunHalo() {
        return sunHalo;
    }
}
