package pepse;

import danogl.GameManager;
import danogl.GameObject;
import danogl.collisions.Layer;
import danogl.components.ScheduledTask;
import danogl.gui.ImageReader;
import danogl.gui.SoundReader;
import danogl.gui.UserInputListener;
import danogl.gui.WindowController;
import danogl.gui.rendering.Camera;
import danogl.util.Vector2;
import pepse.UI.EnergyDisplay;
import pepse.world.Avatar;
import pepse.world.Block;
import pepse.world.Cloud;
import pepse.world.Sky;
import pepse.world.Sword;
import pepse.world.WorldManager;
import pepse.world.monsters.GreenSlime;
import pepse.world.monsters.Monster;
import pepse.world.trees.Fruit;
import pepse.world.trees.Tree;
import java.util.*;

/**
 * The PepseGameManager class orchestrates the game logic for the Pepse simulation game.
 * It initializes the world, handles the avatar, and manages dynamic chunk loading.
 */
public class PepseGameManager extends GameManager {
    /**Represents the layer index for throughable objects.*/
    public static final int LAYER_THROUGHABLE = -50;
    /**Represents the layer index for default objects.*/
    public static final Vector2 WINDOW_SIZE = new Vector2(1200, 600);
    /**Represents the name of the game.*/
    public static final String NAME_OF_GAME = "Simulator";
    /**Represents the world manager.*/
    private WorldManager worldManager;
    private ImageReader imageReader;
    /**Represents the player character.*/
    private Avatar avatar;
    private final int AVATAR_SPAWN_OFFSET = 50;
    /**Represents the loaded chunk indices.*/
    private final Set<Integer> loadedChunkIndices = new HashSet<>();
    /**Represents the number of chunks visible around the avatar.*/
    private static final int VISIBLE_CHUNKS = 3;
    /**Represents the map of chunk data.*/
    private final Map<Integer, ChunkData> chunkDataMap = new HashMap<>(); // Stores loaded chunk data.
    private static final int MAX_SLIMES_PER_CHUNK = 2;
    private static final float SLIME_SPAWN_CHANCE = 0.55f;
    private static final float SLIME_SIZE = 36f;
    private static final float MONSTER_RESPAWN_DELAY = WorldManager.CYCLE_LENGTH;
    private static final float MONSTER_CONTACT_DAMAGE_COOLDOWN = 0.5f;
    private final Set<Monster> pendingMonsterRespawns = new HashSet<>();
    private final Map<Monster, Float> monsterDamageCooldowns = new HashMap<>();

    /**
     * Main entry point for the game. Initializes the game manager and starts the game loop.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        new PepseGameManager().run();
    }

    /**
     * Constructs a new instance of PepseGameManager with default settings.
     */
    PepseGameManager() {
        super(NAME_OF_GAME, WINDOW_SIZE);
    }

    /**
     * Initializes the game by setting up core components, including the world, avatar, and camera.
     *
     * @param imageReader      Loads images for rendering.
     * @param soundReader      Loads sound files for the game.
     * @param inputListener    Listens for user input.
     * @param windowController Manages window-related operations.
     */
    @Override
    public void initializeGame(
            ImageReader imageReader,
            SoundReader soundReader,
            UserInputListener inputListener,
            WindowController windowController
    ) {
        super.initializeGame(imageReader, soundReader, inputListener, windowController);
        this.imageReader = imageReader;
        windowController.setTargetFramerate(60);
        worldManager = new WorldManager(windowController.getWindowDimensions());
        float initialSpawnX = windowController.getWindowDimensions().x() / 2;
        float groundHeight = worldManager.getStartingPositionY(initialSpawnX);
        float avatarSpawnHeight = groundHeight - AVATAR_SPAWN_OFFSET;
        Vector2 avatarPos = new Vector2(initialSpawnX, avatarSpawnHeight);
        avatar = new Avatar(avatarPos, inputListener, imageReader);
        avatar.registerStateAction(Avatar.AvatarState.JUMPING, this::handleTearDrops);
        avatar.addParameterizedBehavior("Fruit", fruit -> handleCollisionAvatarFruit(avatar, (Fruit) fruit));
        gameObjects().addGameObject(avatar, Layer.DEFAULT);
        gameObjects().addGameObject(avatar.getWeaponObject(), Layer.DEFAULT);
        gameObjects().addGameObject(Sky.create(windowController.getWindowDimensions()), Layer.BACKGROUND);
        setCamera(new Camera(avatar, Vector2.ZERO,
                windowController.getWindowDimensions(), windowController.getWindowDimensions()));
        EnergyDisplay energyDisplay = new EnergyDisplay(Vector2.LEFT, avatar::getEnergy);
        gameObjects().addGameObject(energyDisplay, Layer.UI);
        int initialChunk = getChunkIndex(initialSpawnX);
        loadChunksAround(initialChunk);
    }
    /**
     * Updates the game state on each frame. Handles dynamic chunk loading and avatar tracking.
     *
     * @param deltaTime Time elapsed since the last frame.
     */
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        Vector2 avatarPosition = avatar.getCenter();
        int currentChunk = getChunkIndex(avatarPosition.x());
        loadChunksAround(currentChunk);
        handleMonsterRespawns();
        handleAvatarMonsterContactDamage(deltaTime);
    }
    /**
     * Creates raindrops when the avatar is in the jumping state.
     */
    private void handleTearDrops() {
        Cloud.createRain(this::removeObject).forEach(rainDrop ->
                gameObjects().addGameObject(rainDrop, Layer.BACKGROUND)
        );
    }

    /**
     * Removes a specified GameObject from the game world.
     *
     * @param object The GameObject to be removed.
     */
    public void removeObject(GameObject object) {
        gameObjects().removeGameObject(object);
    }

    /**
     * Adds world objects, including terrain and trees, to the game.
     *
     * @param worldManager  The manager responsible for generating world objects.
     * @param terrainBlocks List of terrain blocks to add.
     * @param trees         List of trees to add.
     * @param chunkIndex    The index of the chunk being added.
     */
    private void addWorldObjects
    (WorldManager worldManager,
     List<GameObject> terrainBlocks,
     List<Tree> trees,
     List<Monster> monsters,
     int chunkIndex) {
        if (loadedChunkIndices.isEmpty()) {
            gameObjects().addGameObject
                    (worldManager.getDayCycleFacade().getNight(), Layer.FOREGROUND);
            gameObjects().addGameObject
                    (worldManager.getDayCycleFacade().getSun(), Layer.BACKGROUND);
            gameObjects().addGameObject
                    (worldManager.getDayCycleFacade().getSunHalo(), Layer.BACKGROUND);
            Cloud.create(0, 30).getCloudBlocks().forEach(cloudBlock ->
                    gameObjects().addGameObject(cloudBlock, Layer.BACKGROUND));
        }

        for (GameObject block : terrainBlocks) {
            gameObjects().addGameObject(block, Layer.STATIC_OBJECTS);
        }

        for (Tree tree : trees) {
            for (GameObject treeBlock : tree.getTreeLogBlocks()) {
                gameObjects().addGameObject(treeBlock, Layer.STATIC_OBJECTS);
            }
            for (GameObject leafBlock : tree.getTreeLeafBlocks()) {
                gameObjects().addGameObject(leafBlock, LAYER_THROUGHABLE);
            }
            for (Fruit fruit : tree.getFruits()) {
                gameObjects().addGameObject(fruit, Layer.DEFAULT);
            }
        }

        for (Monster monster : monsters) {
            gameObjects().addGameObject(monster, Layer.DEFAULT);
        }

        chunkDataMap.put(chunkIndex, new ChunkData(terrainBlocks, trees, monsters));
    }

    private List<Monster> generateRandomMonsters(int chunkIndex) {
        List<Monster> monsters = new ArrayList<>();
        int minX = chunkIndex * WorldManager.CHUNK_SIZE;
        int maxX = (chunkIndex + 1) * WorldManager.CHUNK_SIZE;
        Random chunkRandom = new Random(10007L * chunkIndex + 17L);
        Sword playerSword = (Sword) avatar.getWeaponObject();

        for (int i = 0; i < MAX_SLIMES_PER_CHUNK; i++) {
            if (chunkRandom.nextFloat() > SLIME_SPAWN_CHANCE) {
                continue;
            }
            float spawnX = minX + chunkRandom.nextFloat() * (maxX - minX);
            float spawnY = worldManager.getStartingPositionY(spawnX) - SLIME_SIZE;
            monsters.add(new GreenSlime(
                    new Vector2(spawnX, spawnY),
                    new Vector2(SLIME_SIZE, SLIME_SIZE),
                    imageReader,
                    playerSword));
        }

        return monsters;
    }

    private Monster createRespawnedMonster(Monster monster, Vector2 topLeftCorner, Vector2 dimensions) {
        if (monster instanceof GreenSlime) {
            return new GreenSlime(topLeftCorner,
                    dimensions,
                    imageReader,
                    (Sword) avatar.getWeaponObject());
        }
        return null;
    }

    private void handleMonsterRespawns() {
        for (Map.Entry<Integer, ChunkData> entry : chunkDataMap.entrySet()) {
            int chunkIndex = entry.getKey();
            ChunkData chunkData = entry.getValue();
            List<Monster> monstersSnapshot = new ArrayList<>(chunkData.monsters);

            for (Monster monster : monstersSnapshot) {
                if (monster.isAlive() || pendingMonsterRespawns.contains(monster)) {
                    continue;
                }

                pendingMonsterRespawns.add(monster);
                gameObjects().removeGameObject(monster, Layer.DEFAULT);
                Vector2 respawnTopLeft = monster.getTopLeftCorner();
                Vector2 respawnDimensions = monster.getDimensions();

                new ScheduledTask(avatar, MONSTER_RESPAWN_DELAY, false, () -> {
                    pendingMonsterRespawns.remove(monster);
                    if (!loadedChunkIndices.contains(chunkIndex)) {
                        return;
                    }

                    Monster respawnedMonster =
                            createRespawnedMonster(monster, respawnTopLeft, respawnDimensions);
                    if (respawnedMonster == null) {
                        return;
                    }

                    chunkData.monsters.remove(monster);
                    chunkData.monsters.add(respawnedMonster);
                    gameObjects().addGameObject(respawnedMonster, Layer.DEFAULT);
                });
            }
        }
    }

    private void handleAvatarMonsterContactDamage(float deltaTime) {
        List<Monster> staleMonsters = new ArrayList<>();
        for (Map.Entry<Monster, Float> entry : monsterDamageCooldowns.entrySet()) {
            Monster monster = entry.getKey();
            if (!isMonsterManaged(monster)) {
                staleMonsters.add(monster);
                continue;
            }

            float nextCooldown = Math.max(0f, entry.getValue() - deltaTime);
            entry.setValue(nextCooldown);
        }

        for (Monster staleMonster : staleMonsters) {
            monsterDamageCooldowns.remove(staleMonster);
        }

        for (ChunkData chunkData : chunkDataMap.values()) {
            for (Monster monster : chunkData.monsters) {
                if (!monster.isAlive() || !isOverlapping(avatar, monster)) {
                    continue;
                }

                float cooldown = monsterDamageCooldowns.getOrDefault(monster, 0f);
                if (cooldown > 0f) {
                    continue;
                }

                avatar.addEnergy(-monster.getContactDamage());
                monsterDamageCooldowns.put(monster, MONSTER_CONTACT_DAMAGE_COOLDOWN);
            }
        }
    }

    private boolean isMonsterManaged(Monster monster) {
        for (ChunkData chunkData : chunkDataMap.values()) {
            if (chunkData.monsters.contains(monster)) {
                return true;
            }
        }
        return false;
    }

    private boolean isOverlapping(GameObject first, GameObject second) {
        Vector2 firstTopLeft = first.getTopLeftCorner();
        Vector2 firstBottomRight = firstTopLeft.add(first.getDimensions());
        Vector2 secondTopLeft = second.getTopLeftCorner();
        Vector2 secondBottomRight = secondTopLeft.add(second.getDimensions());

        return firstTopLeft.x() < secondBottomRight.x()
                && firstBottomRight.x() > secondTopLeft.x()
                && firstTopLeft.y() < secondBottomRight.y()
                && firstBottomRight.y() > secondTopLeft.y();
    }

    /**
     *  Handles the collision between the avatar and a fruit object.
     *
     */
    private void handleCollisionAvatarFruit(Avatar player, Fruit fruit) {
        player.addEnergy(fruit.getRestoreHealthVal());
        GameObject respawnedPotion = new Fruit(fruit.getPos());
        removeObject(fruit);
        new ScheduledTask(player,
                WorldManager.CYCLE_LENGTH,
                false, () ->
                gameObjects().addGameObject(respawnedPotion, Layer.DEFAULT));
    }
    /**
     * Dynamically loads chunks around the specified chunk index.
     *
     * @param currentChunk The current chunk index.
     */
    private void loadChunksAround(int currentChunk) {
        int startChunk = currentChunk - VISIBLE_CHUNKS;
        int endChunk = currentChunk + VISIBLE_CHUNKS;

        for (int chunkIndex = startChunk; chunkIndex <= endChunk; chunkIndex++) {
            if (!loadedChunkIndices.contains(chunkIndex)) {
                loadChunk(chunkIndex);
            }
        }

        Set<Integer> chunksToKeep = new HashSet<>();
        for (int chunkIndex = startChunk; chunkIndex <= endChunk; chunkIndex++) {
            chunksToKeep.add(chunkIndex);
        }

        Set<Integer> chunksToUnload = new HashSet<>(loadedChunkIndices);
        chunksToUnload.removeAll(chunksToKeep);

        for (Integer chunkIndex : chunksToUnload) {
            unloadChunk(chunkIndex);
        }
    }

    /**
     * Loads a specific chunk and adds its objects to the game world.
     *
     * @param chunkIndex The index of the chunk to load.
     */
    private void loadChunk(int chunkIndex) {
        List<GameObject> terrainBlocks =
                worldManager.generateTerrainBlocks(chunkIndex);
        List<Tree> trees = worldManager.generateTrees(chunkIndex);
        List<Monster> monsters = generateRandomMonsters(chunkIndex);
        addWorldObjects(worldManager, terrainBlocks, trees, monsters, chunkIndex);
        loadedChunkIndices.add(chunkIndex);
    }

    /**
     * Unloads a specific chunk by removing its objects from the game world.
     *
     * @param chunkIndex The index of the chunk to unload.
     */
    private void unloadChunk(int chunkIndex) {
        ChunkData data = chunkDataMap.get(chunkIndex);
        for (GameObject block : data.terrainBlocks) {
            gameObjects().removeGameObject(block, Layer.STATIC_OBJECTS);
        }        for (Tree tree : data.trees) {
            for (GameObject treeBlock : tree.getTreeLogBlocks()) {
                gameObjects().removeGameObject(treeBlock, Layer.STATIC_OBJECTS);
            }
            for (GameObject leafBlock : tree.getTreeLeafBlocks()) {
                gameObjects().removeGameObject(leafBlock, LAYER_THROUGHABLE);
            }
            for (GameObject fruit : tree.getFruits()) {
                gameObjects().removeGameObject(fruit, Layer.DEFAULT);
            }
        }
        for (Monster monster : data.monsters) {
            pendingMonsterRespawns.remove(monster);
            monsterDamageCooldowns.remove(monster);
            gameObjects().removeGameObject(monster, Layer.DEFAULT);
        }

        chunkDataMap.remove(chunkIndex);
        loadedChunkIndices.remove(chunkIndex);
    }

    /**
     * Calculates the chunk index based on the given X-coordinate.
     *
     * @param x The X-coordinate.
     * @return The calculated chunk index.
     */
    private int getChunkIndex(float x) {
        return (int) Math.floor(x / WorldManager.CHUNK_SIZE);
    }

    /**
     * Represents data for a loaded chunk, including terrain blocks and trees.
     */
    private static class ChunkData {
        List<GameObject> terrainBlocks;
        List<Tree> trees;
        List<Monster> monsters;

        ChunkData(List<GameObject> terrainBlocks, List<Tree> trees, List<Monster> monsters) {
            this.terrainBlocks = terrainBlocks;
            this.trees = trees;
            this.monsters = monsters;
        }
    }
}
