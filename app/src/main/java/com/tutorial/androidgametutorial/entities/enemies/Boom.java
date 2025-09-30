package com.tutorial.androidgametutorial.entities.enemies;

import static com.tutorial.androidgametutorial.main.MainActivity.GAME_HEIGHT;
import static com.tutorial.androidgametutorial.main.MainActivity.GAME_WIDTH;

import android.graphics.PointF;

import com.tutorial.androidgametutorial.entities.Character;
import com.tutorial.androidgametutorial.entities.GameCharacters;
import com.tutorial.androidgametutorial.entities.Player;
import com.tutorial.androidgametutorial.entities.BoomSprites;
import com.tutorial.androidgametutorial.environments.GameMap;
import com.tutorial.androidgametutorial.helpers.GameConstants;
import com.tutorial.androidgametutorial.helpers.HelpMethods;

import java.util.Random;

public class Boom extends Character {
    private long lastDirChange = System.currentTimeMillis();
    private Random rand = new Random();
    private boolean moving = true, preparingAttack;
    private boolean chasing = false;
    private Player targetPlayer;
    private float cameraX, cameraY;
    private float normalSpeed = 250f; // Chậm hơn Skeleton một chút
    private float chaseSpeed = 400f;
    private float chaseRange = 150f; // Giảm từ 350f xuống 150f

    // Boom attack system
    private boolean isExploding = false;
    private long explosionStartTime;
    private long explosionDuration = 500; // 0.5 giây
    private int currentExplosionFrame = 0;
    private int[] explosionFrames = {0, 1, 2, 3, 4, 5, 6}; // boom_smile đến boom_bum_6
    private boolean explosionDamageDealt = false;
    private static BoomSprites boomSprites = new BoomSprites();
    private com.tutorial.androidgametutorial.gamestates.Playing playing;

    public Boom(PointF pos) {
        super(pos, GameCharacters.BOOM);
        setStartHealth(80); // Ít máu hơn Skeleton nhưng nguy hiểm hơn
    }

    public void update(double delta, GameMap gameMap) {
        if (moving) {
            updateMove(delta, gameMap);
            updateAnimation();
        }
        if (preparingAttack) {
            checkTimeToAttackTimer();
        }
        if (isExploding) {
            updateExplosion();
        }
    }

    public void update(double delta, GameMap gameMap, Player player, float cameraX, float cameraY) {
        this.targetPlayer = player;
        this.cameraX = cameraX;
        this.cameraY = cameraY;

        float distanceToPlayer = getDistanceToPlayer(player, cameraX, cameraY);

        if (distanceToPlayer <= chaseRange && !preparingAttack && !isExploding) {
            chasing = true;
        } else if (distanceToPlayer > chaseRange * 1.5f) {
            chasing = false;
        }

        if (moving) {
            updateMove(delta, gameMap);
            updateAnimation();
        }
        if (preparingAttack) {
            checkTimeToAttackTimer();
        }
        if (isExploding) {
            updateExplosion();
        }
    }

    private void updateExplosion() {
        long elapsed = System.currentTimeMillis() - explosionStartTime;
        
        // Cập nhật frame dựa trên thời gian
        int frameIndex = (int) ((elapsed * explosionFrames.length) / explosionDuration);
        if (frameIndex >= explosionFrames.length) {
            frameIndex = explosionFrames.length - 1;
        }
        currentExplosionFrame = explosionFrames[frameIndex];

        // Gây sát thương một lần duy nhất ở giữa quá trình nổ
        if (!explosionDamageDealt && elapsed >= explosionDuration / 2) {
            dealExplosionDamage();
            explosionDamageDealt = true;
        }

        // Kết thúc nổ
        if (elapsed >= explosionDuration) {
            isExploding = false;
            moving = true;
            explosionDamageDealt = false;
            currentExplosionFrame = 0;
            // Boom chết sau khi nổ
            setBoomInactive();
        }
    }

    private void dealExplosionDamage() {
        if (targetPlayer != null) {
            // Trừ 2 cục máu (200 damage)
            targetPlayer.damageCharacter(200);
        }
        
        // Thêm ExplosionEffect khi Boom nổ
        if (playing != null) {
            playing.addExplosionEffect(new com.tutorial.androidgametutorial.effects.ExplosionEffect(new PointF(hitbox.centerX(), hitbox.centerY())));
            playing.playBoomExplosionSound(); // Phát âm thanh nổ
        }
    }

    public void prepareAttack(Player player, float cameraX, float cameraY) {
        explosionStartTime = System.currentTimeMillis();
        isExploding = true;
        moving = false;
        preparingAttack = false;
        explosionDamageDealt = false;
        currentExplosionFrame = 0;
        turnTowardsPlayer(player, cameraX, cameraY);
    }

    private void turnTowardsPlayer(Player player, float cameraX, float cameraY) {
        float xDelta = hitbox.left - (player.getHitbox().left - cameraX);
        float yDelta = hitbox.top - (player.getHitbox().top - cameraY);

        if (Math.abs(xDelta) > Math.abs(yDelta)) {
            if (hitbox.left < (player.getHitbox().left - cameraX))
                faceDir = GameConstants.Face_Dir.RIGHT;
            else faceDir = GameConstants.Face_Dir.LEFT;
        } else {
            if (hitbox.top < (player.getHitbox().top - cameraY))
                faceDir = GameConstants.Face_Dir.DOWN;
            else faceDir = GameConstants.Face_Dir.UP;
        }
    }

    private float getDistanceToPlayer(Player player, float cameraX, float cameraY) {
        float playerX = player.getHitbox().left - cameraX;
        float playerY = player.getHitbox().top - cameraY;
        float boomX = hitbox.left;
        float boomY = hitbox.top;

        float deltaX = playerX - boomX;
        float deltaY = playerY - boomY;

        return (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }

    private void updateMove(double delta, GameMap gameMap) {
        float currentSpeed = chasing ? chaseSpeed : normalSpeed;
        float deltaChange = (float) (delta * currentSpeed);

        if (chasing && targetPlayer != null) {
            moveTowardsPlayer(deltaChange, gameMap);
        } else {
            moveRandomly(delta, gameMap, deltaChange);
        }
    }

    private void moveTowardsPlayer(float deltaChange, GameMap gameMap) {
        float playerX = targetPlayer.getHitbox().left - cameraX;
        float playerY = targetPlayer.getHitbox().top - cameraY;
        float boomX = hitbox.left;
        float boomY = hitbox.top;

        float deltaX = playerX - boomX;
        float deltaY = playerY - boomY;

        float distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        if (distance > 0) {
            deltaX = (deltaX / distance) * deltaChange;
            deltaY = (deltaY / distance) * deltaChange;
        }

        if (Math.abs(deltaX) > Math.abs(deltaY)) {
            if (HelpMethods.CanWalkHere(hitbox, deltaX, 0, gameMap)) {
                hitbox.left += deltaX;
                hitbox.right += deltaX;
                faceDir = deltaX > 0 ? GameConstants.Face_Dir.RIGHT : GameConstants.Face_Dir.LEFT;
            } else if (HelpMethods.CanWalkHere(hitbox, 0, deltaY, gameMap)) {
                hitbox.top += deltaY;
                hitbox.bottom += deltaY;
                faceDir = deltaY > 0 ? GameConstants.Face_Dir.DOWN : GameConstants.Face_Dir.UP;
            }
        } else {
            if (HelpMethods.CanWalkHere(hitbox, 0, deltaY, gameMap)) {
                hitbox.top += deltaY;
                hitbox.bottom += deltaY;
                faceDir = deltaY > 0 ? GameConstants.Face_Dir.DOWN : GameConstants.Face_Dir.UP;
            } else if (HelpMethods.CanWalkHere(hitbox, deltaX, 0, gameMap)) {
                hitbox.left += deltaX;
                hitbox.right += deltaX;
                faceDir = deltaX > 0 ? GameConstants.Face_Dir.RIGHT : GameConstants.Face_Dir.LEFT;
            }
        }
    }

    private void moveRandomly(double delta, GameMap gameMap, float deltaChange) {
        if (System.currentTimeMillis() - lastDirChange >= 3000) {
            faceDir = rand.nextInt(4);
            lastDirChange = System.currentTimeMillis();
        }

        switch (faceDir) {
            case GameConstants.Face_Dir.DOWN:
                if (HelpMethods.CanWalkHere(hitbox, 0, deltaChange, gameMap)) {
                    hitbox.top += deltaChange;
                    hitbox.bottom += deltaChange;
                } else
                    faceDir = GameConstants.Face_Dir.UP;
                break;

            case GameConstants.Face_Dir.UP:
                if (HelpMethods.CanWalkHere(hitbox, 0, -deltaChange, gameMap)) {
                    hitbox.top -= deltaChange;
                    hitbox.bottom -= deltaChange;
                } else
                    faceDir = GameConstants.Face_Dir.DOWN;
                break;

            case GameConstants.Face_Dir.RIGHT:
                if (HelpMethods.CanWalkHere(hitbox, deltaChange, 0, gameMap)) {
                    hitbox.left += deltaChange;
                    hitbox.right += deltaChange;
                } else
                    faceDir = GameConstants.Face_Dir.LEFT;
                break;

            case GameConstants.Face_Dir.LEFT:
                if (HelpMethods.CanWalkHere(hitbox, -deltaChange, 0, gameMap)) {
                    hitbox.left -= deltaChange;
                    hitbox.right -= deltaChange;
                } else
                    faceDir = GameConstants.Face_Dir.RIGHT;
                break;
        }
    }

    private void checkTimeToAttackTimer() {
        // Boom không cần timer như Skeleton, nó sẽ nổ ngay khi chạm player
    }

    public boolean isPreparingAttack() {
        return preparingAttack;
    }

    public void setBoomInactive() {
        active = false;
    }

    public boolean isChasing() {
        return chasing;
    }

    public void setChasing(boolean chasing) {
        this.chasing = chasing;
    }

    public boolean isExploding() {
        return isExploding;
    }

    public int getCurrentExplosionFrame() {
        return currentExplosionFrame;
    }

    @Override
    public int getAniIndex() {
        if (isExploding) {
            return currentExplosionFrame;
        }
        return super.getAniIndex();
    }
    
    // Override để sử dụng BoomSprites thay vì GameCharacters
    public android.graphics.Bitmap getBoomSprite() {
        if (isExploding) {
            return boomSprites.getExplosionSprite(currentExplosionFrame);
        } else {
            return boomSprites.getMoveSprite(faceDir);
        }
    }

    public void setPlaying(com.tutorial.androidgametutorial.gamestates.Playing playing) {
        this.playing = playing;
    }
}
