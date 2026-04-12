package pepse.world.monsters;

import danogl.GameObject;
import danogl.collisions.Collision;
import danogl.gui.ImageReader;
import danogl.gui.rendering.AnimationRenderable;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;
import pepse.world.Sword;

import java.util.Random;

public class GreenSlime extends Monster {
    private static final String IDLE_0_PATH = "assets/green_idle0.png";
    private static final String IDLE_1_PATH = "assets/green_idle1.png";
    private static final String HIT_PATH = "assets/green_hit.png";
    private static final double IDLE_FRAME_DURATION = 0.22;
    private static final float HIT_DISPLAY_DURATION = 0.28f;
    private static final float GRAVITY = 300f;
    private static final float JUMP_VELOCITY = -230f;
    private static final float MIN_MOVE_DURATION = 1.1f;
    private static final float MAX_MOVE_DURATION = 2.4f;
    private static final float MIN_IDLE_DURATION = 0.25f;
    private static final float MAX_IDLE_DURATION = 0.8f;
    private static final float OBSTACLE_RESPONSE_COOLDOWN = 0.2f;

    // Sword deals 10 damage, so 20 health means the slime dies after two hits.
    private static final float MAX_HEALTH = 20f;
    private static final int CONTACT_DAMAGE = 3;
    private static final float MOVE_SPEED = 50f;

    private final AnimationRenderable idleAnimation;
    private final Renderable hitRenderable;
    private final Sword trackedSword;
    private final Random random = new Random();
    private float hitTimer = 0f;
    private float movementTimer = 0f;
    private float obstacleResponseCooldown = 0f;
    private boolean idlePhase = false;
    private int horizontalDirection = 1;
    private int lastRegisteredAttackSequence = -1;

    public GreenSlime(Vector2 topLeftCorner, Vector2 dimensions, ImageReader imageReader, Sword trackedSword) {
        super(topLeftCorner,
                dimensions,
                imageReader.readImage(IDLE_0_PATH, true),
                MAX_HEALTH,
                CONTACT_DAMAGE,
                MOVE_SPEED);
        this.trackedSword = trackedSword;
        idleAnimation = new AnimationRenderable(
                new Renderable[]{
                        imageReader.readImage(IDLE_0_PATH, true),
                        imageReader.readImage(IDLE_1_PATH, true)
                },
                IDLE_FRAME_DURATION);
        hitRenderable = imageReader.readImage(HIT_PATH, true);
        renderer().setRenderable(idleAnimation);
        physics().preventIntersectionsFromDirection(Vector2.ZERO);
        transform().setAccelerationY(GRAVITY);
        movementTimer = randomMoveDuration();
        setTag("GreenSlime");
    }

    @Override
    protected void updateBehavior(float deltaTime) {
        processSwordHit();
        obstacleResponseCooldown = Math.max(0f, obstacleResponseCooldown - deltaTime);

        movementTimer -= deltaTime;
        if (movementTimer <= 0f) {
            idlePhase = !idlePhase;
            movementTimer = idlePhase ? randomIdleDuration() : randomMoveDuration();
            if (!idlePhase) {
                horizontalDirection *= -1;
            }
        }

        if (idlePhase) {
            stopHorizontalMovement();
        } else {
            moveTowardX(getCenter().x() + horizontalDirection);
        }

        if (hitTimer > 0f) {
            hitTimer -= deltaTime;
            renderer().setRenderable(hitRenderable);
            return;
        }

        renderer().setRenderable(idleAnimation);
    }

    @Override
    public void onCollisionEnter(GameObject other, Collision collision) {
        if (!isAlive()) {
            return;
        }

        if (other.getTag().startsWith("Block")) {
            // Ignore floor/ceiling collisions. We only respond to side obstacles.
            if (Math.abs(collision.getNormal().x()) < 0.1f || obstacleResponseCooldown > 0f) {
                return;
            }

            obstacleResponseCooldown = OBSTACLE_RESPONSE_COOLDOWN;

            // If grounded, try to hop over the obstacle; otherwise turn around.
            if (Math.abs(getVelocity().y()) < 1f) {
                transform().setVelocityY(JUMP_VELOCITY);
            } else {
                horizontalDirection *= -1;
                if (!idlePhase) {
                    movementTimer = randomMoveDuration();
                }
            }
        }
    }

    private void processSwordHit() {
        if (trackedSword == null || !trackedSword.isAttacking() || !isOverlapping(trackedSword)) {
            return;
        }

        int currentAttackSequence = trackedSword.getAttackSequence();
        if (currentAttackSequence == lastRegisteredAttackSequence) {
            return;
        }

        lastRegisteredAttackSequence = currentAttackSequence;
        hitTimer = HIT_DISPLAY_DURATION;
        takeDamage(trackedSword.getDamage());
    }

    private boolean isOverlapping(GameObject other) {
        Vector2 thisTopLeft = getTopLeftCorner();
        Vector2 thisDimensions = getDimensions();
        Vector2 thisBottomRight = thisTopLeft.add(thisDimensions);

        Vector2 otherTopLeft = other.getTopLeftCorner();
        Vector2 otherDimensions = other.getDimensions();
        Vector2 otherBottomRight = otherTopLeft.add(otherDimensions);

        return thisTopLeft.x() < otherBottomRight.x()
                && thisBottomRight.x() > otherTopLeft.x()
                && thisTopLeft.y() < otherBottomRight.y()
                && thisBottomRight.y() > otherTopLeft.y();
    }

    private float randomMoveDuration() {
        return MIN_MOVE_DURATION + random.nextFloat() * (MAX_MOVE_DURATION - MIN_MOVE_DURATION);
    }

    private float randomIdleDuration() {
        return MIN_IDLE_DURATION + random.nextFloat() * (MAX_IDLE_DURATION - MIN_IDLE_DURATION);
    }
}
