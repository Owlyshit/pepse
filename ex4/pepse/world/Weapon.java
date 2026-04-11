package pepse.world;

import danogl.GameObject;
import danogl.util.Vector2;

/**
 * Weapon contract: avatar triggers intents, weapon executes behavior.
 */
public interface Weapon {
    /**
     * Triggers an attack intent.
     */
    void triggerAttack();

    /**
     * Syncs weapon transform and animation with the avatar each frame.
     *
     * @param handPosition Avatar hand anchor position.
     * @param facingRight true if avatar faces right.
     * @param avatarAnimation Current avatar animation state name.
     * @param deltaTime Frame delta time.
     */
    void syncWithAvatar(Vector2 handPosition,
                        boolean facingRight,
                        String avatarAnimation,
                        float deltaTime);

    /**
     * Exposes the weapon as a game object for scene registration.
     *
     * @return Weapon game object.
     */
    GameObject asGameObject();
}
