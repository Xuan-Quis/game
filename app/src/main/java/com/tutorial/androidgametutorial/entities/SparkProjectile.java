package com.tutorial.androidgametutorial.entities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;

import com.tutorial.androidgametutorial.R;
import com.tutorial.androidgametutorial.helpers.GameConstants;
import com.tutorial.androidgametutorial.helpers.interfaces.BitmapMethods;
import com.tutorial.androidgametutorial.main.MainActivity;

public class SparkProjectile implements BitmapMethods {
    
    private PointF position;
    private PointF target;
    private PointF velocity;
    private RectF hitbox;
    private int damage;
    private float speed;
    private boolean active = true;
    private boolean exploding = false;
    private long explosionStartTime;
    private int currentExplosionFrame = 0;
    private long explosionDuration = 500; // 0.5 giây nổ
    private float rotation = 0f; // Góc xoay của tia năng lượng
    
    private static Bitmap sparkSprite;
    private static EffectExplosionSprites explosionSprites = new EffectExplosionSprites();
    
    static {
        // Load spark sprite một lần
        options.inScaled = false;
        Bitmap original = BitmapFactory.decodeResource(
            MainActivity.getGameContext().getResources(), 
            R.drawable.spark_preview1, 
            options
        );
        sparkSprite = Bitmap.createScaledBitmap(original, 
            GameConstants.Sprite.SIZE, 
            GameConstants.Sprite.SIZE, 
            false);
    }
    
    public SparkProjectile(PointF startPos, PointF target, int damage, float speed) {
        this.position = new PointF(startPos.x, startPos.y);
        this.target = target;
        this.damage = damage;
        this.speed = speed;
        this.hitbox = new RectF(position.x - 8, position.y - 8, position.x + 8, position.y + 8);
        
        // Tính velocity và rotation
        float dx = target.x - startPos.x;
        float dy = target.y - startPos.y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        
        if (distance > 0) {
            this.velocity = new PointF((dx / distance) * speed, (dy / distance) * speed);
            this.rotation = (float) Math.toDegrees(Math.atan2(dy, dx));
        } else {
            this.velocity = new PointF(0, 0);
            this.rotation = 0f;
        }
    }
    
    public void update(double delta) {
        if (!active || exploding) {
            if (exploding) {
                updateExplosion();
            }
            return;
        }
        
        // Di chuyển projectile
        position.x += velocity.x * delta;
        position.y += velocity.y * delta;
        
        // Cập nhật hitbox
        hitbox.set(position.x - 8, position.y - 8, position.x + 8, position.y + 8);
        
        // Kiểm tra đã đến đích chưa
        float dx = target.x - position.x;
        float dy = target.y - position.y;
        float distanceToTarget = (float) Math.sqrt(dx * dx + dy * dy);
        
        if (distanceToTarget < 10) { // Đã đến gần đích
            explode();
        }
    }
    
    private void updateExplosion() {
        long elapsed = System.currentTimeMillis() - explosionStartTime;
        
        // Cập nhật frame dựa trên thời gian
        int frameIndex = (int) ((elapsed * 7) / explosionDuration);
        if (frameIndex >= 7) {
            frameIndex = 6;
        }
        currentExplosionFrame = frameIndex;
        
        // Kết thúc nổ
        if (elapsed >= explosionDuration) {
            active = false;
        }
    }
    
    public void explode() {
        exploding = true;
        explosionStartTime = System.currentTimeMillis();
        currentExplosionFrame = 0;
    }
    
    public void render(Canvas canvas, float cameraX, float cameraY) {
        if (!active) return;
        
        if (exploding) {
            // Vẽ hiệu ứng nổ
            Bitmap explosionSprite = explosionSprites.getExplosionSprite(currentExplosionFrame);
            canvas.drawBitmap(explosionSprite, 
                position.x + cameraX - 48, 
                position.y + cameraY - 48, 
                null);
        } else {
            // Vẽ tia năng lượng với rotation
            canvas.save();
            canvas.rotate(rotation, position.x + cameraX, position.y + cameraY);
            canvas.drawBitmap(sparkSprite, 
                position.x + cameraX - 48, 
                position.y + cameraY - 48, 
                null);
            canvas.restore();
        }
    }
    
    public RectF getHitbox() {
        return hitbox;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public int getDamage() {
        return damage;
    }
}
