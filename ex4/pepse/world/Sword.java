package pepse.world;

import danogl.GameObject;
import danogl.gui.ImageReader;
import danogl.util.Vector2;

import java.util.Random;

public class Sword extends GameObject implements Weapon {
    private enum AttackStyle {
        SLASH,
        STAB
    }
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
    private static final float STAB_READY_ANGLE = 0f;
    private static final float STAB_THRUST_ANGLE = 6f;
    private static final float STAB_RECOVERY_ANGLE = 10f;
    private static final float STAB_MAX_Y_OFFSET = 4f;
    private static final float STAB_MAX_X_OFFSET = 16f;
    private static final int DAMAGE = 10;
    private final Random random = new Random();
    private float animationTime = 0f;
    private float cooldownTimer = 0f;
    private float attackTimer = 0f;
    private float attackXOffset = 0f;
    private float attackYOffset = 0f;
    private boolean isAttacking = false;
    private int attackSequence = 0;
    private boolean facingRight = true;
    private AttackStyle currentAttackStyle = AttackStyle.SLASH;
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
     * @param animationName Current avatar animation state.
     * @param deltaTime Elapsed frame time.
     */
    private void updateAnimationState(Weapon.AvatarAnimation animationName, float deltaTime) {
        animationTime += deltaTime;
        switch (animationName) {
            case RUN:
                float swing = (float) Math.sin(animationTime * RUN_SWING_SPEED) * RUN_SWING_ANGLE;
                renderer().setRenderableAngle(facingRight ? swing : -swing);
                break;
            case JUMP:
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
        currentAttackStyle = random.nextBoolean() ? AttackStyle.SLASH : AttackStyle.STAB;
        isAttacking = true;
        attackSequence++;
        attackTimer = ATTACK_DURATION;
        cooldownTimer = ATTACK_COOLDOWN;
    }

    /**
     * Updates transform and animation from avatar-driven state.
     */
    @Override
    public void syncWithAvatar(Vector2 handPosition,
                               boolean facingRight,
                               Weapon.AvatarAnimation avatarAnimation,
                               float deltaTime) {
        setFacingDirection(facingRight);

        if (cooldownTimer > 0) {
            cooldownTimer = Math.max(0, cooldownTimer - deltaTime);
        }

        if (isAttacking) {
            updateAttackAnimation(deltaTime);
            float directionalXOffset = facingRight ? attackXOffset : -attackXOffset;
            setCenter(handPosition.add(new Vector2(directionalXOffset, attackYOffset)));
            return;
        }

        attackXOffset = 0f;
        attackYOffset = 0f;
        setCenter(handPosition);
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
        if (currentAttackStyle == AttackStyle.STAB) {
            updateStabAttack(progress);
        } else {
            updateSlashAttack(progress);
        }

        if (attackTimer <= 0) {
            isAttacking = false;
            attackXOffset = 0f;
            attackYOffset = 0f;
        }
    }

    private void updateSlashAttack(float progress) {
        float localAngle;

        if (progress < 0.5f) {
            localAngle = lerp(0f, ATTACK_MAX_SWING_ANGLE, progress * 2f);
        } else {
            localAngle = lerp(ATTACK_MAX_SWING_ANGLE, -ATTACK_RECOVERY_ANGLE, (progress - 0.5f) * 2f);
        }

        attackXOffset = 0f;
        attackYOffset = 0f;
        renderer().setRenderableAngle(facingRight ? localAngle : -localAngle);
    }

    private void updateStabAttack(float progress) {
        float localAngle;
        if (progress < 0.3f) {
            float phaseProgress = progress / 0.3f;
            localAngle = lerp(0f, STAB_READY_ANGLE, phaseProgress);
            attackXOffset = lerp(0f, -3f, phaseProgress);
            attackYOffset = lerp(0f, STAB_MAX_Y_OFFSET, phaseProgress);
        } else if (progress < 0.7f) {
            float phaseProgress = (progress - 0.3f) / 0.4f;
            localAngle = lerp(STAB_READY_ANGLE, STAB_THRUST_ANGLE, phaseProgress);
            attackXOffset = lerp(-3f, STAB_MAX_X_OFFSET, phaseProgress);
            attackYOffset = STAB_MAX_Y_OFFSET;
        } else {
            float phaseProgress = (progress - 0.7f) / 0.3f;
            localAngle = lerp(STAB_THRUST_ANGLE, -STAB_RECOVERY_ANGLE, phaseProgress);
            attackXOffset = lerp(STAB_MAX_X_OFFSET, 0f, phaseProgress);
            attackYOffset = lerp(STAB_MAX_Y_OFFSET, 0f, phaseProgress);
        }

        renderer().setRenderableAngle(facingRight ? localAngle : -localAngle);
    }

    private static float lerp(float start, float end, float t) {
        return start + (end - start) * t;
    }
    @Override
    public int getDamage() {
            return DAMAGE;
    }

    public boolean isAttacking() {
        return isAttacking;
    }

    public int getAttackSequence() {
        return attackSequence;
    }
}
