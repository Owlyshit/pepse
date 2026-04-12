package pepse.world.monsters;

import danogl.GameObject;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;

/**
 * Base class for all monsters in the world.
 */
public abstract class Monster extends GameObject {
    private static final String DEFAULT_TAG = "Monster";
    private float health;
    private final float maxHealth;
    private final int contactDamage;
    private final float moveSpeed;
    private boolean alive = true;
    /**
     * Creates a monster with common stats.
     *
     * @param topLeftCorner Spawn top-left corner.
     * @param dimensions Monster size.
     * @param renderable Monster sprite/renderable.
     * @param maxHealth Maximum health points.
     * @param contactDamage Damage dealt on contact.
     * @param moveSpeed Horizontal movement speed.
     */
    protected Monster(Vector2 topLeftCorner,
                      Vector2 dimensions,
                      Renderable renderable,
                      float maxHealth,
                      int contactDamage,
                      float moveSpeed) {
        super(topLeftCorner, dimensions, renderable);
        this.maxHealth = Math.max(1f, maxHealth);
        this.health = this.maxHealth;
        this.contactDamage = Math.max(0, contactDamage);
        this.moveSpeed = Math.max(0f, moveSpeed);
        setTag(DEFAULT_TAG);
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        if (!alive) {
            return;
        }
        updateBehavior(deltaTime);
    }

    /**
     * Monster-specific behavior called every frame while alive.
     *
     * @param deltaTime Frame delta time.
     */
    protected abstract void updateBehavior(float deltaTime);

    /**
     * Applies incoming damage and kills the monster when health reaches zero.
     *
     * @param amount Damage amount.
     */
    public void takeDamage(int amount) {
        if (!alive || amount <= 0) {
            return;
        }
        health = Math.max(0f, health - amount);
        if (health <= 0f) {
            die();
        }
    }

    /**
     * Stops the monster and marks it as dead.
     * Subclasses can override for custom death effects.
     */
    protected void die() {
        alive = false;
        transform().setVelocity(Vector2.ZERO);
    }

    /**
     * Makes the monster move toward a target x-position.
     *
     * @param targetX Target x coordinate.
     */
    protected void moveTowardX(float targetX) {
        float direction = Math.signum(targetX - getCenter().x());
        transform().setVelocityX(direction * moveSpeed);
        if (direction != 0f) {
            renderer().setIsFlippedHorizontally(direction < 0f);
        }
    }

    /**
     * Stops horizontal movement.
     */
    protected void stopHorizontalMovement() {
        transform().setVelocityX(0f);
    }

    @Override
    public boolean shouldCollideWith(GameObject other) {
        String tag = other.getTag();
        return tag != null && tag.startsWith("Block");
    }

    public float getHealth() {
        return health;
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    public int getContactDamage() {
        return contactDamage;
    }

    public float getMoveSpeed() {
        return moveSpeed;
    }

    public boolean isAlive() {
        return alive;
    }
}
