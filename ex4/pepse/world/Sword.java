package pepse.world;

import danogl.GameObject;
import danogl.gui.ImageReader;
import danogl.util.Vector2;

/**
 * A temporary square sword placeholder that stays attached to the avatar hand.
 */
public class Sword extends GameObject implements Weapon {
    private static final float SIZE = 25f;
    private static final String SWORD_PATH = "assets/sword.png";
    private static final float IDLE_ANGLE = 0f;
    private static final float RUN_SWING_SPEED = 10f;
    private static final float RUN_SWING_ANGLE = 16f;
    private static final float JUMP_ANGLE = -30f;
    private static final float ATTACK_COOLDOWN = 0.35f;
    private static final float ATTACK_DURATION = 0.18f;
    private static final float ATTACK_MAX_SWING_ANGLE = 65f;
    private static final float ATTACK_RECOVERY_ANGLE = 20f;
    private float animationTime = 0f;
    private float cooldownTimer = 0f;
    private float attackTimer = 0f;
    private boolean isAttacking = false;
    private boolean facingRight = true;
    /**
     * Creates a sword object at an initial hand anchor position.
     *
     * @param imageReader Used for loading the sword image.
     * @param initialHandPosition The sword holder hand position.
     */
    public Sword(ImageReader imageReader, Vector2 initialHandPosition) {
        super(initialHandPosition.subtract(Vector2.ONES.mult(SIZE / 2f)),
                Vector2.ONES.mult(SIZE),
                imageReader.readImage(SWORD_PATH, true));
        setTag("Sword");
    }

    /**
     * Flips the sword image according to holder facing direction.
     *
     * @param facingRight true when holder faces right, false when faces left.
     */
    private void setFacingDirection(boolean facingRight) {
        this.facingRight = facingRight;
        renderer().setIsFlippedHorizontally(!facingRight);
    }

    /**
     * Updates sword orientation based on avatar movement state.
     *
     * @param animationName Current avatar animation name (idle/run/jump).
     * @param deltaTime Elapsed frame time.
     */
    private void updateAnimationState(String animationName, float deltaTime) {
        animationTime += deltaTime;
        switch (animationName) {
            case "run":
                float swing = (float) Math.sin(animationTime * RUN_SWING_SPEED) * RUN_SWING_ANGLE;
                renderer().setRenderableAngle(facingRight ? swing : -swing);
                break;
            case "jump":
                renderer().setRenderableAngle(facingRight ? JUMP_ANGLE : -JUMP_ANGLE);
                break;
            default:
                renderer().setRenderableAngle(IDLE_ANGLE);
                break;
        }
    }

    /**
     * Triggers an attack if cooldown allows it.
     */
    @Override
    public void triggerAttack() {
        if (cooldownTimer > 0 || isAttacking) {
            return;
        }
        isAttacking = true;
        attackTimer = ATTACK_DURATION;
        cooldownTimer = ATTACK_COOLDOWN;
    }

    /**
     * Updates transform and animation from avatar-driven state.
     */
    @Override
    public void syncWithAvatar(Vector2 handPosition,
                               boolean facingRight,
                               String avatarAnimation,
                               float deltaTime) {
        setCenter(handPosition);
        setFacingDirection(facingRight);

        if (cooldownTimer > 0) {
            cooldownTimer = Math.max(0, cooldownTimer - deltaTime);
        }

        if (isAttacking) {
            updateAttackAnimation(deltaTime);
            return;
        }

        updateAnimationState(avatarAnimation, deltaTime);
    }

    /**
     * Exposes this sword object through the Weapon abstraction.
     */
    @Override
    public GameObject asGameObject() {
        return this;
    }

    private void updateAttackAnimation(float deltaTime) {
        attackTimer -= deltaTime;
        float progress = 1f - Math.max(0, attackTimer) / ATTACK_DURATION;
        float localAngle;

        if (progress < 0.5f) {
            localAngle = lerp(0f, ATTACK_MAX_SWING_ANGLE, progress * 2f);
        } else {
            localAngle = lerp(ATTACK_MAX_SWING_ANGLE, -ATTACK_RECOVERY_ANGLE, (progress - 0.5f) * 2f);
        }

        renderer().setRenderableAngle(facingRight ? localAngle : -localAngle);

        if (attackTimer <= 0) {
            isAttacking = false;
        }
    }

    private static float lerp(float start, float end, float t) {
        return start + (end - start) * t;
    }
    @Override
    public int getDamage() {
            // TODO Auto-generated method stub
            return 0;
    }
}
