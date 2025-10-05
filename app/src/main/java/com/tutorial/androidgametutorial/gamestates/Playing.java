package com.tutorial.androidgametutorial.gamestates;

import com.tutorial.androidgametutorial.R;
import static com.tutorial.androidgametutorial.helpers.GameConstants.Sprite.X_DRAW_OFFSET;
import static com.tutorial.androidgametutorial.main.MainActivity.GAME_HEIGHT;
import static com.tutorial.androidgametutorial.main.MainActivity.GAME_WIDTH;

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
import com.tutorial.androidgametutorial.entities.GameObject;
import com.tutorial.androidgametutorial.entities.Player;
import com.tutorial.androidgametutorial.entities.Projectile;
import com.tutorial.androidgametutorial.entities.Weapons;
import com.tutorial.androidgametutorial.entities.EffectExplosion;
import com.tutorial.androidgametutorial.entities.SparkSkill;
import com.tutorial.androidgametutorial.entities.enemies.Boom;
import com.tutorial.androidgametutorial.entities.enemies.Monster;
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
            System.out.println("🎮 Player đang attack! Attack checked: " + player.isAttackChecked());
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
        if (mapManager.getCurrentMap().getMonsterArrayList() != null)
            for (Monster monster : mapManager.getCurrentMap().getMonsterArrayList())
                if (monster.isActive()) {
                    monster.update(delta, mapManager.getCurrentMap(), player, cameraX, cameraY, this);
                    if (monster.isAttacking()) {
                        if (!monster.isAttackChecked()) {
                            checkEnemyAttack(monster);
                        }
                    } else if (!monster.isPreparingAttack()) {
                        if (HelpMethods.IsPlayerCloseForAttack(monster, player, cameraY, cameraX)) {
                            monster.prepareAttack(player, cameraX, cameraY);
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

        sortArray();
        updateProjectiles(delta);
        updateEffectExplosions(delta);
        updateSparkSkills(delta);
        updateItems(delta);
        player.updateEffects(); // Cập nhật hiệu ứng items
        spawnEnemies(); // Spawn quái vô hạn
    }

    private void buildEntityList() {
        listOfDrawables = mapManager.getCurrentMap().getDrawableList();
        // Khởi tạo Boom với các resource id ảnh di chuyển và tấn công
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
        // Thêm Boom vào listOfDrawables (nếu có chỗ trống hoặc cần mở rộng mảng)
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
            System.out.println("Enemy Hit Player!");
            player.damageCharacter(character.getDamage());
            checkPlayerDead();
        } else {
            System.out.println("Enemy Missed Player!");
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
        System.out.println("⚔️ Player đang attack! Damage: " + player.getDamage());

        RectF attackBoxWithoutCamera = new RectF(player.getAttackBox());
        attackBoxWithoutCamera.left -= cameraX;
        attackBoxWithoutCamera.top -= cameraY;
        attackBoxWithoutCamera.right -= cameraX;
        attackBoxWithoutCamera.bottom -= cameraY;

        System.out.println("🎯 Attack box: (" + attackBoxWithoutCamera.left + ", " + attackBoxWithoutCamera.top +
                ", " + attackBoxWithoutCamera.right + ", " + attackBoxWithoutCamera.bottom + ")");

        // Check Skeleton
        if (mapManager.getCurrentMap().getSkeletonArrayList() != null) {
            System.out.println("👹 Có " + mapManager.getCurrentMap().getSkeletonArrayList().size() + " Skeleton");
            for (Skeleton s : mapManager.getCurrentMap().getSkeletonArrayList()) {
                System.out.println("🔍 Kiểm tra Skeleton tại: (" + s.getHitbox().centerX() + ", " + s.getHitbox().centerY() + ") - HP: " + s.getCurrentHealth());
                if (attackBoxWithoutCamera.intersects(
                        s.getHitbox().left,
                        s.getHitbox().top,
                        s.getHitbox().right,
                        s.getHitbox().bottom)) {

                    System.out.println("💥 HIT Skeleton! Damage: " + player.getDamage());
                    // Trừ máu quái
                    s.damageCharacter(player.getDamage());

                    // 🔊 Phát âm thanh nhát chém khi trúng
                    playSwordHit();

                    // Nếu quái chết thì set inactive và drop item
                    if (s.getCurrentHealth() <= 0) {
                        s.setSkeletonInactive();
                        enemyKilled(); // Sử dụng method thống nhất thay vì killCount++ trực tiếp
                        System.out.println("💀 Skeleton đã chết! Kill count: " + killCount);
                        // Chỉ drop item nếu chưa drop (tránh drop nhiều lần)
                        if (!s.hasDroppedItem()) {
                            s.setHasDroppedItem(true);
                            Item droppedItem = HelpMethods.tryDropItem(new PointF(s.getHitbox().centerX(), s.getHitbox().centerY()));
                            if (droppedItem != null) {
                                mapManager.getCurrentMap().getItemArrayList().add(droppedItem);
                                System.out.println("🎁 Skeleton chết! Drop item: " + droppedItem.getItemType());
                            } else {
                                System.out.println("❌ Skeleton chết nhưng không drop item");
                            }
                        }
                    }
                }
            }
        }

        // Check Monster (tách riêng khỏi vòng lặp Skeleton)
        if (mapManager.getCurrentMap().getMonsterArrayList() != null) {
            System.out.println("👹 Có " + mapManager.getCurrentMap().getMonsterArrayList().size() + " Monster");
            for (Monster m : mapManager.getCurrentMap().getMonsterArrayList()) {
                System.out.println("🔍 Kiểm tra Monster tại: (" + m.getHitbox().centerX() + ", " + m.getHitbox().centerY() + ") - HP: " + m.getCurrentHealth());
                if (attackBoxWithoutCamera.intersects(
                        m.getHitbox().left,
                        m.getHitbox().top,
                        m.getHitbox().right,
                        m.getHitbox().bottom)) {

                    System.out.println("💥 HIT Monster! Damage: " + player.getDamage());
                    m.damageCharacter(player.getDamage());
                    playSwordHit();

                    if (m.getCurrentHealth() <= 0) {
                        m.setMonsterInactive();
                        enemyKilled(); // Sử dụng method thống nhất thay vì killCount++ trực tiếp
                        System.out.println("💀 Monster đã chết! Kill count: " + killCount);
                        // Chỉ drop item nếu chưa drop (tránh drop nhiều lần)
                        if (!m.hasDroppedItem()) {
                            m.setHasDroppedItem(true);
                            Item droppedItem = HelpMethods.tryDropItem(new PointF(m.getHitbox().centerX(), m.getHitbox().centerY()));
                            if (droppedItem != null) {
                                mapManager.getCurrentMap().getItemArrayList().add(droppedItem);
                                System.out.println("🎁 Monster chết! Drop item: " + droppedItem.getItemType());
                            } else {
                                System.out.println("❌ Monster chết nhưng không drop item");
                            }
                        }
                    }
                }
            }
        }

        // Check Boom (thêm logic tấn công Boom)
        if (mapManager.getCurrentMap().getBoomArrayList() != null) {
            System.out.println("💥 Có " + mapManager.getCurrentMap().getBoomArrayList().size() + " Boom");
            for (Boom boom : mapManager.getCurrentMap().getBoomArrayList()) {
                if (!boom.isActive()) continue;
                System.out.println("🔍 Kiểm tra Boom tại: (" + boom.getHitbox().centerX() + ", " + boom.getHitbox().centerY() + ") - HP: " + boom.getCurrentHealth());
                if (attackBoxWithoutCamera.intersects(
                        boom.getHitbox().left,
                        boom.getHitbox().top,
                        boom.getHitbox().right,
                        boom.getHitbox().bottom)) {

                    System.out.println("💥 HIT Boom! Damage: " + player.getDamage());
                    boom.damageCharacter(player.getDamage());
                    playSwordHit();

                    if (boom.getCurrentHealth() <= 0) {
                        boom.setBoomInactive();
                        enemyKilled(); // Sử dụng method thống nhất thay vì killCount++ trực tiếp
                        System.out.println("💀 Boom đã chết! Kill count: " + killCount);
                        // Boom không drop item
                    }
                }
            }
        }

        player.setAttackChecked(true);
    }

    private void updateItems(double delta) {
        if (mapManager.getCurrentMap().getItemArrayList() != null) {
            // Sử dụng Iterator để có thể xóa items an toàn
            java.util.Iterator<Item> itemIterator = mapManager.getCurrentMap().getItemArrayList().iterator();
            while (itemIterator.hasNext()) {
                Item item = itemIterator.next();

                if (!item.isActive()) {
                    // Xóa items đã deactivate khỏi map
                    itemIterator.remove();
                    System.out.println("🗑️ Xóa item " + item.getItemType() + " đã deactivate khỏi map");
                    continue;
                }

                item.update(delta);

                // Kiểm tra collision với player
                // Chuyển item hitbox từ world coords sang screen coords để so sánh với player
                RectF itemScreenHitbox = new RectF(
                        item.getHitbox().left + cameraX,
                        item.getHitbox().top + cameraY,
                        item.getHitbox().right + cameraX,
                        item.getHitbox().bottom + cameraY
                );

                // Debug ít hơn - chỉ khi có collision gần
                float distance = (float) Math.sqrt(
                        Math.pow(itemScreenHitbox.centerX() - player.getHitbox().centerX(), 2) +
                                Math.pow(itemScreenHitbox.centerY() - player.getHitbox().centerY(), 2)
                );

                if (distance < 300) { // Tăng từ 200 lên 300 để debug nhiều hơn
                    System.out.println("🎯 Item " + item.getItemType() + " tại: (" + itemScreenHitbox.centerX() + ", " + itemScreenHitbox.centerY() + ")");
                    System.out.println("👤 Player tại: (" + player.getHitbox().centerX() + ", " + player.getHitbox().centerY() + ")");
                    System.out.println("📏 Khoảng cách: " + distance);
                    System.out.println("🎯 Item hitbox: (" + itemScreenHitbox.left + ", " + itemScreenHitbox.top + ", " + itemScreenHitbox.right + ", " + itemScreenHitbox.bottom + ")");
                    System.out.println("👤 Player hitbox: (" + player.getHitbox().left + ", " + player.getHitbox().top + ", " + player.getHitbox().right + ", " + player.getHitbox().bottom + ")");
                }

                // Kiểm tra collision bằng logic đơn giản hơn
                boolean collision = (itemScreenHitbox.left < player.getHitbox().right &&
                        itemScreenHitbox.right > player.getHitbox().left &&
                        itemScreenHitbox.top < player.getHitbox().bottom &&
                        itemScreenHitbox.bottom > player.getHitbox().top);

                if (collision) {
                    // Player ăn item
                    System.out.println("💥 COLLISION DETECTED! Player chạm vào item " + item.getItemType());
                    if (item.getItemType() == com.tutorial.androidgametutorial.entities.items.Items.MEDIPACK) {
                        player.useMedipack();
                    } else if (item.getItemType() == com.tutorial.androidgametutorial.entities.items.Items.FISH) {
                        player.useFish();
                    } else if (item.getItemType() == com.tutorial.androidgametutorial.entities.items.Items.EMPTY_POT) {
                        player.useEmptyPot();
                    }

                    item.deactivate();
                    itemIterator.remove(); // Xóa ngay lập tức khỏi map
                    System.out.println("✅ Item đã được ăn và xóa khỏi map!");
                }
            }

            System.out.println("🔍 Có " + mapManager.getCurrentMap().getItemArrayList().size() + " items trong map");
        } else {
            System.out.println("❌ itemArrayList is null!");
        }
    }

    private void spawnEnemies() {
        // CHỈ SPAWN QUÁI Ở MAP NGOÀI - KHÔNG SPAWN Ở MAP TRONG NHÀ
        if (!isOutsideMap()) {
            System.out.println("🏠 Đang ở trong nhà - không spawn quái");
            return; // Thoát ngay nếu đang ở trong nhà
        }

        // Spawn quái vô hạn - kiểm tra mỗi 3 giây
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSpawnTime >= 3000) { // 3 giây spawn 1 lần
            lastSpawnTime = currentTime;

            // KIỂM tra NULL trước khi spawn để tránh crash
            if (mapManager.getCurrentMap().getSkeletonArrayList() == null ||
                    mapManager.getCurrentMap().getMonsterArrayList() == null ||
                    mapManager.getCurrentMap().getBoomArrayList() == null) {
                System.out.println("❌ Một hoặc nhiều ArrayList enemies là null - không thể spawn");
                return;
            }

            // Spawn 1 quái mỗi lần để tránh drop item trùng lặp
            int spawnCount = 1; // Chỉ spawn 1 quái mỗi lần
            System.out.println("🔄 Spawn " + spawnCount + " enemy(ies) ở map ngoài");

            for (int i = 0; i < spawnCount; i++) {
                // Spawn ở vị trí random xung quanh player
                float spawnX = player.getHitbox().centerX() + (float) (Math.random() - 0.5) * 1000;
                float spawnY = player.getHitbox().centerY() + (float) (Math.random() - 0.5) * 1000;

                // Chọn loại quái random
                double random = Math.random();
                if (random < 0.4) { // 40% Skeleton
                    Skeleton skeleton = new Skeleton(new PointF(spawnX, spawnY));
                    mapManager.getCurrentMap().getSkeletonArrayList().add(skeleton);
                    System.out.println("👹 Spawn Skeleton tại: (" + spawnX + ", " + spawnY + ") - Total Skeleton: " + mapManager.getCurrentMap().getSkeletonArrayList().size());
                } else if (random < 0.7) { // 30% Monster
                    Monster monster = new Monster(new PointF(spawnX, spawnY));
                    mapManager.getCurrentMap().getMonsterArrayList().add(monster);
                    System.out.println("👹 Spawn Monster tại: (" + spawnX + ", " + spawnY + ") - Total Monster: " + mapManager.getCurrentMap().getMonsterArrayList().size());
                } else { // 30% Boom
                    Boom boom = new Boom(new PointF(spawnX, spawnY));
                    boom.setPlaying(this); // Set playing reference
                    mapManager.getCurrentMap().getBoomArrayList().add(boom);
                    System.out.println("💥 Spawn Boom tại: (" + spawnX + ", " + spawnY + ") - Total Boom: " + mapManager.getCurrentMap().getBoomArrayList().size());
                }
            }
        }
    }

    @Override
    public void render(Canvas c) {
        mapManager.drawTiles(c);
        if (listOfEntitiesMade)
            drawSortedEntities(c);

        playingUI.draw(c);

        // 🔥 Bổ sung: vẽ projectile
        drawProjectiles(c);
        // Vẽ EffectExplosion
        drawEffectExplosions(c);
        // Vẽ SparkSkills
        drawSparkSkills(c);
        // Vẽ Items
        drawItems(c);
        // Vẽ Shield effect
        drawShieldEffect(c);
    }

    private void drawProjectiles(Canvas c) {
        for (Projectile p : projectiles) {
            if (p.isActive()) {
                p.render(c, projectilePaint, cameraX, cameraY);
            }
        }
        // Vẽ hiệu ứng nổ
        for (ExplosionEffect effect : explosionEffects) {
            effect.render(c, cameraX, cameraY);
        }
    }

    private void drawEffectExplosions(Canvas c) {
        // Tạo bản sao để tránh ConcurrentModificationException
        ArrayList<EffectExplosion> effectExplosionsCopy = new ArrayList<>(effectExplosions);
        for (EffectExplosion explosion : effectExplosionsCopy) {
            if (explosion.isActive()) {
                explosion.render(c, cameraX, cameraY);
            }
        }
    }

    private void drawSparkSkills(Canvas c) {
        // Tạo bản sao để tránh ConcurrentModificationException
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
        // Âm thanh đã được phát trong Player.castSparkSkill()
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
            } else if (e instanceof Monster monster) {
                if (monster.isActive())
                    drawCharacter(c, monster);
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
//        c.drawRect(character.getAttackBox(), redPaint);
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

    private void drawShieldEffect(Canvas c) {
        float centerX = player.getHitbox().centerX() + cameraX;
        float centerY = player.getHitbox().centerY() + cameraY;
        float radius = player.getHitbox().width() / 2 + 20; // Vòng tròn lớn hơn player một chút

        // Vẽ shield effect
        if (player.hasShield()) {
            // Vẽ vòng tròn xanh quanh player
            Paint shieldPaint = new Paint();
            shieldPaint.setColor(Color.CYAN);
            shieldPaint.setStyle(Paint.Style.STROKE);
            shieldPaint.setStrokeWidth(8);

            c.drawCircle(centerX, centerY, radius, shieldPaint);

            // Vẽ số đòn còn lại
            Paint textPaint = new Paint();
            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(30);
            textPaint.setFakeBoldText(true);
            c.drawText("" + player.getShieldHits(), centerX - 10, centerY + 10, textPaint);
        }

        // Vẽ speed boost effect
        if (player.hasSpeedBoost()) {
            // Vẽ vòng tròn vàng cho speed boost
            Paint speedPaint = new Paint();
            speedPaint.setColor(Color.YELLOW);
            speedPaint.setStyle(Paint.Style.STROKE);
            speedPaint.setStrokeWidth(6);

            c.drawCircle(centerX, centerY, radius + 15, speedPaint);

            // Vẽ text "SPEED"
            Paint speedTextPaint = new Paint();
            speedTextPaint.setColor(Color.YELLOW);
            speedTextPaint.setTextSize(20);
            speedTextPaint.setFakeBoldText(true);
            c.drawText("SPEED", centerX - 25, centerY - 30, speedTextPaint);
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

        // Draw the red health bar from left to right, shrinking from the right as health decreases
        canvas.drawLine(c.getHitbox().left + cameraX,
                c.getHitbox().top + cameraY - 5 * GameConstants.Sprite.SCALE_MULTIPLIER,
                c.getHitbox().left + cameraX + barWidth,
                c.getHitbox().top + cameraY - 5 * GameConstants.Sprite.SCALE_MULTIPLIER, healthBarRed);
    }

    private void updatePlayerMove(double delta) {
        if (!movePlayer) return;

        float baseSpeed = (float) (delta * 300 * player.getSpeedMultiplier());

        // Tính góc di chuyển dựa vào lastTouchDiff
        double angle = Math.atan(Math.abs(lastTouchDiff.y) / Math.abs(lastTouchDiff.x));
        float xSpeed = (float) Math.cos(angle);
        float ySpeed = (float) Math.sin(angle);

        // Xác định hướng mặt
        if (xSpeed > ySpeed) {
            player.setFaceDir(lastTouchDiff.x > 0 ? GameConstants.Face_Dir.RIGHT : GameConstants.Face_Dir.LEFT);
        } else {
            player.setFaceDir(lastTouchDiff.y > 0 ? GameConstants.Face_Dir.DOWN : GameConstants.Face_Dir.UP);
        }

        // Điều chỉnh dấu vận tốc
        xSpeed = lastTouchDiff.x < 0 ? -xSpeed : xSpeed;
        ySpeed = lastTouchDiff.y < 0 ? -ySpeed : ySpeed;

        float deltaX = -xSpeed * baseSpeed;
        float deltaY = -ySpeed * baseSpeed;

        float deltaCameraX = -cameraX - deltaX;
        float deltaCameraY = -cameraY - deltaY;

        boolean hitWall = false;

        // PHÂN BIỆT MAP NGOÀI VÀ MAP TRONG NHÀ
        if (isOutsideMap()) {
            // ========== MAP NGOÀI (OUTSIDE) ==========
            // SỬ DỤNG HÀM RIÊNG CHO MAP NGOÀI - KHÔNG CHẶN BIÊN MAP
            if (HelpMethods.CanWalkHereOutside(player.getHitbox(), deltaCameraX, deltaCameraY, mapManager.getCurrentMap())) {
                cameraX += deltaX;
                cameraY += deltaY;
            } else {
                // Kiểm tra va chạm với obstacles riêng từng chiều
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

            // Kiểm tra game over khi ra ngoài biên map (CHỈ Ở MAP NGOÀI)
            checkPlayerOutOfBounds();

        } else {
            // ========== MAP TRONG NHÀ (INSIDE) ==========
            // SỬ DỤNG HÀM CŨ - CÓ CHẶN BIÊN MAP
            if (HelpMethods.CanWalkHere(player.getHitbox(), deltaCameraX, deltaCameraY, mapManager.getCurrentMap())) {
                cameraX += deltaX;
                cameraY += deltaY;
            } else {
                // Kiểm tra riêng từng chiều
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
            // KHÔNG kiểm tra game over khi ở trong nhà
        }
    }

    private boolean isOutsideMap() {
        // Kiểm tra xem map hiện tại có phải là map ngoài không (bao gồm cả SNOW map)
        return mapManager.getCurrentMap().getFloorType() == com.tutorial.androidgametutorial.environments.Tiles.OUTSIDE ||
               mapManager.getCurrentMap().getFloorType() == com.tutorial.androidgametutorial.environments.Tiles.SNOW;
    }

    private void checkPlayerOutOfBounds() {
        // Tính toán vị trí thế giới của người chơi
        float playerWorldX = -cameraX + player.getHitbox().centerX();
        float playerWorldY = -cameraY + player.getHitbox().centerY();

        // Lấy kích thước map
        float mapWidth = mapManager.getMaxWidthCurrentMap();
        float mapHeight = mapManager.getMaxHeightCurrentMap();

        // Kiểm tra nếu người chơi ra ngoài biên map
        if (playerWorldX < 0 || playerWorldX > mapWidth ||
                playerWorldY < 0 || playerWorldY > mapHeight) {
            // Chuyển sang màn hình game over
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
        if (isSwordSoundEnabled) { // Check the variable before playing sound
            soundPool.play(swordHitSoundId, 1, 1, 1, 0, 1f);
        }
    }

    public void setSwordSoundEnabled(boolean enabled) { // Add this method
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
        if (isSwordSoundEnabled) { // Sử dụng cùng flag với sword sound
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


    // Cập nhật và kiểm tra va chạm cho các projectile, trừ máu, hiệu ứng ....
    private void updateProjectiles(double delta) {
        for (Projectile p : projectiles) {
            if (!p.isActive()) continue;
            p.update(delta);
            // check va chạm với skeleton
            if (mapManager.getCurrentMap().getSkeletonArrayList() != null) {
                for (Skeleton s : mapManager.getCurrentMap().getSkeletonArrayList()) {
                    if (!s.isActive()) continue;
                    if (RectF.intersects(p.getHitbox(), s.getHitbox())) {
                        int halfMaxHp = s.getMaxHealth() / 2;
                        s.damageCharacter(halfMaxHp);
                        // Thêm hiệu ứng nổ tại vị trí quái bị trúng
                        explosionEffects.add(new ExplosionEffect(new PointF(s.getHitbox().centerX(), s.getHitbox().centerY())));
                        if (s.getCurrentHealth() <= 0) {
                            s.setSkeletonInactive();
                            enemyKilled(); // Sử dụng method thống nhất thay vì killCount++ trực tiếp
                            System.out.println("💀 Skeleton chết bởi projectile! Kill count: " + killCount);
                            // Chỉ drop item nếu chưa drop (tránh drop nhiều lần)
                            if (!s.hasDroppedItem()) {
                                s.setHasDroppedItem(true);
                                Item droppedItem = HelpMethods.tryDropItem(new PointF(s.getHitbox().centerX(), s.getHitbox().centerY()));
                                if (droppedItem != null) {
                                    mapManager.getCurrentMap().getItemArrayList().add(droppedItem);
                                    System.out.println("🎁 Skeleton chết bởi Throw Sword! Drop item: " + droppedItem.getItemType());
                                }
                            }
                        }
                        p.deactivate();
                        break;
                    }
                }
            }

            // check va chạm với monster
            if (mapManager.getCurrentMap().getMonsterArrayList() != null) {
                for (Monster m : mapManager.getCurrentMap().getMonsterArrayList()) {
                    if (!m.isActive()) continue;
                    if (RectF.intersects(p.getHitbox(), m.getHitbox())) {
                        int halfMaxHp = m.getMaxHealth() / 2;
                        m.damageCharacter(halfMaxHp);
                        // Thêm hiệu ứng nổ tại vị trí quái bị trúng
                        explosionEffects.add(new ExplosionEffect(new PointF(m.getHitbox().centerX(), m.getHitbox().centerY())));
                        if (m.getCurrentHealth() <= 0) {
                            m.setMonsterInactive();
                            enemyKilled(); // Sử dụng method thống nhất thay vì killCount++ trực tiếp
                            System.out.println("💀 Monster đã chết! Kill count: " + killCount);
                            // Chỉ drop item nếu chưa drop (tránh drop nhiều lần)
                            if (!m.hasDroppedItem()) {
                                m.setHasDroppedItem(true);
                                Item droppedItem = HelpMethods.tryDropItem(new PointF(m.getHitbox().centerX(), m.getHitbox().centerY()));
                                if (droppedItem != null) {
                                    mapManager.getCurrentMap().getItemArrayList().add(droppedItem);
                                    System.out.println("🎁 Monster chết bởi Throw Sword! Drop item: " + droppedItem.getItemType());
                                }
                            }
                        }
                        p.deactivate();
                        break;
                    }
                }
            }

            // check va chạm với boom
            if (mapManager.getCurrentMap().getBoomArrayList() != null) {
                for (Boom boom : mapManager.getCurrentMap().getBoomArrayList()) {
                    if (!boom.isActive()) continue;
                    if (RectF.intersects(p.getHitbox(), boom.getHitbox())) {
                        int halfMaxHp = boom.getMaxHealth() / 2;
                        boom.damageCharacter(halfMaxHp);
                        // Thêm hiệu ứng nổ tại vị trí quái bị trúng
                        explosionEffects.add(new ExplosionEffect(new PointF(boom.getHitbox().centerX(), boom.getHitbox().centerY())));
                        if (boom.getCurrentHealth() <= 0) {
                            boom.setBoomInactive();
                            enemyKilled(); // Sử dụng method thống nhất thay vì killCount++ trực tiếp
                            System.out.println("💀 Boom đã chết! Kill count: " + killCount);
                        }
                        p.deactivate();
                        break;
                    }
                }
            }

            // check projectile bay ra khỏi map
            if (p.isOutOfBounds(mapManager.getMaxWidthCurrentMap(), mapManager.getMaxHeightCurrentMap())) {
                p.deactivate();
            }
        }
        // Cập nhật hiệu ứng nổ
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
        Iterator<SparkSkill> it = sparkSkills.iterator();
        while (it.hasNext()) {
            SparkSkill sparkSkill = it.next();
            if (sparkSkill.isActive()) {
                sparkSkill.update(delta, this);
            } else {
                it.remove();
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
        // Kiểm tra xem đã đủ thời gian để chiến thắng chưa
        long currentTime = System.currentTimeMillis();
        if (currentTime - gameStartTime >= VICTORY_TIME) {
            int currentMapLevel = mapManager.getCurrentMapLevel();

            if (currentMapLevel == 1) {
                // Hoàn thành Map 1 - chuyển sang Map 2
                System.out.println("🎉 HOÀN THÀNH MAP 1! Chuyển sang Map 2 - Snow World!");
                System.out.println("📊 TỔNG KẾT MAP 1: Đã tiêu diệt " + killCount + " quái vật!");
                mapManager.progressToNextMap();

                // Reset CHỈSYSTEM chơi thời gian cho map mới - KHÔNG RESET KILLCOUNT
                gameStartTime = System.currentTimeMillis();
                // KHÔNG RESET killCount - để tích lũy số quái đã giết qua cả 2 map
                // killCount = 0; // XÓA DÒNG NÀY

                // Reset player position for new map
                float playerStartX = GAME_WIDTH / 2f;
                float playerStartY = GAME_HEIGHT / 2f;
                player.resetPosition(playerStartX, playerStartY);

                System.out.println("❄️ Bắt đầu Map 2 - Thế giới băng tuyết với nhiều quái vật hơn!");
                System.out.println("📊 Số quái đã tiêu diệt từ Map 1: " + killCount + " - Tiếp tục tích lũy!");

            } else if (currentMapLevel == 2) {
                // Hoàn thành Map 2 - hiển thị màn hình chiến thắng
                System.out.println("🏆 CHIẾN THẮNG HOÀN TOÀN! Đã hoàn thành cả 2 map với " + killCount + " quái bị tiêu diệt!");
                System.out.println("📊 FINAL KILL COUNT: " + killCount);
                game.getWinScreen().setKillCount(killCount);
                game.setCurrentGameState(Game.GameState.WIN_SCREEN);
            }
        }
    }

    public void enemyKilled() {
        killCount++;
        System.out.println("✅ Đã tiêu diệt " + killCount + " quái.");
    }

    public void setDifficulty(Game.Difficulty difficulty) {
        this.currentDifficulty = difficulty;
        System.out.println("🎮 Độ khó đã được set: " + difficulty);
    }

    public Game.Difficulty getCurrentDifficulty() {
        return currentDifficulty;
    }

    public void resetGame() {
        // Reset map progression to Map 1
        mapManager.resetToMap1();

        // Reset camera về vị trí ban đầu (giữa map)
        calcStartCameraValues();

        // Reset player về trạng thái ban đầu
        player.resetCharacterHealth();
        player.resetAnimation();

        // Reset movement state
        movePlayer = false;
        lastTouchDiff = null;

        // Clear tất cả projectiles và effects
        projectiles.clear();
        explosionEffects.clear();
        effectExplosions.clear();
        sparkSkills.clear();

        // Reset spawn timer về 0
        lastSpawnTime = 0;

        // Reset game stats for victory screen
        gameStartTime = System.currentTimeMillis();
        killCount = 0;

        // KHÔI PHỤC LẠI MAP VỀ TRẠNG THÁI BAN ĐẦU - như khi vào game lần đầu
        mapManager.resetMapToInitialState();

        System.out.println("🔄 Game đã được HOÀN TOÀN reset về trạng thái ban đầu!");
        System.out.println("📍 Camera reset về: (" + cameraX + ", " + cameraY + ")");
        System.out.println("👹 Map đã được khôi phục về trạng thái ban đầu với quái vật gốc");
        System.out.println("⏰ Timer reset: " + gameStartTime + ", Kill count reset: " + killCount);
    }
}
