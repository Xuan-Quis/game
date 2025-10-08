package com.tutorial.androidgametutorial.gamestates;

import com.tutorial.androidgametutorial.R;
import static com.tutorial.androidgametutorial.helpers.GameConstants.Sprite.X_DRAW_OFFSET;
import static com.tutorial.androidgametutorial.main.MainActivity.GAME_HEIGHT;
import static com.tutorial.androidgametutorial.main.MainActivity.GAME_WIDTH;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.view.MotionEvent;

import com.tutorial.androidgametutorial.entities.Building;
import com.tutorial.androidgametutorial.entities.Character;
import com.tutorial.androidgametutorial.entities.Entity;
import com.tutorial.androidgametutorial.entities.GameCharacters;
import com.tutorial.androidgametutorial.entities.GameObject;
import com.tutorial.androidgametutorial.entities.Player;
import com.tutorial.androidgametutorial.entities.Projectile;
import com.tutorial.androidgametutorial.entities.Weapons;
import com.tutorial.androidgametutorial.entities.EffectExplosion;
import com.tutorial.androidgametutorial.entities.SparkSkill;
import com.tutorial.androidgametutorial.entities.enemies.Boom;
import com.tutorial.androidgametutorial.entities.enemies.Boss;
import com.tutorial.androidgametutorial.entities.enemies.BossState;
import com.tutorial.androidgametutorial.entities.enemies.Skeleton;
import com.tutorial.androidgametutorial.entities.items.Item;
import com.tutorial.androidgametutorial.environments.Doorway;
import com.tutorial.androidgametutorial.environments.MapManager;
import com.tutorial.androidgametutorial.helpers.GameConstants;
import com.tutorial.androidgametutorial.helpers.HelpMethods;
import com.tutorial.androidgametutorial.helpers.interfaces.GameStateInterface;
import com.tutorial.androidgametutorial.main.Game;
import com.tutorial.androidgametutorial.ui.PlayingUI;
import com.tutorial.androidgametutorial.effects.ExplosionEffect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class Playing extends BaseState implements GameStateInterface {
    private float cameraX, cameraY;
    private boolean movePlayer;
    private PointF lastTouchDiff;
    private MapManager mapManager;
    private Player player;
    private Boss boss;
    private PlayingUI playingUI;
    private final Paint redPaint, healthBarRed, healthBarBlack;

    // Difficulty system
    private Game.Difficulty currentDifficulty = Game.Difficulty.EASY;

    private boolean doorwayJustPassed;
    private Entity[] listOfDrawables;
    private boolean listOfEntitiesMade;

    // thêm
    private SoundPool soundPool;
    private int swordHitSoundId;
    private int playerHitWallSoundId;
    private int boomExplosionSoundId;
    private boolean isSwordSoundEnabled = true; // Add this line


    private ArrayList<Projectile> projectiles = new ArrayList<>();
    private final Paint projectilePaint = new Paint();
    // thời điểm gây sát thương (ms)
    private ArrayList<ExplosionEffect> explosionEffects = new ArrayList<>();
    private ArrayList<EffectExplosion> effectExplosions = new ArrayList<>();
    private ArrayList<SparkSkill> sparkSkills = new ArrayList<>();

    // Spawn enemies
    private long lastSpawnTime = 0;

    // Tracking game stats for victory screen
    private long gameStartTime = 0;
    private int killCount = 0;
    private static final long VICTORY_TIME = 20000; // 20 seconds in milliseconds

    public Playing(Game game) {
        super(game);

        mapManager = new MapManager(this);
        calcStartCameraValues();

        player = new Player();

        // THAY ĐỔI: Khởi tạo boss là null. Boss sẽ được tạo sau khi vào map 3.
        boss = null;

        playingUI = new PlayingUI(this);

        // Initialize game start time
        gameStartTime = System.currentTimeMillis();
        killCount = 0;

        redPaint = new Paint();
        redPaint.setStrokeWidth(1);
        redPaint.setStyle(Paint.Style.STROKE);
        redPaint.setColor(Color.RED);

        healthBarRed = new Paint();
        healthBarBlack = new Paint();

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(audioAttributes)
                .build();
        projectilePaint.setColor(Color.CYAN);
        projectilePaint.setStyle(Paint.Style.FILL);

        swordHitSoundId = soundPool.load(game.getContext(), R.raw.sword_slice, 1);
        playerHitWallSoundId = soundPool.load(game.getContext(), R.raw.wall_hit, 1);
        boomExplosionSoundId = soundPool.load(game.getContext(), R.raw.explosion_boom, 1);

        initHealthBars();
    }

    // BỔ SUNG: Hàm để tạo Boss. Sẽ được gọi bởi MapManager
    public void spawnBoss() {
        // Tạo boss ở giữa map hiện tại (map 3)
        float bossX = mapManager.getMaxWidthCurrentMap() / 2f;
        float bossY = mapManager.getMaxHeightCurrentMap() / 2f;
        boss = new Boss(new PointF(bossX, bossY));
        System.out.println("🔥 BOSS ĐÃ XUẤT HIỆN TẠI MAP 3! 🔥");
    }


    // Helper to get direction to target
    private int getDirectionToTarget(float dx, float dy) {
        if (Math.abs(dx) > Math.abs(dy)) {
            return dx > 0 ? GameConstants.Face_Dir.RIGHT : GameConstants.Face_Dir.LEFT;
        } else {
            return dy > 0 ? GameConstants.Face_Dir.DOWN : GameConstants.Face_Dir.UP;
        }
    }

    private void initHealthBars() {
        healthBarRed.setStrokeWidth(10);
        healthBarRed.setStyle(Paint.Style.STROKE);
        healthBarRed.setColor(Color.RED);
        healthBarBlack.setStrokeWidth(14);
        healthBarBlack.setStyle(Paint.Style.STROKE);
        healthBarBlack.setColor(Color.BLACK);

    }

    private void calcStartCameraValues() {
        cameraX = GAME_WIDTH / 2f - mapManager.getMaxWidthCurrentMap() / 2f;
        cameraY = GAME_HEIGHT / 2f - mapManager.getMaxHeightCurrentMap() / 2f;
    }


    @Override
    public void update(double delta) {
        // Check for victory condition
        checkVictoryCondition();

        buildEntityList();
        updatePlayerMove(delta);
        player.update(delta, movePlayer);
        mapManager.setCameraValues(cameraX, cameraY);
        checkForDoorway();

        if (player.isAttacking()) {
            if (!player.isAttackChecked()) {
                checkPlayerAttack();
            }
        }

        if (mapManager.getCurrentMap().getSkeletonArrayList() != null)
            for (Skeleton skeleton : mapManager.getCurrentMap().getSkeletonArrayList())
                if (skeleton.isActive()) {
                    skeleton.update(delta, mapManager.getCurrentMap(), player, cameraX, cameraY, this);
                    if (skeleton.isAttacking()) {
                        if (!skeleton.isAttackChecked()) {
                            checkEnemyAttack(skeleton);
                        }
                    } else if (!skeleton.isPreparingAttack()) {
                        if (HelpMethods.IsPlayerCloseForAttack(skeleton, player, cameraY, cameraX)) {
                            skeleton.prepareAttack(player, cameraX, cameraY);
                        }
                    }
                }

        if (mapManager.getCurrentMap().getBoomArrayList() != null)
            for (Boom boom : mapManager.getCurrentMap().getBoomArrayList())
                if (boom.isActive()) {
                    boom.update(delta, mapManager.getCurrentMap(), player, cameraX, cameraY, this);
                    if (boom.isExploding()) {
                        // Boom tự động gây sát thương khi exploding, không cần check attack
                    } else if (!boom.isPreparingAttack()) {
                        if (HelpMethods.IsPlayerCloseForAttack(boom, player, cameraY, cameraX)) {
                            boom.prepareAttack(player, cameraX, cameraY);
                        }
                    }
                }

        // Cập nhật trạng thái và logic của Boss (chỉ khi boss đã tồn tại)
        if (boss != null) {
            float playerScreenX = player.getHitbox().centerX();
            float playerScreenY = player.getHitbox().centerY();

            float bossScreenX = boss.getPosition().x + cameraX;
            float bossScreenY = boss.getPosition().y + cameraY;

            float dx = playerScreenX - bossScreenX;
            float dy = playerScreenY - bossScreenY;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            if (distance < 300f && boss.getState() == BossState.IDLE) {
                if (player.getHitbox().centerX() > bossScreenX) {
                    boss.setState(BossState.PREPARE_ATTACK_RIGHT);
                } else {
                    boss.setState(BossState.PREPARE_ATTACK_LEFT);
                }
            }

            boss.update(System.currentTimeMillis(), player);
        }

        sortArray();
        updateProjectiles(delta);
        updateEffectExplosions(delta);
        updateSparkSkills(delta);
        updateItems(delta);
        player.updateEffects();
        spawnEnemies();
    }

    private void buildEntityList() {
        listOfDrawables = mapManager.getCurrentMap().getDrawableList();
        int[] boomMoveResIds = new int[]{
                R.drawable.boom_front,
                R.drawable.boom_left,
                R.drawable.boom_right,
                R.drawable.boom_behind
        };
        int[] boomAttackResIds = new int[]{
                R.drawable.boom_smile,
                R.drawable.boom_bum,
                R.drawable.boom_bum_2,
                R.drawable.boom_bum_3,
                R.drawable.boom_bum_4,
                R.drawable.boom_bum_5,
                R.drawable.boom_bum_6
        };
        Entity[] newList = Arrays.copyOf(listOfDrawables, listOfDrawables.length + 1);
        newList[newList.length - 2] = player;
        listOfDrawables = newList;
        listOfEntitiesMade = true;
    }

    private void sortArray() {
        player.setLastCameraYValue(cameraY);
//        Arrays.sort(listOfDrawables);
    }

    public void setCameraValues(PointF cameraPos) {
        this.cameraX = cameraPos.x;
        this.cameraY = cameraPos.y;
    }

    private void checkForDoorway() {
        Doorway doorwayPlayerIsOn = mapManager.isPlayerOnDoorway(player.getHitbox());

        if (doorwayPlayerIsOn != null) {
            if (!doorwayJustPassed) mapManager.changeMap(doorwayPlayerIsOn.getDoorwayConnectedTo());
        } else doorwayJustPassed = false;

    }

    public void setDoorwayJustPassed(boolean doorwayJustPassed) {
        this.doorwayJustPassed = doorwayJustPassed;
    }


    private void checkEnemyAttack(Character character) {
        character.updateWepHitbox();
        RectF playerHitbox = new RectF(player.getHitbox());
        playerHitbox.left -= cameraX;
        playerHitbox.top -= cameraY;
        playerHitbox.right -= cameraX;
        playerHitbox.bottom -= cameraY;
        if (RectF.intersects(character.getAttackBox(), playerHitbox)) {
            player.damageCharacter(character.getDamage());
            checkPlayerDead();
        }
        character.setAttackChecked(true);
    }

    private void checkPlayerDead() {
        if (player.getCurrentHealth() > 0)
            return;

        game.setCurrentGameState(Game.GameState.DEATH_SCREEN);
        player.resetCharacterHealth();

    }

    private void checkPlayerAttack() {
        RectF attackBoxWithoutCamera = new RectF(player.getAttackBox());
        attackBoxWithoutCamera.left -= cameraX;
        attackBoxWithoutCamera.top -= cameraY;
        attackBoxWithoutCamera.right -= cameraX;
        attackBoxWithoutCamera.bottom -= cameraY;

        // Check Skeleton
        if (mapManager.getCurrentMap().getSkeletonArrayList() != null) {
            for (Skeleton s : mapManager.getCurrentMap().getSkeletonArrayList()) {
                if (attackBoxWithoutCamera.intersects(
                        s.getHitbox().left,
                        s.getHitbox().top,
                        s.getHitbox().right,
                        s.getHitbox().bottom)) {

                    s.damageCharacter(player.getDamage());
                    playSwordHit();

                    if (s.getCurrentHealth() <= 0) {
                        s.setSkeletonInactive();
                        enemyKilled();
                        if (!s.hasDroppedItem()) {
                            s.setHasDroppedItem(true);
                            Item droppedItem = HelpMethods.tryDropItem(new PointF(s.getHitbox().centerX(), s.getHitbox().centerY()));
                            if (droppedItem != null) {
                                mapManager.getCurrentMap().getItemArrayList().add(droppedItem);
                            }
                        }
                    }
                }
            }
        }

        // Check Boom
        if (mapManager.getCurrentMap().getBoomArrayList() != null) {
            for (Boom boom : mapManager.getCurrentMap().getBoomArrayList()) {
                if (!boom.isActive()) continue;
                if (attackBoxWithoutCamera.intersects(
                        boom.getHitbox().left,
                        boom.getHitbox().top,
                        boom.getHitbox().right,
                        boom.getHitbox().bottom)) {
                    boom.damageCharacter(player.getDamage());
                    playSwordHit();
                    if (boom.getCurrentHealth() <= 0) {
                        boom.setBoomInactive();
                        enemyKilled();
                    }
                }
            }
        }

        player.setAttackChecked(true);
    }

    private void updateItems(double delta) {
        if (mapManager.getCurrentMap().getItemArrayList() != null) {
            java.util.Iterator<Item> itemIterator = mapManager.getCurrentMap().getItemArrayList().iterator();
            while (itemIterator.hasNext()) {
                Item item = itemIterator.next();
                if (!item.isActive()) {
                    itemIterator.remove();
                    continue;
                }
                item.update(delta);
                RectF itemScreenHitbox = new RectF(
                        item.getHitbox().left + cameraX,
                        item.getHitbox().top + cameraY,
                        item.getHitbox().right + cameraX,
                        item.getHitbox().bottom + cameraY
                );
                boolean collision = (itemScreenHitbox.left < player.getHitbox().right &&
                        itemScreenHitbox.right > player.getHitbox().left &&
                        itemScreenHitbox.top < player.getHitbox().bottom &&
                        itemScreenHitbox.bottom > player.getHitbox().top);

                if (collision) {
                    if (item.getItemType() == com.tutorial.androidgametutorial.entities.items.Items.MEDIPACK) {
                        player.useMedipack();
                    } else if (item.getItemType() == com.tutorial.androidgametutorial.entities.items.Items.FISH) {
                        player.useFish();
                    } else if (item.getItemType() == com.tutorial.androidgametutorial.entities.items.Items.EMPTY_POT) {
                        player.useEmptyPot();
                    }
                    item.deactivate();
                    itemIterator.remove();
                }
            }
        }
    }

    private void spawnEnemies() {
        if (!isOutsideMap()) {
            return;
        }
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSpawnTime >= 3000) {
            lastSpawnTime = currentTime;
            if (mapManager.getCurrentMap().getSkeletonArrayList() == null ||
                    mapManager.getCurrentMap().getBoomArrayList() == null) {
                return;
            }
            int spawnCount = 1;
            for (int i = 0; i < spawnCount; i++) {
                float spawnX = player.getHitbox().centerX() + (float) (Math.random() - 0.5) * 1000;
                float spawnY = player.getHitbox().centerY() + (float) (Math.random() - 0.5) * 1000;
                double random = Math.random();
                if (random < 0.4) {
                    Skeleton skeleton = new Skeleton(new PointF(spawnX, spawnY), GameCharacters.SKELETON);
                    mapManager.getCurrentMap().getSkeletonArrayList().add(skeleton);
                } else {
                    Boom boom = new Boom(new PointF(spawnX, spawnY));
                    boom.setPlaying(this);
                    mapManager.getCurrentMap().getBoomArrayList().add(boom);
                }
            }
        }
    }

    @Override
    public void render(Canvas c) {
        mapManager.drawTiles(c);
        if (listOfEntitiesMade)
            drawSortedEntities(c);

        // Vẽ Boss (chỉ khi boss đã tồn tại)
        if (boss != null) {
            boss.draw(c, cameraX, cameraY); // Sử dụng phương thức draw mới trong Boss
        }

        playingUI.draw(c);
        drawProjectiles(c);
        drawEffectExplosions(c);
        drawSparkSkills(c);
        drawItems(c);
    }



    private void drawProjectiles(Canvas c) {
        for (Projectile p : projectiles) {
            if (p.isActive()) {
                p.render(c, projectilePaint, cameraX, cameraY);
            }
        }
        for (ExplosionEffect effect : explosionEffects) {
            effect.render(c, cameraX, cameraY);
        }
    }

    private void drawEffectExplosions(Canvas c) {
        ArrayList<EffectExplosion> effectExplosionsCopy = new ArrayList<>(effectExplosions);
        for (EffectExplosion explosion : effectExplosionsCopy) {
            if (explosion.isActive()) {
                explosion.render(c, cameraX, cameraY);
            }
        }
    }

    private void drawSparkSkills(Canvas c) {
        ArrayList<SparkSkill> sparkSkillsCopy = new ArrayList<>(sparkSkills);
        for (SparkSkill sparkSkill : sparkSkillsCopy) {
            if (sparkSkill.isActive()) {
                sparkSkill.render(c, cameraX, cameraY);
            }
        }
    }

    public void castThrowSwordSkill() {
        player.castThrowSword(this);
    }

    public void castEffectExplosionSkill() {
        player.castEffectExplosion(this);
    }

    public void castSparkSkill() {
        player.castSparkSkill(this);
    }

    private void drawSortedEntities(Canvas c) {
        for (Entity e : listOfDrawables) {
            if (e instanceof Skeleton skeleton) {
                if (skeleton.isActive())
                    drawCharacter(c, skeleton);
            } else if (e instanceof GameObject gameObject) {
                mapManager.drawObject(c, gameObject);
            } else if (e instanceof Building building) {
                mapManager.drawBuilding(c, building);
            } else if (e instanceof Item item) {
                mapManager.drawItem(c, item);
            } else if (e instanceof Player) {
                drawPlayer(c);
            } else if (e instanceof Boom boom) {
                if (boom.isActive())
                    drawBoom(c, boom);
            }
        }
    }


    private void drawPlayer(Canvas c) {
        c.drawBitmap(Weapons.SHADOW.getWeaponImg(), player.getHitbox().left, player.getHitbox().bottom - 5 * GameConstants.Sprite.SCALE_MULTIPLIER, null);
        c.drawBitmap(player.getGameCharType().getSprite(player.getAniIndex(), player.getFaceDir()), player.getHitbox().left - X_DRAW_OFFSET, player.getHitbox().top - GameConstants.Sprite.Y_DRAW_OFFSET, null);
        c.drawRect(player.getHitbox(), redPaint);
        if (player.isAttacking()) drawWeapon(c, player);
    }


    private void drawWeapon(Canvas c, Character character) {
        c.rotate(character.getWepRot(), character.getAttackBox().left, character.getAttackBox().top);
        c.drawBitmap(Weapons.BIG_SWORD.getWeaponImg(), character.getAttackBox().left + character.wepRotAdjustLeft(), character.getAttackBox().top + character.wepRotAdjustTop(), null);
        c.rotate(character.getWepRot() * -1, character.getAttackBox().left, character.getAttackBox().top);
        c.drawRect(character.getAttackBox(), redPaint);
    }

    private void drawEnemyWeapon(Canvas c, Character character) {
        c.rotate(character.getWepRot(), character.getAttackBox().left + cameraX, character.getAttackBox().top + cameraY);
        c.drawBitmap(Weapons.BIG_SWORD.getWeaponImg(), character.getAttackBox().left + cameraX + character.wepRotAdjustLeft(), character.getAttackBox().top + cameraY + character.wepRotAdjustTop(), null);
        c.rotate(character.getWepRot() * -1, character.getAttackBox().left + cameraX, character.getAttackBox().top + cameraY);
    }


    public void drawCharacter(Canvas canvas, Character c) {
        canvas.drawBitmap(Weapons.SHADOW.getWeaponImg(), c.getHitbox().left + cameraX, c.getHitbox().bottom - 5 * GameConstants.Sprite.SCALE_MULTIPLIER + cameraY, null);
        canvas.drawBitmap(c.getGameCharType().getSprite(c.getAniIndex(), c.getFaceDir()), c.getHitbox().left + cameraX - X_DRAW_OFFSET, c.getHitbox().top + cameraY - GameConstants.Sprite.Y_DRAW_OFFSET, null);
        canvas.drawRect(c.getHitbox().left + cameraX, c.getHitbox().top + cameraY, c.getHitbox().right + cameraX, c.getHitbox().bottom + cameraY, redPaint);
        if (c.isAttacking())
            drawEnemyWeapon(canvas, c);

        if (c.getCurrentHealth() < c.getMaxHealth())
            drawHealthBar(canvas, c);
    }

    private void drawBoom(Canvas canvas, Boom boom) {
        canvas.drawBitmap(Weapons.SHADOW.getWeaponImg(), boom.getHitbox().left + cameraX, boom.getHitbox().bottom - 5 * GameConstants.Sprite.SCALE_MULTIPLIER + cameraY, null);
        canvas.drawBitmap(boom.getBoomSprite(), boom.getHitbox().left + cameraX - X_DRAW_OFFSET, boom.getHitbox().top + cameraY - GameConstants.Sprite.Y_DRAW_OFFSET, null);
        canvas.drawRect(boom.getHitbox().left + cameraX, boom.getHitbox().top + cameraY, boom.getHitbox().right + cameraX, boom.getHitbox().bottom + cameraY, redPaint);

        if (boom.getCurrentHealth() < boom.getMaxHealth())
            drawHealthBar(canvas, boom);
    }

    private void drawItems(Canvas c) {
        if (mapManager.getCurrentMap().getItemArrayList() != null) {
            for (Item item : mapManager.getCurrentMap().getItemArrayList()) {
                if (item.isActive()) {
                    item.render(c, cameraX, cameraY);
                }
            }
        }
    }

    private void drawHealthBar(Canvas canvas, Character c) {
        canvas.drawLine(c.getHitbox().left + cameraX,
                c.getHitbox().top + cameraY - 5 * GameConstants.Sprite.SCALE_MULTIPLIER,
                c.getHitbox().right + cameraX,
                c.getHitbox().top + cameraY - 5 * GameConstants.Sprite.SCALE_MULTIPLIER, healthBarBlack);

        float fullBarWidth = c.getHitbox().width();
        float percentOfMaxHealth = (float) c.getCurrentHealth() / c.getMaxHealth();
        float barWidth = fullBarWidth * percentOfMaxHealth;

        canvas.drawLine(c.getHitbox().left + cameraX,
                c.getHitbox().top + cameraY - 5 * GameConstants.Sprite.SCALE_MULTIPLIER,
                c.getHitbox().left + cameraX + barWidth,
                c.getHitbox().top + cameraY - 5 * GameConstants.Sprite.SCALE_MULTIPLIER, healthBarRed);
    }

    private void updatePlayerMove(double delta) {
        if (!movePlayer) return;

        float baseSpeed = (float) (delta * 300 * player.getSpeedMultiplier());

        double angle = Math.atan(Math.abs(lastTouchDiff.y) / Math.abs(lastTouchDiff.x));
        float xSpeed = (float) Math.cos(angle);
        float ySpeed = (float) Math.sin(angle);

        if (xSpeed > ySpeed) {
            player.setFaceDir(lastTouchDiff.x > 0 ? GameConstants.Face_Dir.RIGHT : GameConstants.Face_Dir.LEFT);
        } else {
            player.setFaceDir(lastTouchDiff.y > 0 ? GameConstants.Face_Dir.DOWN : GameConstants.Face_Dir.UP);
        }

        xSpeed = lastTouchDiff.x < 0 ? -xSpeed : xSpeed;
        ySpeed = lastTouchDiff.y < 0 ? -ySpeed : ySpeed;

        float deltaX = -xSpeed * baseSpeed;
        float deltaY = -ySpeed * baseSpeed;

        float deltaCameraX = -cameraX - deltaX;
        float deltaCameraY = -cameraY - deltaY;

        boolean hitWall = false;

        if (isOutsideMap()) {
            if (HelpMethods.CanWalkHereOutside(player.getHitbox(), deltaCameraX, deltaCameraY, mapManager.getCurrentMap())) {
                cameraX += deltaX;
                cameraY += deltaY;
            } else {
                if (HelpMethods.CanWalkHereUpDownOutside(player.getHitbox(), deltaCameraY, -cameraX, mapManager.getCurrentMap())) {
                    cameraY += deltaY;
                } else {
                    hitWall = true;
                }
                if (HelpMethods.CanWalkHereLeftRightOutside(player.getHitbox(), deltaCameraX, -cameraY, mapManager.getCurrentMap())) {
                    cameraX += deltaX;
                } else {
                    hitWall = true;
                }
                if (hitWall) playPlayerHitWall();
            }
            checkPlayerOutOfBounds();
        } else {
            if (HelpMethods.CanWalkHere(player.getHitbox(), deltaCameraX, deltaCameraY, mapManager.getCurrentMap())) {
                cameraX += deltaX;
                cameraY += deltaY;
            } else {
                if (HelpMethods.CanWalkHereUpDown(player.getHitbox(), deltaCameraY, -cameraX, mapManager.getCurrentMap())) {
                    cameraY += deltaY;
                } else {
                    hitWall = true;
                }
                if (HelpMethods.CanWalkHereLeftRight(player.getHitbox(), deltaCameraX, -cameraY, mapManager.getCurrentMap())) {
                    cameraX += deltaX;
                } else {
                    hitWall = true;
                }
                if (hitWall) playPlayerHitWall();
            }
        }
    }

    private boolean isOutsideMap() {
        return mapManager.getCurrentMap().getFloorType() == com.tutorial.androidgametutorial.environments.Tiles.OUTSIDE ||
                mapManager.getCurrentMap().getFloorType() == com.tutorial.androidgametutorial.environments.Tiles.SNOW;
    }

    private void checkPlayerOutOfBounds() {
        float playerWorldX = -cameraX + player.getHitbox().centerX();
        float playerWorldY = -cameraY + player.getHitbox().centerY();

        float mapWidth = mapManager.getMaxWidthCurrentMap();
        float mapHeight = mapManager.getMaxHeightCurrentMap();

        if (playerWorldX < 0 || playerWorldX > mapWidth ||
                playerWorldY < 0 || playerWorldY > mapHeight) {
            game.setCurrentGameState(Game.GameState.DEATH_SCREEN);
        }
    }

    public void setGameStateToMenu() {
        game.setCurrentGameState(Game.GameState.MENU);
    }

    public void setPlayerMoveTrue(PointF lastTouchDiff) {
        movePlayer = true;
        this.lastTouchDiff = lastTouchDiff;
    }

    public void setPlayerMoveFalse() {
        movePlayer = false;
        player.resetAnimation();
    }

    @Override
    public void touchEvents(MotionEvent event) {
        playingUI.touchEvents(event);
    }

    public Player getPlayer() {
        return player;
    }

    public PlayingUI getPlayingUI() {
        return playingUI;
    }

    private void playSwordHit() {
        if (isSwordSoundEnabled) {
            soundPool.play(swordHitSoundId, 1, 1, 1, 0, 1f);
        }
    }

    public void setSwordSoundEnabled(boolean enabled) {
        isSwordSoundEnabled = enabled;
    }


    public void dispose() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }

    public void addProjectile(Projectile p) {
        projectiles.add(p);
    }

    public void addEffectExplosion(EffectExplosion explosion) {
        effectExplosions.add(explosion);
    }

    public void addSparkSkill(SparkSkill sparkSkill) {
        sparkSkills.add(sparkSkill);
    }

    public void addExplosionEffect(ExplosionEffect explosionEffect) {
        explosionEffects.add(explosionEffect);
    }

    public void playBoomExplosionSound() {
        if (isSwordSoundEnabled) {
            soundPool.play(boomExplosionSoundId, 1, 1, 1, 0, 1f);
        }
    }

    public Skeleton findNearestSkeleton(float px, float py, float range) {
        Skeleton nearest = null;
        float minDistSq = range * range;

        if (mapManager.getCurrentMap().getSkeletonArrayList() != null) {
            for (Skeleton s : mapManager.getCurrentMap().getSkeletonArrayList()) {
                if (!s.isActive()) continue;
                float dx = s.getHitbox().centerX() - px;
                float dy = s.getHitbox().centerY() - py;
                float distSq = dx * dx + dy * dy;

                if (distSq < minDistSq) {
                    minDistSq = distSq;
                    nearest = s;
                }
            }
        }
        return nearest;
    }


    private void updateProjectiles(double delta) {
        for (Projectile p : projectiles) {
            if (!p.isActive()) continue;
            p.update(delta);
            if (mapManager.getCurrentMap().getSkeletonArrayList() != null) {
                for (Skeleton s : mapManager.getCurrentMap().getSkeletonArrayList()) {
                    if (!s.isActive()) continue;
                    if (RectF.intersects(p.getHitbox(), s.getHitbox())) {
                        int halfMaxHp = s.getMaxHealth() / 2;
                        s.damageCharacter(halfMaxHp);
                        explosionEffects.add(new ExplosionEffect(new PointF(s.getHitbox().centerX(), s.getHitbox().centerY())));
                        if (s.getCurrentHealth() <= 0) {
                            s.setSkeletonInactive();
                            enemyKilled();
                            if (!s.hasDroppedItem()) {
                                s.setHasDroppedItem(true);
                                Item droppedItem = HelpMethods.tryDropItem(new PointF(s.getHitbox().centerX(), s.getHitbox().centerY()));
                                if (droppedItem != null) {
                                    mapManager.getCurrentMap().getItemArrayList().add(droppedItem);
                                }
                            }
                        }
                        p.deactivate();
                        break;
                    }
                }
            }

            if (mapManager.getCurrentMap().getBoomArrayList() != null) {
                for (Boom boom : mapManager.getCurrentMap().getBoomArrayList()) {
                    if (!boom.isActive()) continue;
                    if (RectF.intersects(p.getHitbox(), boom.getHitbox())) {
                        int halfMaxHp = boom.getMaxHealth() / 2;
                        boom.damageCharacter(halfMaxHp);
                        explosionEffects.add(new ExplosionEffect(new PointF(boom.getHitbox().centerX(), boom.getHitbox().centerY())));
                        if (boom.getCurrentHealth() <= 0) {
                            boom.setBoomInactive();
                            enemyKilled();
                        }
                        p.deactivate();
                        break;
                    }
                }
            }
            if (p.isOutOfBounds(mapManager.getMaxWidthCurrentMap(), mapManager.getMaxHeightCurrentMap())) {
                p.deactivate();
            }
        }
        Iterator<ExplosionEffect> it = explosionEffects.iterator();
        while (it.hasNext()) {
            ExplosionEffect effect = it.next();
            effect.update();
            if (!effect.isActive()) it.remove();
        }
    }

    private void updateEffectExplosions(double delta) {
        Iterator<EffectExplosion> it = effectExplosions.iterator();
        while (it.hasNext()) {
            EffectExplosion explosion = it.next();
            if (explosion.isActive()) {
                explosion.update(delta, this);
            } else {
                it.remove();
            }
        }
    }

    private void updateSparkSkills(double delta) {
        ArrayList<SparkSkill> sparkSkillsCopy = new ArrayList<>(sparkSkills);
        Iterator<SparkSkill> it = sparkSkillsCopy.iterator();

        while (it.hasNext()) {
            SparkSkill sparkSkill = it.next();
            if (sparkSkill.isActive()) {
                sparkSkill.update(delta, this);
            } else {
                sparkSkills.remove(sparkSkill);
            }
        }
    }

    public float getCameraX() {
        return cameraX;
    }

    public float getCameraY() {
        return cameraY;
    }

    public MapManager getMapManager() {
        return mapManager;
    }

    private void playPlayerHitWall() {
        soundPool.play(playerHitWallSoundId, 1, 1, 1, 0, 1f);
    }

    private void checkVictoryCondition() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - gameStartTime >= VICTORY_TIME) {
            int currentMapLevel = mapManager.getCurrentMapLevel();

            if (currentMapLevel == 1) {
                mapManager.progressToNextMap();
                gameStartTime = System.currentTimeMillis();
                float playerStartX = GAME_WIDTH / 2f;
                float playerStartY = GAME_HEIGHT / 2f;
                player.resetPosition(playerStartX, playerStartY);
            } else if (currentMapLevel == 2) {
                mapManager.progressToNextMap();
                gameStartTime = System.currentTimeMillis();
                float playerStartX = GAME_WIDTH / 2f;
                float playerStartY = GAME_HEIGHT / 2f;
                player.resetPosition(playerStartX, playerStartY);
            } else if (currentMapLevel == 3) {
                game.getWinScreen().setKillCount(killCount);
                game.setCurrentGameState(Game.GameState.WIN_SCREEN);
            }
        }
    }

    public void enemyKilled() {
        killCount++;
    }

    public void setDifficulty(Game.Difficulty difficulty) {
        this.currentDifficulty = difficulty;
    }

    public Game.Difficulty getCurrentDifficulty() {
        return currentDifficulty;
    }

    public void resetGame() {
        mapManager.resetToMap1();
        calcStartCameraValues();
        player.resetCharacterHealth();
        player.resetAnimation();
        movePlayer = false;
        lastTouchDiff = null;
        projectiles.clear();
        explosionEffects.clear();
        effectExplosions.clear();
        sparkSkills.clear();
        lastSpawnTime = 0;
        gameStartTime = System.currentTimeMillis();
        killCount = 0;
        mapManager.resetMapToInitialState();

        // THAY ĐỔI: Xóa boss khi reset game
        boss = null;
    }
}