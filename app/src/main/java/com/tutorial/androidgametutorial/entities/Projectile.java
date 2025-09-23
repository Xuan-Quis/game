package com.tutorial.androidgametutorial.entities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;

import com.tutorial.androidgametutorial.main.MainActivity;
import com.tutorial.androidgametutorial.helpers.GameConstants;
import com.tutorial.androidgametutorial.R;

public class Projectile {
    private PointF pos;
    private float vx, vy;
    private int damage;
    private boolean active = true;
    private float speed;
    private float radius = 15; // bán kính va chạm

    private static Bitmap[] pulseFrames;
//    private static final int PULSE_FRAME_COUNT = 4;
//    private static final int[] PULSE_FRAME_RES_IDS = {
//        R.drawable.pulse1, R.drawable.pulse2, R.drawable.pulse3, R.drawable.pulse4
//    };
    private static final int PULSE_FRAME_COUNT = 6;
    private static final int[] PULSE_FRAME_RES_IDS = {
            R.drawable.charged1, R.drawable.charged2, R.drawable.charged3, R.drawable.charged4,
            R.drawable.charged5, R.drawable.charged6
    };
    private int currentFrame = 0;
    private long lastFrameTime = 0;
    private static final long FRAME_DURATION = 80; // ms per frame

    static {
        pulseFrames = new Bitmap[PULSE_FRAME_COUNT];
        for (int i = 0; i < PULSE_FRAME_COUNT; i++) {
            pulseFrames[i] = BitmapFactory.decodeResource(
                MainActivity.getGameContext().getResources(),
                PULSE_FRAME_RES_IDS[i]
            );
        }
    }

    public Projectile(PointF start, PointF target, int damage, float speed) {
        this.pos = new PointF(start.x, start.y);
        this.damage = damage;
        this.speed = speed;

        float dx = target.x - start.x;
        float dy = target.y - start.y;
        float len = (float) Math.sqrt(dx * dx + dy * dy);

        // tránh chia cho 0 nếu start == target
        if (len == 0f) {
            vx = 0;
            vy = 0;
        } else {
            vx = dx / len * speed;
            vy = dy / len * speed;
        }
    }

    public void update(double delta) {
        // dùng delta (giả sử delta là giây)
        pos.x += vx * (float) delta;
        pos.y += vy * (float) delta;
    }


    public void render(Canvas c, Paint paint, float cameraX, float cameraY) {
        if (!active) return;
        // Animate pulse frames
        long now = System.currentTimeMillis();
        if (now - lastFrameTime > FRAME_DURATION) {
            currentFrame = (currentFrame + 1) % PULSE_FRAME_COUNT;
            lastFrameTime = now;
        }
        Bitmap frame = pulseFrames[currentFrame];
        if (frame != null) {
            float drawX = pos.x + cameraX;
            float drawY = pos.y + cameraY;
            // Tính góc xoay dựa trên hướng bay
            float angle = (float) Math.toDegrees(Math.atan2(vy, vx));
            Matrix matrix = new Matrix();
            matrix.postTranslate(-frame.getWidth() / 2f, -frame.getHeight() / 2f); // Đưa về tâm
            matrix.postRotate(angle);
            matrix.postTranslate(drawX, drawY); // Đưa về vị trí cần vẽ
            c.drawBitmap(frame, matrix, null);
        } else {
            c.drawCircle(pos.x + cameraX, pos.y + cameraY, radius, paint);
        }
    }

    public RectF getHitbox() {
        return new RectF(pos.x - radius, pos.y - radius, pos.x + radius, pos.y + radius);
    }

    public int getDamage() {
        return damage;
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        active = false;
    }
    public boolean isOutOfBounds(int mapWidth, int mapHeight) {
        return pos.x < 0 || pos.x > mapWidth || pos.y < 0 || pos.y > mapHeight;
    }
}