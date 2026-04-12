package pepse.world;

import danogl.GameObject;
import danogl.collisions.Collision;
import danogl.gui.ImageReader;
import danogl.gui.UserInputListener;
import danogl.gui.rendering.AnimationRenderable;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;

import java.awt.event.KeyEvent;
import java.util.*;
import java.util.function.Consumer;

/**
 * The Avatar class represents the player-controlled character in the game world.
 * It manages movement, animations, energy levels, and custom interactions with other game objects.
 */
public class Avatar extends GameObject {
    /**
     * Enum representing the different states of the avatar.
     */
    public enum AvatarState {
        /** The avatar is idle.*/
        IDLE,
        /** the avatar is running.*/
        RUNNING,
        /** the avatar is jumping*/
         JUMPING
    }
    /**
     * Fields
     */
    private AvatarState currentState = AvatarState.IDLE;
    private final Map<AvatarState, List<Runnable>> stateActions = new HashMap<>();
    private final Map<String, Consumer<GameObject>> parameterizedBehaviors = new HashMap<>();
    private final UserInputListener inputListener;
    private final Map<Weapon.AvatarAnimation, AnimationRenderable> animations;
    private final Weapon weapon;
    private Weapon.AvatarAnimation currentAnimation = Weapon.AvatarAnimation.IDLE;
    private boolean isFacingRight = true;
    private boolean wasAttackPressed = false;
    private float energy = 100;
    /**
     * Constants
     */
    private static final float WIDTH = 20;
    private static final float HEIGHT = 50;
    private static final float VELOCITY_X = 300;
    private static final float VELOCITY_Y = -300;
    private static final float GRAVITY = 300;
    private static final float MINIMUM_AMOUNT_OF_ENERGY_TO_MOVE = 0;
    private static final float MOVING_ENERGY_LOSS = -0.5f;
    private static final float REQUIRED_ENERGY_TO_JUMP = 10f;
    private static final float JUMPING_ENERGY_COST = -10f;
    private static final float REQUIRED_ENERGY_TO_ATTACK = 5f;
    private static final float ATTACK_ENERGY_COST = -5f;
    private static final float IDLE_ENERGY_REGEN = 1;
    private static final float HAND_OFFSET_X = 25f;
    private static final float HAND_OFFSET_Y = 26f;
    /**
     * Animation frame paths
     */
    private static final String[] IDLE_FRAME_PATHS = {
            "assets/idle_0.png", "assets/idle_1.png", "assets/idle_2.png", "assets/idle_3.png"
    };
    private static final String[] JUMP_FRAME_PATHS = {
            "assets/jump_0.png", "assets/jump_1.png", "assets/jump_2.png", "assets/jump_3.png"
    };
    private static final String[] RUN_FRAME_PATHS = {
            "assets/run_0.png", "assets/run_1.png", "assets/run_2.png", "assets/run_3.png"
    };
    /**
     * Constructs an Avatar object with initial position, user input listener, and animations.
     * @param pos           The initial position of the avatar.
     * @param inputListener The listener for user input, used for movement and interactions.
     * @param imageReader   Used to load image assets for avatar animations.
     */
    public Avatar(Vector2 pos, UserInputListener inputListener, ImageReader imageReader) {
        super(pos, new Vector2(WIDTH, HEIGHT), null);
        physics().preventIntersectionsFromDirection(Vector2.ZERO);
        transform().setAccelerationY(GRAVITY);
        this.inputListener = inputListener;

        animations = new HashMap<>();
        animations.put(Weapon.AvatarAnimation.IDLE, createAnimation(IDLE_FRAME_PATHS, imageReader, 0.1));
        animations.put(Weapon.AvatarAnimation.RUN, createAnimation(RUN_FRAME_PATHS, imageReader, 0.1));
        animations.put(Weapon.AvatarAnimation.JUMP, createAnimation(JUMP_FRAME_PATHS, imageReader, 0.1));
        renderer().setRenderable(animations.get(Weapon.AvatarAnimation.IDLE));
        weapon = new Sword(imageReader, getHandWorldPosition());
        setTag("Avatar");

        initializeStateActions();
    }

    /**
     * Initializes the actions associated with each state of the avatar.
     */
    private void initializeStateActions() {
        for (AvatarState state : AvatarState.values()) {
            stateActions.put(state, new ArrayList<>());
        }
    }

    /**
     * Adds a custom action to be executed when the avatar enters a specific state.
     *
     * @param state  The state for which the action is registered.
     * @param action The action to execute when the state is active.
     */
    public void addStateAction(AvatarState state, Runnable action) {
        stateActions.get(state).add(action);
    }

    /**
     * Updates the avatar's state, energy, movement, and animations based on user input and game physics.
     *
     * @param deltaTime The time elapsed since the last update.
     */
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        float xVel = 0;
        boolean leftPressed = inputListener.isKeyPressed(KeyEvent.VK_LEFT);
        boolean rightPressed = inputListener.isKeyPressed(KeyEvent.VK_RIGHT);
        boolean spacePressed = inputListener.isKeyPressed(KeyEvent.VK_SPACE);
        boolean attackPressed = inputListener.isKeyPressed(KeyEvent.VK_X);

        if (leftPressed && energy > MINIMUM_AMOUNT_OF_ENERGY_TO_MOVE) {
            xVel -= VELOCITY_X;
            renderer().setIsFlippedHorizontally(true);
            isFacingRight = false;
            setAnimation(Weapon.AvatarAnimation.RUN);
            addEnergy(MOVING_ENERGY_LOSS * deltaTime);
            currentState = AvatarState.RUNNING;
        }
        if (rightPressed && energy > MINIMUM_AMOUNT_OF_ENERGY_TO_MOVE) {
            xVel += VELOCITY_X;
            renderer().setIsFlippedHorizontally(false);
            isFacingRight = true;
            setAnimation(Weapon.AvatarAnimation.RUN);
            addEnergy(MOVING_ENERGY_LOSS * deltaTime);
            currentState = AvatarState.RUNNING;
        }
        transform().setVelocityX(xVel);
        if (spacePressed && getVelocity().y() == 0 && energy >= REQUIRED_ENERGY_TO_JUMP) {
            transform().setVelocityY(VELOCITY_Y);
            addEnergy(JUMPING_ENERGY_COST);
            setAnimation(Weapon.AvatarAnimation.JUMP);
            executeStateActions(AvatarState.JUMPING);
            currentState = AvatarState.JUMPING;
        }
        if (getVelocity().y() == 0 && xVel == 0) {
            setAnimation(Weapon.AvatarAnimation.IDLE);
            addEnergy(IDLE_ENERGY_REGEN * deltaTime);
            currentState = AvatarState.IDLE;
        }
        if (attackPressed && !wasAttackPressed && energy >= REQUIRED_ENERGY_TO_ATTACK) {
            weapon.triggerAttack();
            addEnergy(ATTACK_ENERGY_COST);
            
        }
        wasAttackPressed = attackPressed;

        weapon.syncWithAvatar(getHandWorldPosition(), isFacingRight, currentAnimation, deltaTime);
    }

    /**
     * Ensures energy stays within the range [0, 100].
     *
     * @param energyToAdd The amount of energy to add (can be negative).
     */
    public void addEnergy(float energyToAdd) {
        energy = Math.min(Math.max(energy + energyToAdd, 0), 100);
    }

    /**
     * Executes all actions associated with the specified avatar state.
     *
     * @param state The state for which the actions are executed.
     */
    private void executeStateActions(AvatarState state) {
        List<Runnable> actions = stateActions.get(state);
        for (Runnable action : actions) {
            action.run();
        }
    }

    /**
     * Adds a parameterized behavior to handle specific interactions.
     * @param behaviorName The name/key of the behavior.
     * @param behavior The Consumer that defines the behavior.
     */
    public void addParameterizedBehavior(String behaviorName, Consumer<GameObject> behavior) {
        parameterizedBehaviors.put(behaviorName, behavior);
    }
    /**
     * Retrieves the current energy level of the avatar.
     *
     * @return The energy level.
     */
    public float getEnergy() {
        return energy;
    }

    /**
     * Gets the equipped weapon game object.
     *
     * @return The equipped weapon as a game object.
     */
    public GameObject getWeaponObject() {
        return weapon.asGameObject();
    }

    /**
     * Indicates the current facing direction of the avatar.
     *
     * @return true if facing right, false if facing left.
     */
    public boolean isFacingRight() {
        return isFacingRight;
    }

    /**
     * Computes the world position of the avatar hand anchor.
     *
     * @return Hand anchor position in world coordinates.
     */
    public Vector2 getHandWorldPosition() {
        Vector2 avatarTopLeft = getTopLeftCorner();
        float handX = isFacingRight
                ? avatarTopLeft.x() + HAND_OFFSET_X
                : avatarTopLeft.x() + getDimensions().x() - HAND_OFFSET_X;
        float handY = avatarTopLeft.y() + HAND_OFFSET_Y;
        return new Vector2(handX, handY);
    }

    /**
     * Creates an animation from a set of image file paths.
     *
     * @param framePaths   The file paths of the animation frames.
     * @param imageReader  Used to load images from the file paths.
     * @param frameDuration The duration of each animation frame.
     * @return The constructed AnimationRenderable object.
     */
    private AnimationRenderable createAnimation(String[] framePaths,
                                                ImageReader imageReader,
                                                double frameDuration) {
        Renderable[] frames = new Renderable[framePaths.length];
        for (int i = 0; i < framePaths.length; i++) {
            frames[i] = imageReader.readImage(framePaths[i], true);
        }
        return new AnimationRenderable(frames, frameDuration);
    }

    /**
     * Sets the current animation to the specified animation name.
     *
     * @param animationName The name of the animation to set.
     */
    private void setAnimation(Weapon.AvatarAnimation animationName) {
        if (!currentAnimation.equals(animationName)) {
            renderer().setRenderable(animations.get(animationName));
            currentAnimation = animationName;
        }
    }

    /**
     * Handles collision events when the avatar collides with another game object.
     *
     * @param other     The game object involved in the collision.
     * @param collision The collision information.
     */
    @Override
    public void onCollisionEnter(GameObject other, Collision collision) {
        if (other.getTag().startsWith("Block")) {
            transform().setVelocityY(0);
            return;
        }
        if (parameterizedBehaviors.containsKey(other.getTag())) {
            parameterizedBehaviors.get(other.getTag()).accept(other);
        }
    }
    /**
     * Registers an action to be executed for a specific avatar state.
     *
     * @param state  The state for which the action is registered.
     * @param action The action to execute when the state is active.
     */
    public void registerStateAction(AvatarState state, Runnable action) {
        addStateAction(state, action);
    }
}
