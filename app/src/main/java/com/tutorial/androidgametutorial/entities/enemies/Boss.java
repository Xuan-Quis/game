package com.tutorial.androidgametutorial.entities.enemies;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;

// Chúng ta cần import lớp Player để Boss có thể tấn công người chơi
import com.tutorial.androidgametutorial.entities.Player;

public class Boss {

    private PointF position;
    private float speed = 2.0f;

    private Bitmap[][] currentSprites;
    private int currentFrame = 0;
    private long lastFrameTime = 0L;
    private long frameDuration = 120L; // Tốc độ hoạt ảnh (ms trên mỗi frame)

    private BossState state = BossState.IDLE;
    private boolean facingRight = true;

    // Thời gian cho các trạng thái
    private long stateStartTime = 0L;
    private long prepareDuration = 600L; // Thời gian chuẩn bị tấn công
    private long attackDuration = 800L;  // Thời gian thực hiện đòn tấn công

    // --- CÁC THUỘC TÍNH MỚI CHO VIỆC TẤN CÔNG ---
    private int attackDamage;
    private RectF attackBox;
    private boolean attackChecked; // Dùng để đảm bảo chỉ gây sát thương một lần mỗi đòn

    public Boss(PointF position) {
        this.position = position;
        setState(BossState.IDLE);
        lastFrameTime = System.currentTimeMillis();
        stateStartTime = lastFrameTime;

        // Khởi tạo giá trị sát thương và vùng tấn công
        this.attackDamage = 35; // Boss gây 35 sát thương mỗi đòn
        this.attackBox = new RectF();
    }

    private void setAnimation(BossAnimation anim) {
        if (anim == null) return;
        currentSprites = anim.getSprites(); //
        currentFrame = 0;
        lastFrameTime = System.currentTimeMillis();
    }

    /**
     * Cập nhật trạng thái của Boss.
     * Phương thức này cần được gọi liên tục trong vòng lặp game.
     * @param nowMillis Thời gian hiện tại.
     * @param targetPlayer Đối tượng người chơi để Boss tấn công.
     */
    public void update(long nowMillis, Player targetPlayer) {
        // Cập nhật frame của hoạt ảnh
        if (currentSprites != null && currentSprites.length > 0) {
            int frameCount = currentSprites[0].length;
            if (frameCount > 0 && nowMillis - lastFrameTime > frameDuration) {
                currentFrame = (currentFrame + 1) % frameCount;
                lastFrameTime = nowMillis;
            }
        }

        // Xử lý logic theo từng trạng thái của Boss
        switch (state) {
            case WALK:
                move();
                break;

            case PREPARE_ATTACK_LEFT:
            case PREPARE_ATTACK_RIGHT:
                // Nếu hết thời gian chuẩn bị, chuyển sang tấn công
                if (nowMillis - stateStartTime >= prepareDuration) {
                    if (state == BossState.PREPARE_ATTACK_LEFT) setState(BossState.ATTACK_LEFT);
                    else setState(BossState.ATTACK_RIGHT);
                }
                break;

            case ATTACK_LEFT:
            case ATTACK_RIGHT:
                // Nếu hết thời gian tấn công, quay về trạng thái đứng yên
                if (nowMillis - stateStartTime >= attackDuration) {
                    setState(BossState.IDLE);
                } else {
                    // Thực hiện logic tấn công
                    performAttack(targetPlayer);
                }
                break;

            case IDLE:
            default:
                // Đứng yên, có thể thêm logic để Boss tự di chuyển hoặc tấn công sau một khoảng thời gian
                break;
        }
    }

    private void move() {
        if (facingRight) position.x += speed;
        else position.x -= speed;
    }

    /**
     * Thực hiện logic tấn công, kiểm tra va chạm và gây sát thương.
     * @param targetPlayer Người chơi là mục tiêu.
     */
    private void performAttack(Player targetPlayer) {
        // Hoạt ảnh tấn công có 4 frame (từ 0 đến 3).
        // Chúng ta chỉ kiểm tra gây sát thương tại frame thứ 3 (index = 2) - là frame vung vũ khí mạnh nhất.
        // Biến `attackChecked` đảm bảo sát thương chỉ được tính 1 lần.
        if (!attackChecked && currentFrame == 2) {
            attackChecked = true;
            updateAttackBox();

            // Kiểm tra nếu hitbox của người chơi giao với vùng tấn công của Boss
            if (RectF.intersects(attackBox, targetPlayer.getHitbox())) {
                // Trừ máu người chơi bằng phương thức từ lớp cha Character
                targetPlayer.damageCharacter(attackDamage);
            }
        }
    }

    /**
     * Cập nhật vị trí và kích thước của vùng tấn công (hitbox) dựa trên hướng của Boss.
     */
    private void updateAttackBox() {
        float attackRangeX = 120f; // Tầm tấn công theo chiều ngang
        float attackBoxHeight = 150f; // Chiều cao của vùng tấn công
        float yOffset = 20f; // Chỉnh vị trí của vùng tấn công theo chiều dọc

        if (facingRight) {
            // Vùng tấn công bên phải của Boss
            attackBox.set(position.x + 80, position.y + yOffset, position.x + 80 + attackRangeX, position.y + yOffset + attackBoxHeight);
        } else {
            // Vùng tấn công bên trái của Boss
            attackBox.set(position.x - attackRangeX, position.y + yOffset, position.x, position.y + yOffset + attackBoxHeight);
        }
    }

    /**
     * Vẽ Boss lên màn hình.
     * @param canvas Đối tượng Canvas để vẽ.
     */
    public void draw(Canvas canvas, float cameraX, float cameraY) {
        if (currentSprites == null) return;
        Bitmap frame = null;
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
            case IDLE:
                setAnimation(BossAnimation.BOSS_IDLE);
                break;
            case WALK:
                setAnimation(BossAnimation.BOSS_WALK);
                break;
            case PREPARE_ATTACK_LEFT:
                setAnimation(BossAnimation.BOSS_PREPARE_ATTACK_LEFT);
                facingRight = false;
                break;
            case PREPARE_ATTACK_RIGHT:
                setAnimation(BossAnimation.BOSS_PREPARE_ATTACK_RIGHT);
                facingRight = true;
                break;
            case ATTACK_LEFT:
                setAnimation(BossAnimation.BOSS_ATTACK_LEFT);
                facingRight = false;
                attackChecked = false; // Reset lại cờ kiểm tra khi bắt đầu một đòn tấn công mới
                break;
            case ATTACK_RIGHT:
                setAnimation(BossAnimation.BOSS_ATTACK_RIGHT);
                facingRight = true;
                attackChecked = false; // Reset lại cờ kiểm tra khi bắt đầu một đòn tấn công mới
                break;
            default:
                setAnimation(BossAnimation.BOSS_IDLE);
                break;
        }
        currentFrame = 0;
        lastFrameTime = System.currentTimeMillis();
    }

    // Các phương thức getter/setter
    public PointF getPosition() { return position; }
    public BossState getState() { return state; }
    public void setFacingRight(boolean facingRight) { this.facingRight = facingRight; }
    public boolean isFacingRight() { return facingRight; }
    public void setSpeed(float speed) { this.speed = speed; }

    public Bitmap[][] getSprites() {
        return currentSprites;
    }

    public int getCurrentFrame() {
        return currentFrame;
    }
}