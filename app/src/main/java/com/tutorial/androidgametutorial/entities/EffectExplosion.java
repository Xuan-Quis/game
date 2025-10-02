package com.tutorial.androidgametutorial.entities;

import android.graphics.PointF;
import android.graphics.RectF;

import com.tutorial.androidgametutorial.entities.enemies.Boom;
import com.tutorial.androidgametutorial.entities.enemies.Monster;
import com.tutorial.androidgametutorial.entities.enemies.Skeleton;
import com.tutorial.androidgametutorial.gamestates.Playing;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EffectExplosion {
    
    private PointF startPos;
    private List<ExplosionProjectile> projectiles;
    private long startTime;
    private long duration = 2000; // 2 giây tồn tại
    private int damage = 50;
    private float range = 300f; // Tầm bắn
    private int projectileCount = 2; // Số lượng projectile bắn ra
    private boolean active = true;
    private static EffectExplosionSprites sprites = new EffectExplosionSprites();
    private Random random = new Random();
    
    public EffectExplosion(PointF startPos) {
        this.startPos = startPos;
        this.startTime = System.currentTimeMillis();
        this.projectiles = new ArrayList<>();
        
        // Tạo các projectile bắn random hướng
        createRandomProjectiles();
    }
    
    private void createRandomProjectiles() {
        for (int i = 0; i < projectileCount; i++) {
            // Random góc từ 0 đến 360 độ
            float angle = random.nextFloat() * 360f;
            // Random khoảng cách từ 50% đến 100% range
            float distance = range * (0.5f + random.nextFloat() * 0.5f);
            
            // Tính vị trí đích
            float targetX = startPos.x + (float) (Math.cos(Math.toRadians(angle)) * distance);
            float targetY = startPos.y + (float) (Math.sin(Math.toRadians(angle)) * distance);
            
            ExplosionProjectile projectile = new ExplosionProjectile(
                new PointF(startPos.x, startPos.y),
                new PointF(targetX, targetY),
                damage,
                200f // tốc độ bay
            );
            
            projectiles.add(projectile);
        }
    }
    
    public void update(double delta, Playing playing) {
        if (!active) return;
        
        // Cập nhật tất cả projectiles
        for (ExplosionProjectile projectile : projectiles) {
            if (projectile.isActive()) {
                projectile.update(delta);
                checkProjectileCollision(projectile, playing);
            }
        }
        
        // Kiểm tra thời gian tồn tại
        if (System.currentTimeMillis() - startTime >= duration) {
            // Kích hoạt tất cả projectiles còn lại
            for (ExplosionProjectile projectile : projectiles) {
                if (projectile.isActive()) {
                    projectile.explode();
                }
            }
            active = false;
        }
    }
    
    private void checkProjectileCollision(ExplosionProjectile projectile, Playing playing) {
        // Kiểm tra va chạm với Skeleton
        if (playing.getMapManager().getCurrentMap().getSkeletonArrayList() != null) {
            for (Skeleton skeleton : playing.getMapManager().getCurrentMap().getSkeletonArrayList()) {
                if (!skeleton.isActive()) continue;
                if (RectF.intersects(projectile.getHitbox(), skeleton.getHitbox())) {
                    skeleton.damageCharacter(damage);
                    if (skeleton.getCurrentHealth() <= 0) {
                        skeleton.setSkeletonInactive();
                        playing.enemyKilled(); // THÊM DÒNG NÀY để tăng killCount
                        System.out.println("💀 Skeleton chết bởi EffectExplosion! Kill count tăng!");
                        // Thử drop item khi skeleton chết bởi EffectExplosion
                        com.tutorial.androidgametutorial.entities.items.Item droppedItem = com.tutorial.androidgametutorial.helpers.HelpMethods.tryDropItem(new PointF(skeleton.getHitbox().centerX(), skeleton.getHitbox().centerY()));
                        if (droppedItem != null) {
                            playing.getMapManager().getCurrentMap().getItemArrayList().add(droppedItem);
                            System.out.println("🎁 Skeleton chết bởi EffectExplosion! Drop item: " + droppedItem.getItemType());
                        }
                    }
                    projectile.explode();
                    break;
                }
            }
        }
        
        // Kiểm tra va chạm với Monster
        if (playing.getMapManager().getCurrentMap().getMonsterArrayList() != null) {
            for (Monster monster : playing.getMapManager().getCurrentMap().getMonsterArrayList()) {
                if (!monster.isActive()) continue;
                if (RectF.intersects(projectile.getHitbox(), monster.getHitbox())) {
                    monster.damageCharacter(damage);
                    if (monster.getCurrentHealth() <= 0) {
                        monster.setMonsterInactive();
                        playing.enemyKilled(); // THÊM DÒNG NÀY để tăng killCount
                        System.out.println("💀 Monster chết bởi EffectExplosion! Kill count tăng!");
                        // Thử drop item khi monster chết bởi EffectExplosion
                        com.tutorial.androidgametutorial.entities.items.Item droppedItem = com.tutorial.androidgametutorial.helpers.HelpMethods.tryDropItem(new PointF(monster.getHitbox().centerX(), monster.getHitbox().centerY()));
                        if (droppedItem != null) {
                            playing.getMapManager().getCurrentMap().getItemArrayList().add(droppedItem);
                            System.out.println("🎁 Monster chết bởi EffectExplosion! Drop item: " + droppedItem.getItemType());
                        }
                    }
                    projectile.explode();
                    break;
                }
            }
        }
        
        // Kiểm tra va chạm với Boom
        if (playing.getMapManager().getCurrentMap().getBoomArrayList() != null) {
            for (Boom boom : playing.getMapManager().getCurrentMap().getBoomArrayList()) {
                if (!boom.isActive()) continue;
                if (RectF.intersects(projectile.getHitbox(), boom.getHitbox())) {
                    boom.damageCharacter(damage);
                    if (boom.getCurrentHealth() <= 0) {
                        boom.setBoomInactive();
                        playing.enemyKilled(); // THÊM DÒNG NÀY để tăng killCount
                        System.out.println("💀 Boom chết bởi EffectExplosion! Kill count tăng!");
                    }
                    projectile.explode();
                    break;
                }
            }
        }
    }
    
    public void render(android.graphics.Canvas canvas, float cameraX, float cameraY) {
        for (ExplosionProjectile projectile : projectiles) {
            if (projectile.isActive()) {
                projectile.render(canvas, cameraX, cameraY);
            }
        }
    }
    
    public boolean isActive() {
        return active;
    }
    
    public static EffectExplosionSprites getSprites() {
        return sprites;
    }
    
    // Inner class cho ExplosionProjectile
    public static class ExplosionProjectile {
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
        
        public ExplosionProjectile(PointF startPos, PointF target, int damage, float speed) {
            this.position = new PointF(startPos.x, startPos.y);
            this.target = target;
            this.damage = damage;
            this.speed = speed;
            this.hitbox = new RectF(position.x - 8, position.y - 8, position.x + 8, position.y + 8);
            
            // Tính velocity
            float dx = target.x - startPos.x;
            float dy = target.y - startPos.y;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            
            if (distance > 0) {
                this.velocity = new PointF((dx / distance) * speed, (dy / distance) * speed);
            } else {
                this.velocity = new PointF(0, 0);
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
        
        public void render(android.graphics.Canvas canvas, float cameraX, float cameraY) {
            if (!active) return;
            
            if (exploding) {
                // Vẽ hiệu ứng nổ
                android.graphics.Bitmap explosionSprite = sprites.getExplosionSprite(currentExplosionFrame);
                canvas.drawBitmap(explosionSprite, 
                    position.x - cameraX - 48, 
                    position.y - cameraY - 48, 
                    null);
            } else {
                // Vẽ projectile (có thể dùng spark_preview hoặc tạo sprite riêng)
                // Tạm thời vẽ một hình tròn nhỏ
                android.graphics.Paint paint = new android.graphics.Paint();
                paint.setColor(android.graphics.Color.YELLOW);
                canvas.drawCircle(position.x - cameraX, position.y - cameraY, 4, paint);
            }
        }
        
        public RectF getHitbox() {
            return hitbox;
        }
        
        public boolean isActive() {
            return active;
        }
    }
}
