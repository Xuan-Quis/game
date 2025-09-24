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

    private boolean doorwayJustPassed;
    private Entity[] listOfDrawables;
    private boolean listOfEntitiesMade;

    // thêm
    private SoundPool soundPool;
    private int swordHitSoundId;
    private int playerHitWallSoundId;
   private boolean isSwordSoundEnabled = true; // Add this line


    private ArrayList<Projectile> projectiles = new ArrayList<>();
    private final Paint projectilePaint = new Paint();
   // thời điểm gây sát thương (ms)
    private ArrayList<ExplosionEffect> explosionEffects = new ArrayList<>();

    public Playing(Game game) {
        super(game);

        mapManager = new MapManager(this);
        calcStartCameraValues();

        player = new Player();

        playingUI = new PlayingUI(this);

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
        buildEntityList();
        updatePlayerMove(delta);
        player.update(delta, movePlayer);
        mapManager.setCameraValues(cameraX, cameraY);
        checkForDoorway();


        if (player.isAttacking()) if (!player.isAttackChecked()) checkPlayerAttack();

        if (mapManager.getCurrentMap().getSkeletonArrayList() != null)
            for (Skeleton skeleton : mapManager.getCurrentMap().getSkeletonArrayList())
                if (skeleton.isActive()) {
                    // Sử dụng phương thức update mới với tham số Player và camera để Skeleton có thể đuổi theo
                    skeleton.update(delta, mapManager.getCurrentMap(), player, cameraX, cameraY);
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


        sortArray();
        updateProjectiles(delta);

    }


    private void buildEntityList() {
        listOfDrawables = mapManager.getCurrentMap().getDrawableList();
        listOfDrawables[listOfDrawables.length - 1] = player;
        listOfEntitiesMade = true;
    }

    private void sortArray() {
        player.setLastCameraYValue(cameraY);
        Arrays.sort(listOfDrawables);
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

    RectF attackBoxWithoutCamera = new RectF(player.getAttackBox());
    attackBoxWithoutCamera.left -= cameraX;
    attackBoxWithoutCamera.top -= cameraY;
    attackBoxWithoutCamera.right -= cameraX;
    attackBoxWithoutCamera.bottom -= cameraY;

    if (mapManager.getCurrentMap().getSkeletonArrayList() != null) {
        for (Skeleton s : mapManager.getCurrentMap().getSkeletonArrayList()) {
            if (attackBoxWithoutCamera.intersects(
                    s.getHitbox().left,
                    s.getHitbox().top,
                    s.getHitbox().right,
                    s.getHitbox().bottom)) {

                // Trừ máu quái
                s.damageCharacter(player.getDamage());

                // 🔊 Phát âm thanh nhát chém khi trúng
                playSwordHit();

                // Nếu quái chết thì set inactive
                if (s.getCurrentHealth() <= 0) {
                    s.setSkeletonInactive();
                }
            }
        }
    }

    player.setAttackChecked(true);
}



    @Override
    public void render(Canvas c) {
        mapManager.drawTiles(c);
        if (listOfEntitiesMade)
            drawSortedEntities(c);

        playingUI.draw(c);

        // 🔥 Bổ sung: vẽ projectile
        drawProjectiles(c);
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
    public void castThrowSwordSkill() {
        player.castThrowSword(this);
    }
    private void drawSortedEntities(Canvas c) {
        for (Entity e : listOfDrawables) {
            if (e instanceof Skeleton skeleton) {
                if (skeleton.isActive()) drawCharacter(c, skeleton);
            } else if (e instanceof GameObject gameObject) {
                mapManager.drawObject(c, gameObject);
            } else if (e instanceof Building building) {
                mapManager.drawBuilding(c, building);
            } else if (e instanceof Item item) {
                mapManager.drawItem(c, item);
            } else if (e instanceof Player) {
                drawPlayer(c);
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

        float baseSpeed = (float) (delta * 300);

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

        // Kiểm tra va chạm tổng thể
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

            // Phát âm thanh nếu chạm viền
            if (hitWall) playPlayerHitWall();
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
                        if (s.getCurrentHealth() <= 0) s.setSkeletonInactive();
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
    public float getCameraX() {
        return cameraX;
    }

    public float getCameraY() {
        return cameraY;
    }

    private void playPlayerHitWall() {
        soundPool.play(playerHitWallSoundId, 1, 1, 1, 0, 1f);
    }

}
