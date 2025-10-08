package com.tutorial.androidgametutorial.entities.enemies;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import com.tutorial.androidgametutorial.entities.Player;
import com.tutorial.androidgametutorial.helpers.GameConstants;

public class Boss {

    private PointF position;
    private float speed = 1.0f;
    private Bitmap[][] currentSprites;
    private int currentFrame = 0;
    private long lastFrameTime = 0L;
    private long frameDuration = 120L;
    private BossState state = BossState.IDLE;
    private boolean facingRight = true;
    private long stateStartTime = 0L;
    private long prepareDuration = 600L;
    private long attackDuration = 800L;
    private int attackDamage;
    private RectF attackBox;
    private boolean attackChecked;

    // BỔ SUNG: Hệ thống máu và trạng thái cho Boss
    private int maxHealth;
    private int currentHealth;
    private boolean active = true;
    private RectF hitbox;

    public Boss(PointF position) {
        this.position = position;
        setState(BossState.IDLE);
        lastFrameTime = System.currentTimeMillis();
        stateStartTime = lastFrameTime;

        this.attackDamage = 35;
        this.attackBox = new RectF();

        // BỔ SUNG: Khởi tạo máu và hitbox
        setStartHealth(1000); // Boss có 1000 máu
        // Kích thước hitbox của Boss, bạn có thể điều chỉnh
        float bossWidth = GameConstants.Sprite.SIZE * 1.5f;
        float bossHeight = GameConstants.Sprite.SIZE * 1.5f;
        this.hitbox = new RectF(position.x, position.y, position.x + bossWidth, position.y + bossHeight);
    }

    // BỔ SUNG: Các hàm quản lý máu
    public void setStartHealth(int health) {
        this.maxHealth = health;
        this.currentHealth = this.maxHealth;
    }

    public void damageCharacter(int damage) {
        this.currentHealth -= damage;
        if (this.currentHealth <= 0) {
            this.currentHealth = 0;
            this.active = false; // Boss chết
            System.out.println("🎉 BOSS ĐÃ BỊ TIÊU DIỆT! 🎉");
        }
    }

    public boolean isDead() {
        return !active;
    }

    public int getCurrentHealth() {
        return currentHealth;
    }

    public RectF getHitbox() {
        return hitbox;
    }
    public void update(long nowMillis, Player targetPlayer) {
        if (!active) return;

        hitbox.offsetTo(position.x, position.y);

        // Cập nhật hoạt ảnh
        if (currentSprites != null && currentSprites.length > 0) {
            int frameCount = currentSprites[0].length;
            if (frameCount > 0 && nowMillis - lastFrameTime > frameDuration) {
                currentFrame = (currentFrame + 1) % frameCount;
                lastFrameTime = nowMillis;
            }
        }

        // --- Logic AI Mới ---
        float playerWorldX = targetPlayer.getHitbox().centerX();
        float playerWorldY = targetPlayer.getHitbox().centerY();
        float bossWorldX = this.hitbox.centerX();
        float bossWorldY = this.hitbox.centerY();

        float dx = playerWorldX - bossWorldX;
        float dy = playerWorldY - bossWorldY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        // Xử lý logic theo trạng thái
        switch (state) {
            case IDLE:
            case WALK:
                // Nếu người chơi trong tầm tấn công, chuẩn bị tấn công
                if (distance < 300f) {
                    if (playerWorldX > bossWorldX) setState(BossState.PREPARE_ATTACK_RIGHT);
                    else setState(BossState.PREPARE_ATTACK_LEFT);
                }
                // Nếu người chơi ở xa hơn, đuổi theo
                else {
                    setState(BossState.WALK);
                    moveTowardsPlayer(targetPlayer);
                }
                break;

            case PREPARE_ATTACK_LEFT:
            case PREPARE_ATTACK_RIGHT:
                if (nowMillis - stateStartTime >= prepareDuration) {
                    if (state == BossState.PREPARE_ATTACK_LEFT) setState(BossState.ATTACK_LEFT);
                    else setState(BossState.ATTACK_RIGHT);
                }
                break;

            case ATTACK_LEFT:
            case ATTACK_RIGHT:
                if (nowMillis - stateStartTime >= attackDuration) {
                    setState(BossState.IDLE);
                } else {
                    performAttack(targetPlayer);
                }
                break;
        }
    }
    private void moveTowardsPlayer(Player targetPlayer) {
        float playerX = targetPlayer.getHitbox().centerX();
        float playerY = targetPlayer.getHitbox().centerY();
        float bossX = this.hitbox.centerX();
        float bossY = this.hitbox.centerY();

        float deltaX = playerX - bossX;
        float deltaY = playerY - bossY;

        float distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        if (distance > 0) {
            // Chuẩn hóa vector và nhân với tốc độ
            float moveX = (deltaX / distance) * speed;
            float moveY = (deltaY / distance) * speed;

            position.x += moveX;
            position.y += moveY;
        }

        // Cập nhật hướng mặt của Boss
        if (Math.abs(deltaX) > Math.abs(deltaY)) {
            facingRight = deltaX > 0;
        }
    }

    private void performAttack(Player targetPlayer) {
        if (!attackChecked && currentFrame == 2) {
            attackChecked = true;
            updateAttackBox(targetPlayer);

            // Chuyển hitbox của người chơi từ screen-space về world-space để so sánh
            RectF playerWorldHitbox = new RectF(targetPlayer.getHitbox());
            // Giả sử hitbox của player trong Playing là screen-space, cần trừ đi camera
            // Nhưng trong logic này, chúng ta sẽ tạo attack box của boss ở world-space
            // và so sánh với hitbox của player cũng ở world-space

            // Lỗi logic cũ: attackBox của Boss đang so sánh với hitbox của Player trên màn hình.
            // Cần sửa lại để cả 2 đều ở world space.
            // Hitbox của player đã ở world space (do không cộng camera), nên attackBox của Boss cũng phải ở world space.
            if (RectF.intersects(attackBox, playerWorldHitbox)) {
                targetPlayer.damageCharacter(attackDamage);
            }
        }
    }

    private void updateAttackBox(Player targetPlayer) {
        float attackRangeX = 120f;
        float attackBoxHeight = 150f;
        float yOffset = 20f;

        // Tọa độ world-space của player
        float playerWorldX = targetPlayer.getHitbox().centerX();

        // Quyết định hướng dựa trên vị trí của player
        if (playerWorldX > this.hitbox.centerX()) {
            facingRight = true;
        } else {
            facingRight = false;
        }

        if (facingRight) {
            attackBox.set(hitbox.right, hitbox.top + yOffset, hitbox.right + attackRangeX, hitbox.top + yOffset + attackBoxHeight);
        } else {
            attackBox.set(hitbox.left - attackRangeX, hitbox.top + yOffset, hitbox.left, hitbox.top + yOffset + attackBoxHeight);
        }
    }

    public void draw(Canvas canvas, float cameraX, float cameraY) {
        // BỔ SUNG: Nếu Boss không active, không vẽ
        if (!active) return;

        if (currentSprites == null) return;
        Bitmap frame;
        int row = 0;
        int cols = currentSprites[row].length;
        int index = (cols == 0) ? 0 : (currentFrame % cols);
        frame = currentSprites[row][index];

        if (frame != null) {
            canvas.drawBitmap(frame, position.x + cameraX, position.y + cameraY, null);
        }
    }

    public void setState(BossState newState) {
        if (newState == null || state == newState) return;
        state = newState;
        stateStartTime = System.currentTimeMillis();
        switch (newState) {
            case IDLE: setAnimation(BossAnimation.BOSS_IDLE); break;
            case WALK: setAnimation(BossAnimation.BOSS_WALK); break;
            case PREPARE_ATTACK_LEFT: setAnimation(BossAnimation.BOSS_PREPARE_ATTACK_LEFT); facingRight = false; break;
            case PREPARE_ATTACK_RIGHT: setAnimation(BossAnimation.BOSS_PREPARE_ATTACK_RIGHT); facingRight = true; break;
            case ATTACK_LEFT: setAnimation(BossAnimation.BOSS_ATTACK_LEFT); facingRight = false; attackChecked = false; break;
            case ATTACK_RIGHT: setAnimation(BossAnimation.BOSS_ATTACK_RIGHT); facingRight = true; attackChecked = false; break;
            default: setAnimation(BossAnimation.BOSS_IDLE); break;
        }
        currentFrame = 0;
        lastFrameTime = System.currentTimeMillis();
    }

    private void setAnimation(BossAnimation anim) {
        if (anim == null) return;
        currentSprites = anim.getSprites();
        currentFrame = 0;
    }

    public PointF getPosition() { return position; }
    public BossState getState() { return state; }
}