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
    private Game.Difficulty currentDifficulty = Game.Difficulty.EASY;
    private boolean doorwayJustPassed;
    private Entity[] listOfDrawables;
    private boolean listOfEntitiesMade;
    private SoundPool soundPool;
    private int swordHitSoundId;
    private int playerHitWallSoundId;
    private int boomExplosionSoundId;
    private boolean isSwordSoundEnabled = true;
    private ArrayList<Projectile> projectiles = new ArrayList<>();
    private final Paint projectilePaint = new Paint();
    private ArrayList<ExplosionEffect> explosionEffects = new ArrayList<>();
    private ArrayList<EffectExplosion> effectExplosions = new ArrayList<>();
    private ArrayList<SparkSkill> sparkSkills = new ArrayList<>();
    private long lastSpawnTime = 0;
    private int killCount = 0;
    private boolean isCheckingVictory = false;

    public Playing(Game game) {
        super(game);

        mapManager = new MapManager(this);
        player = new Player();
        boss = new Boss(new PointF(800f, 400f));
        playingUI = new PlayingUI(this);
        killCount = 0;
        calcStartCameraValues();
        redPaint = new Paint();
        redPaint.setStrokeWidth(1);
        redPaint.setStyle(Paint.Style.STROKE);
        redPaint.setColor(Color.RED);
        healthBarRed = new Paint();
        healthBarBlack = new Paint();

        AudioAttributes audioAttributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
        soundPool = new SoundPool.Builder().setMaxStreams(5).setAudioAttributes(audioAttributes).build();
        projectilePaint.setColor(Color.CYAN);
        projectilePaint.setStyle(Paint.Style.FILL);
        swordHitSoundId = soundPool.load(game.getContext(), R.raw.sword_slice, 1);
        playerHitWallSoundId = soundPool.load(game.getContext(), R.raw.wall_hit, 1);
        boomExplosionSoundId = soundPool.load(game.getContext(), R.raw.explosion_boom, 1);
        initHealthBars();
    }
    private void initHealthBars() {
        healthBarRed.setColor(Color.RED);
        healthBarRed.setStrokeWidth(6);
        healthBarRed.setStyle(Paint.Style.STROKE);
        healthBarBlack.setColor(Color.BLACK);
        healthBarBlack.setStrokeWidth(8);
        healthBarBlack.setStyle(Paint.Style.STROKE);
    }

    public void spawnBoss() {
        float bossX = mapManager.getMaxWidthCurrentMap() / 2f;
        float bossY = mapManager.getMaxHeightCurrentMap() / 2f;
        boss = new Boss(new PointF(bossX, bossY));
        System.out.println("🔥 BOSS ĐÃ XUẤT HIỆN! 🔥");
    }

    // BỔ SUNG: Hàm để test, bạn có thể gọi hàm này từ một nút bấm hoặc bất cứ đâu
    public void forceSpawnBoss() {
        if (boss == null) {
            spawnBoss();
        }
    }

    @Override
    public void update(double delta) {
        checkVictoryCondition();
        buildEntityList();
        updatePlayerMove(delta);
        player.update(delta, movePlayer);
        mapManager.setCameraValues(cameraX, cameraY);
        checkForDoorway();

        if (player.isAttacking() && !player.isAttackChecked()) {
            checkPlayerAttack();
        }

        updateEnemies(delta);

        if (boss != null) {
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

    private void updateEnemies(double delta) {
        if (mapManager.getCurrentMap().getSkeletonArrayList() != null)
            for (Skeleton skeleton : mapManager.getCurrentMap().getSkeletonArrayList())
                if (skeleton.isActive()) {
                    skeleton.update(delta, mapManager.getCurrentMap(), player, cameraX, cameraY, this);
                    if (skeleton.isAttacking() && !skeleton.isAttackChecked()) {
                        checkEnemyAttack(skeleton);
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
                    if (!boom.isPreparingAttack() && !boom.isExploding()) {
                        if (HelpMethods.IsPlayerCloseForAttack(boom, player, cameraY, cameraX)) {
                            boom.prepareAttack(player, cameraX, cameraY);
                        }
                    }
                }
    }

    // THAY ĐỔI: Logic thắng game hoàn toàn mới
    private void checkVictoryCondition() {
        if (isCheckingVictory) return;

        int currentMapLevel = mapManager.getCurrentMapLevel();
        boolean canProgress = false;

        // Kịch bản 1: Map có Boss
        if (boss != null) {
            if (boss.isDead()) {
                if (currentMapLevel < 3) {
                    // Thắng boss ở map < 3 -> qua màn
                    canProgress = true;
                } else {
                    // Thắng boss ở map 3 -> thắng game
                    isCheckingVictory = true;
                    game.getWinScreen().setKillCount(killCount);
                    game.setCurrentGameState(Game.GameState.WIN_SCREEN);
                    return; // Kết thúc hàm
                }
            }
        }
        // Kịch bản 2: Map không có Boss (và không phải map cuối)
        else if (currentMapLevel < 3) {
            if (areAllEnemiesDefeated()) {
                canProgress = true;
            }
        }

        // Xử lý qua màn nếu đủ điều kiện
        if (canProgress) {
            isCheckingVictory = true;
            mapManager.progressToNextMap();
            player.resetPosition(GAME_WIDTH / 2f, GAME_HEIGHT / 2f);
//            boss = null; // Xóa boss cũ khi qua màn
            isCheckingVictory = false;
        }
    }

    private void checkPlayerAttack() {
        RectF attackBoxWorld = new RectF(player.getAttackBox());
        attackBoxWorld.offset(-cameraX, -cameraY);

        // Tấn công quái thường
        if (mapManager.getCurrentMap().getSkeletonArrayList() != null)
            for (Skeleton s : mapManager.getCurrentMap().getSkeletonArrayList())
                if (s.isActive() && RectF.intersects(attackBoxWorld, s.getHitbox())) {
                    s.damageCharacter(player.getDamage());
                    playSwordHit();
                    if (s.getCurrentHealth() <= 0) {
                        s.setSkeletonInactive();
                        enemyKilled();
                        if (!s.hasDroppedItem()) {
                            s.setHasDroppedItem(true);
                            Item droppedItem = HelpMethods.tryDropItem(new PointF(s.getHitbox().centerX(), s.getHitbox().centerY()));
                            if (droppedItem != null)
                                mapManager.getCurrentMap().getItemArrayList().add(droppedItem);
                        }
                    }
                }
        if (mapManager.getCurrentMap().getBoomArrayList() != null)
            for (Boom b : mapManager.getCurrentMap().getBoomArrayList())
                if (b.isActive() && RectF.intersects(attackBoxWorld, b.getHitbox())) {
                    b.damageCharacter(player.getDamage());
                    playSwordHit();
                    if (b.getCurrentHealth() <= 0) {
                        b.setBoomInactive();
                        enemyKilled();
                    }
                }

        // BỔ SUNG: Tấn công Boss
        if (boss != null && !boss.isDead() && RectF.intersects(attackBoxWorld, boss.getHitbox())) {
            boss.damageCharacter(player.getDamage());
            playSwordHit();
        }

        player.setAttackChecked(true);
    }

    @Override
    public void render(Canvas c) {
        mapManager.drawTiles(c);
        if (listOfEntitiesMade)
            drawSortedEntities(c);

        if (boss != null) {
            boss.draw(c, cameraX, cameraY);
        }

        playingUI.draw(c);
        drawProjectiles(c);
        drawEffectExplosions(c);
        drawSparkSkills(c);
        drawItems(c);
    }
    private void calcStartCameraValues() {
        cameraX = GAME_WIDTH / 2f - mapManager.getMaxWidthCurrentMap() / 2f;
        cameraY = GAME_HEIGHT / 2f - mapManager.getMaxHeightCurrentMap() / 2f;
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
        killCount = 0;
        mapManager.resetMapToInitialState();
//        boss = null;
        isCheckingVictory = false;
    }


    // ==================================================================
    // CÁC HÀM CÒN LẠI (GIỮ NGUYÊN, không cần thay đổi)
    // ==================================================================

    private boolean areAllEnemiesDefeated() {
        if (mapManager.getCurrentMapLevel() == 3) return false;
        if (!isOutsideMap()) return false;
        ArrayList<Skeleton> skeletons = mapManager.getCurrentMap().getSkeletonArrayList();
        if (skeletons != null) for (Skeleton s : skeletons) if (s.isActive()) return false;
        ArrayList<Boom> booms = mapManager.getCurrentMap().getBoomArrayList();
        if (booms != null) for (Boom b : booms) if (b.isActive()) return false;
        return true;
    }

    private void spawnEnemies() {
        if (mapManager.getCurrentMapLevel() == 3) return;
        if (!isOutsideMap()) return;
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSpawnTime >= 3000) {
            lastSpawnTime = currentTime;
            if (mapManager.getCurrentMap().getSkeletonArrayList() == null || mapManager.getCurrentMap().getBoomArrayList() == null)
                return;
            float spawnX = -cameraX + (float) (Math.random() * GAME_WIDTH);
            float spawnY = -cameraY + (float) (Math.random() * GAME_HEIGHT);
            if (Math.random() < 0.6) {
                mapManager.getCurrentMap().getSkeletonArrayList().add(new Skeleton(new PointF(spawnX, spawnY), GameCharacters.SKELETON));
            } else {
                Boom boom = new Boom(new PointF(spawnX, spawnY));
                boom.setPlaying(this);
                mapManager.getCurrentMap().getBoomArrayList().add(boom);
            }
        }
    }

    private void buildEntityList() {
        listOfDrawables = mapManager.getCurrentMap().getDrawableList();
        Entity[] newList = Arrays.copyOf(listOfDrawables, listOfDrawables.length + 1);
        newList[newList.length - 2] = player;
        listOfDrawables = newList;
        listOfEntitiesMade = true;
    }

    private void sortArray() {
        player.setLastCameraYValue(cameraY);
    }

    public void setCameraValues(PointF cameraPos) {
        this.cameraX = cameraPos.x;
        this.cameraY = cameraPos.y;
    }

    private void checkForDoorway() {
        Doorway d = mapManager.isPlayerOnDoorway(player.getHitbox());
        if (d != null) {
            if (!doorwayJustPassed) mapManager.changeMap(d.getDoorwayConnectedTo());
        } else doorwayJustPassed = false;
    }

    public void setDoorwayJustPassed(boolean val) {
        this.doorwayJustPassed = val;
    }

    private void checkEnemyAttack(Character c) {
        c.updateWepHitbox();
        RectF pHitbox = new RectF(player.getHitbox());
        pHitbox.offset(-cameraX, -cameraY);
        if (RectF.intersects(c.getAttackBox(), pHitbox)) {
            player.damageCharacter(c.getDamage());
            checkPlayerDead();
        }
        c.setAttackChecked(true);
    }

    private void checkPlayerDead() {
        if (player.getCurrentHealth() <= 0) {
            game.setCurrentGameState(Game.GameState.DEATH_SCREEN);
            player.resetCharacterHealth();
        }
    }

    private void updateItems(double delta) {
        if (mapManager.getCurrentMap().getItemArrayList() != null) {
            Iterator<Item> it = mapManager.getCurrentMap().getItemArrayList().iterator();
            while (it.hasNext()) {
                Item item = it.next();
                if (!item.isActive()) {
                    it.remove();
                    continue;
                }
                item.update(delta);
                RectF itemScreenHitbox = new RectF(item.getHitbox());
                itemScreenHitbox.offset(cameraX, cameraY);
                if (RectF.intersects(itemScreenHitbox, player.getHitbox())) {
                    if (item.getItemType() == com.tutorial.androidgametutorial.entities.items.Items.MEDIPACK)
                        player.useMedipack();
                    else if (item.getItemType() == com.tutorial.androidgametutorial.entities.items.Items.FISH)
                        player.useFish();
                    else if (item.getItemType() == com.tutorial.androidgametutorial.entities.items.Items.EMPTY_POT)
                        player.useEmptyPot();
                    item.deactivate();
                    it.remove();
                }
            }
        }
    }

    private void drawProjectiles(Canvas c) {
        for (Projectile p : projectiles)
            if (p.isActive()) p.render(c, projectilePaint, cameraX, cameraY);
        for (ExplosionEffect e : explosionEffects) e.render(c, cameraX, cameraY);
    }

    private void drawEffectExplosions(Canvas c) {
        for (EffectExplosion e : new ArrayList<>(effectExplosions))
            if (e.isActive()) e.render(c, cameraX, cameraY);
    }

    private void drawSparkSkills(Canvas c) {
        for (SparkSkill s : new ArrayList<>(sparkSkills))
            if (s.isActive()) s.render(c, cameraX, cameraY);
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
            if (e instanceof Skeleton s && s.isActive()) drawCharacter(c, s);
            else if (e instanceof GameObject g) mapManager.drawObject(c, g);
            else if (e instanceof Building b) mapManager.drawBuilding(c, b);
            else if (e instanceof Item i) mapManager.drawItem(c, i);
            else if (e instanceof Player) drawPlayer(c);
            else if (e instanceof Boom b && b.isActive()) drawBoom(c, b);
        }
    }

    private void drawPlayer(Canvas c) {
        c.drawBitmap(Weapons.SHADOW.getWeaponImg(), player.getHitbox().left, player.getHitbox().bottom - 5 * GameConstants.Sprite.SCALE_MULTIPLIER, null);
        c.drawBitmap(player.getGameCharType().getSprite(player.getAniIndex(), player.getFaceDir()), player.getHitbox().left - X_DRAW_OFFSET, player.getHitbox().top - GameConstants.Sprite.Y_DRAW_OFFSET, null);
        if (player.isAttacking()) drawWeapon(c, player);
    }

    private void drawWeapon(Canvas c, Character character) {
        c.rotate(character.getWepRot(), character.getAttackBox().left, character.getAttackBox().top);
        c.drawBitmap(Weapons.BIG_SWORD.getWeaponImg(), character.getAttackBox().left + character.wepRotAdjustLeft(), character.getAttackBox().top + character.wepRotAdjustTop(), null);
        c.rotate(character.getWepRot() * -1, character.getAttackBox().left, character.getAttackBox().top);
    }

    private void drawEnemyWeapon(Canvas c, Character character) {
        c.rotate(character.getWepRot(), character.getAttackBox().left + cameraX, character.getAttackBox().top + cameraY);
        c.drawBitmap(Weapons.BIG_SWORD.getWeaponImg(), character.getAttackBox().left + cameraX + character.wepRotAdjustLeft(), character.getAttackBox().top + cameraY + character.wepRotAdjustTop(), null);
        c.rotate(character.getWepRot() * -1, character.getAttackBox().left + cameraX, character.getAttackBox().top + cameraY);
    }

    public void drawCharacter(Canvas c, Character character) {
        c.drawBitmap(Weapons.SHADOW.getWeaponImg(), character.getHitbox().left + cameraX, character.getHitbox().bottom - 5 * GameConstants.Sprite.SCALE_MULTIPLIER + cameraY, null);
        c.drawBitmap(character.getGameCharType().getSprite(character.getAniIndex(), character.getFaceDir()), character.getHitbox().left + cameraX - X_DRAW_OFFSET, character.getHitbox().top + cameraY - GameConstants.Sprite.Y_DRAW_OFFSET, null);
        if (character.isAttacking()) drawEnemyWeapon(c, character);
        if (character.getCurrentHealth() < character.getMaxHealth()) drawHealthBar(c, character);
    }

    private void drawBoom(Canvas c, Boom boom) {
        c.drawBitmap(Weapons.SHADOW.getWeaponImg(), boom.getHitbox().left + cameraX, boom.getHitbox().bottom - 5 * GameConstants.Sprite.SCALE_MULTIPLIER + cameraY, null);
        c.drawBitmap(boom.getBoomSprite(), boom.getHitbox().left + cameraX - X_DRAW_OFFSET, boom.getHitbox().top + cameraY - GameConstants.Sprite.Y_DRAW_OFFSET, null);
        if (boom.getCurrentHealth() < boom.getMaxHealth()) drawHealthBar(c, boom);
    }

    private void drawItems(Canvas c) {
        if (mapManager.getCurrentMap().getItemArrayList() != null)
            for (Item i : mapManager.getCurrentMap().getItemArrayList())
                if (i.isActive()) i.render(c, cameraX, cameraY);
    }

    private void drawHealthBar(Canvas c, Character character) {
        float barWidth = character.getHitbox().width() * ((float) character.getCurrentHealth() / character.getMaxHealth());
        c.drawLine(character.getHitbox().left + cameraX, character.getHitbox().top + cameraY - 5 * GameConstants.Sprite.SCALE_MULTIPLIER, character.getHitbox().right + cameraX, character.getHitbox().top + cameraY - 5 * GameConstants.Sprite.SCALE_MULTIPLIER, healthBarBlack);
        c.drawLine(character.getHitbox().left + cameraX, character.getHitbox().top + cameraY - 5 * GameConstants.Sprite.SCALE_MULTIPLIER, character.getHitbox().left + cameraX + barWidth, character.getHitbox().top + cameraY - 5 * GameConstants.Sprite.SCALE_MULTIPLIER, healthBarRed);
    }

    private void updatePlayerMove(double delta) {
        if (!movePlayer) return;
        float baseSpeed = (float) (delta * 300 * player.getSpeedMultiplier());
        double angle = Math.atan(Math.abs(lastTouchDiff.y) / Math.abs(lastTouchDiff.x));
        float xSpeed = (float) Math.cos(angle);
        float ySpeed = (float) Math.sin(angle);
        if (xSpeed > ySpeed)
            player.setFaceDir(lastTouchDiff.x > 0 ? GameConstants.Face_Dir.RIGHT : GameConstants.Face_Dir.LEFT);
        else
            player.setFaceDir(lastTouchDiff.y > 0 ? GameConstants.Face_Dir.DOWN : GameConstants.Face_Dir.UP);
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
                if (HelpMethods.CanWalkHereUpDownOutside(player.getHitbox(), deltaCameraY, -cameraX, mapManager.getCurrentMap()))
                    cameraY += deltaY;
                else hitWall = true;
                if (HelpMethods.CanWalkHereLeftRightOutside(player.getHitbox(), deltaCameraX, -cameraY, mapManager.getCurrentMap()))
                    cameraX += deltaX;
                else hitWall = true;
                if (hitWall) playPlayerHitWall();
            }
            checkPlayerOutOfBounds();
        } else {
            if (HelpMethods.CanWalkHere(player.getHitbox(), deltaCameraX, deltaCameraY, mapManager.getCurrentMap())) {
                cameraX += deltaX;
                cameraY += deltaY;
            } else {
                if (HelpMethods.CanWalkHereUpDown(player.getHitbox(), deltaCameraY, -cameraX, mapManager.getCurrentMap()))
                    cameraY += deltaY;
                else hitWall = true;
                if (HelpMethods.CanWalkHereLeftRight(player.getHitbox(), deltaCameraX, -cameraY, mapManager.getCurrentMap()))
                    cameraX += deltaX;
                else hitWall = true;
                if (hitWall) playPlayerHitWall();
            }
        }
    }

    private boolean isOutsideMap() {
        return mapManager.getCurrentMap().getFloorType() == com.tutorial.androidgametutorial.environments.Tiles.OUTSIDE || mapManager.getCurrentMap().getFloorType() == com.tutorial.androidgametutorial.environments.Tiles.SNOW;
    }

    private void checkPlayerOutOfBounds() {
        float pX = -cameraX + player.getHitbox().centerX();
        float pY = -cameraY + player.getHitbox().centerY();
        float mW = mapManager.getMaxWidthCurrentMap();
        float mH = mapManager.getMaxHeightCurrentMap();
        if (pX < 0 || pX > mW || pY < 0 || pY > mH)
            game.setCurrentGameState(Game.GameState.DEATH_SCREEN);
    }

    public void setGameStateToMenu() {
        game.setCurrentGameState(Game.GameState.MENU);
    }

    public void setPlayerMoveTrue(PointF diff) {
        movePlayer = true;
        this.lastTouchDiff = diff;
    }

    public void setPlayerMoveFalse() {
        movePlayer = false;
        player.resetAnimation();
    }

    @Override
    public void touchEvents(MotionEvent e) {
        playingUI.touchEvents(e);
    }

    public Player getPlayer() {
        return player;
    }

    public PlayingUI getPlayingUI() {
        return playingUI;
    }

    private void playSwordHit() {
        if (isSwordSoundEnabled) soundPool.play(swordHitSoundId, 1, 1, 1, 0, 1f);
    }

    public void setSwordSoundEnabled(boolean e) {
        isSwordSoundEnabled = e;
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

    public void addEffectExplosion(EffectExplosion e) {
        effectExplosions.add(e);
    }

    public void addSparkSkill(SparkSkill s) {
        sparkSkills.add(s);
    }

    public void addExplosionEffect(ExplosionEffect e) {
        explosionEffects.add(e);
    }

    public void playBoomExplosionSound() {
        if (isSwordSoundEnabled) soundPool.play(boomExplosionSoundId, 1, 1, 1, 0, 1f);
    }

    public Skeleton findNearestSkeleton(float px, float py, float range) {
        Skeleton nearest = null;
        float minDistSq = range * range;
        if (mapManager.getCurrentMap().getSkeletonArrayList() != null)
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
        return nearest;
    }

    private void updateProjectiles(double delta) {
        for (Projectile p : projectiles) {
            if (!p.isActive()) continue;
            p.update(delta);
            if (mapManager.getCurrentMap().getSkeletonArrayList() != null)
                for (Skeleton s : mapManager.getCurrentMap().getSkeletonArrayList()) {
                    if (!s.isActive()) continue;
                    if (RectF.intersects(p.getHitbox(), s.getHitbox())) {
                        s.damageCharacter(s.getMaxHealth() / 2);
                        explosionEffects.add(new ExplosionEffect(new PointF(s.getHitbox().centerX(), s.getHitbox().centerY())));
                        if (s.getCurrentHealth() <= 0) {
                            s.setSkeletonInactive();
                            enemyKilled();
                            if (!s.hasDroppedItem()) {
                                s.setHasDroppedItem(true);
                                Item droppedItem = HelpMethods.tryDropItem(new PointF(s.getHitbox().centerX(), s.getHitbox().centerY()));
                                if (droppedItem != null)
                                    mapManager.getCurrentMap().getItemArrayList().add(droppedItem);
                            }
                        }
                        p.deactivate();
                        break;
                    }
                }
            if (mapManager.getCurrentMap().getBoomArrayList() != null)
                for (Boom b : mapManager.getCurrentMap().getBoomArrayList()) {
                    if (!b.isActive()) continue;
                    if (RectF.intersects(p.getHitbox(), b.getHitbox())) {
                        b.damageCharacter(b.getMaxHealth() / 2);
                        explosionEffects.add(new ExplosionEffect(new PointF(b.getHitbox().centerX(), b.getHitbox().centerY())));
                        if (b.getCurrentHealth() <= 0) {
                            b.setBoomInactive();
                            enemyKilled();
                        }
                        p.deactivate();
                        break;
                    }
                }
            if (p.isOutOfBounds(mapManager.getMaxWidthCurrentMap(), mapManager.getMaxHeightCurrentMap()))
                p.deactivate();
        }
        Iterator<ExplosionEffect> it = explosionEffects.iterator();
        while (it.hasNext()) {
            ExplosionEffect e = it.next();
            e.update();
            if (!e.isActive()) it.remove();
        }
    }

    private void updateEffectExplosions(double delta) {
        Iterator<EffectExplosion> it = effectExplosions.iterator();
        while (it.hasNext()) {
            EffectExplosion e = it.next();
            if (e.isActive()) e.update(delta, this);
            else it.remove();
        }
    }

    private void updateSparkSkills(double delta) {
        ArrayList<SparkSkill> copy = new ArrayList<>(sparkSkills);
        for (SparkSkill s : copy) {
            if (s.isActive()) s.update(delta, this);
            else sparkSkills.remove(s);
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

    public void enemyKilled() {
        killCount++;
    }

    public void setDifficulty(Game.Difficulty d) {
        this.currentDifficulty = d;
    }

    public Game.Difficulty getCurrentDifficulty() {
        return currentDifficulty;
    }
}