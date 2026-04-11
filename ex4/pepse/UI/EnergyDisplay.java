package pepse.UI;

import danogl.GameObject;
import danogl.components.CoordinateSpace;
import danogl.gui.rendering.TextRenderable;
import danogl.util.Vector2;

import java.awt.*;
import java.util.function.Supplier;

/**
 * A UI component to display the energy of the avatar.
 */
public class EnergyDisplay extends GameObject {
    private final Supplier<Float> energySupplier;
    private final TextRenderable textRenderable;
    private static final int SIZE = 50;
    /**
     * Constructs a new EnergyDisplay instance.
     *
     * @param topLeftCorner The top-left corner of the energy display.
     * @param energySupplier A supplier for the energy value to be displayed.
     */
    public EnergyDisplay(Vector2 topLeftCorner, Supplier<Float> energySupplier) {
        super(topLeftCorner, new Vector2(SIZE,SIZE), null);
        this.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        this.energySupplier = energySupplier;
        textRenderable = new TextRenderable((String.valueOf(SIZE)),"Arial");
        textRenderable.setColor(Color.WHITE); // For dark backgrounds
        renderer().setRenderable(textRenderable);
    }
    /**
        * Updates the energy display with the current energy value.
        *
        * @param deltaTime The time elapsed since the last update.
    */
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        float energy = energySupplier.get();
        textRenderable.setString("Energy: " + (int) energy); // Round down and display
    }
}
