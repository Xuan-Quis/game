package com.tutorial.androidgametutorial.entities;

import android.graphics.Canvas;
import android.graphics.PointF;

import com.tutorial.androidgametutorial.entities.enemies.Boom;
import com.tutorial.androidgametutorial.entities.enemies.Monster;
import com.tutorial.androidgametutorial.entities.enemies.Skeleton;
import com.tutorial.androidgametutorial.gamestates.Playing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class SparkSkill {
    
    private PointF startPos;
    private List<SparkProjectile> projectiles;
    private long startTime;
    private long duration = 3000; // 3 giây tồn tại
    private int damage = 30;
    private float range = 400f; // Tầm bắn
    private int projectileCount = 15; // Số lượng projectile mỗi đợt
    private int totalWaves = 6; // Tổng số đợt bắn
    private long waveInterval = 1000; // Khoảng cách giữa các đợt (1 giây)
    private int currentWave = 0;
    private long lastWaveTime = 0;
    private boolean active = true;
    private Random random = new Random();
    private Playing playing;
    
    public SparkSkill(PointF startPos, Playing playing) {
        this.startPos = startPos;
        this.playing = playing;
        this.startTime = System.currentTimeMillis();
        this.lastWaveTime = startTime;
        this.projectiles = new ArrayList<>();
        
        // Bắt đầu đợt đầu tiên ngay lập tức
        createWaveProjectiles();
    }
    
    private void createWaveProjectiles() {
        // Tìm quái vật gần nhất để làm hướng chính
        Skeleton nearestSkeleton = findNearestSkeleton();
        PointF mainTarget = null;
        
        if (nearestSkeleton != null) {
            mainTarget = new PointF(nearestSkeleton.getHitbox().centerX(), nearestSkeleton.getHitbox().centerY());
        }
        
        for (int i = 0; i < projectileCount; i++) {
            PointF target;
            
            if (mainTarget != null && i < 6) {
                // 6 tia đầu bắn về phía quái vật gần nhất với độ lệch nhỏ
                float angleOffset = (random.nextFloat() - 0.5f) * 60f; // ±30 độ
                float distance = range * (0.8f + random.nextFloat() * 0.2f);
                
                float dx = mainTarget.x - startPos.x;
                float dy = mainTarget.y - startPos.y;
                float baseAngle = (float) Math.toDegrees(Math.atan2(dy, dx));
                float finalAngle = baseAngle + angleOffset;
                
                target = new PointF(
                    startPos.x + (float) (Math.cos(Math.toRadians(finalAngle)) * distance),
                    startPos.y + (float) (Math.sin(Math.toRadians(finalAngle)) * distance)
                );
            } else {
                // Các tia còn lại bắn random hướng
                float angle = random.nextFloat() * 360f;
                float distance = range * (0.6f + random.nextFloat() * 0.4f);
                
                target = new PointF(
                    startPos.x + (float) (Math.cos(Math.toRadians(angle)) * distance),
                    startPos.y + (float) (Math.sin(Math.toRadians(angle)) * distance)
                );
            }
            
            SparkProjectile projectile = new SparkProjectile(
                new PointF(startPos.x, startPos.y),
                target,
                damage,
                500f // Tăng tốc độ bay lên 500f
            );
            
            projectiles.add(projectile);
        }
        
        currentWave++;
        lastWaveTime = System.currentTimeMillis();
    }
    
    private void createRandomProjectiles() {
        // Tìm quái vật gần nhất để làm hướng chính
        Skeleton nearestSkeleton = findNearestSkeleton();
        PointF mainTarget = null;
        
        if (nearestSkeleton != null) {
            mainTarget = new PointF(nearestSkeleton.getHitbox().centerX(), nearestSkeleton.getHitbox().centerY());
        }
        
        for (int i = 0; i < projectileCount; i++) {
            PointF target;
            
            if (mainTarget != null && i < 8) {
                // 8 tia đầu bắn về phía quái vật gần nhất với độ lệch nhỏ
                float angleOffset = (random.nextFloat() - 0.5f) * 60f; // ±30 độ
                float distance = range * (0.8f + random.nextFloat() * 0.2f);
                
                float dx = mainTarget.x - startPos.x;
                float dy = mainTarget.y - startPos.y;
                float baseAngle = (float) Math.toDegrees(Math.atan2(dy, dx));
                float finalAngle = baseAngle + angleOffset;
                
                target = new PointF(
                    startPos.x + (float) (Math.cos(Math.toRadians(finalAngle)) * distance),
                    startPos.y + (float) (Math.sin(Math.toRadians(finalAngle)) * distance)
                );
            } else {
                // Các tia còn lại bắn random hướng
                float angle = random.nextFloat() * 360f;
                float distance = range * (0.6f + random.nextFloat() * 0.4f);
                
                target = new PointF(
                    startPos.x + (float) (Math.cos(Math.toRadians(angle)) * distance),
                    startPos.y + (float) (Math.sin(Math.toRadians(angle)) * distance)
                );
            }
            
            SparkProjectile projectile = new SparkProjectile(
                new PointF(startPos.x, startPos.y),
                target,
                damage,
                300f // tốc độ bay
            );
            
            projectiles.add(projectile);
        }
    }
    
    private Skeleton findNearestSkeleton() {
        if (playing != null) {
            return playing.findNearestSkeleton(startPos.x, startPos.y, range);
        }
        return null;
    }
    
    public void update(double delta, Playing playing) {
        if (!active) return;
        
        // Kiểm tra xem có cần tạo đợt mới không
        long currentTime = System.currentTimeMillis();
        if (currentWave < totalWaves && 
            currentTime - lastWaveTime >= waveInterval) {
            createWaveProjectiles();
        }
        
        // Cập nhật tất cả projectiles
        Iterator<SparkProjectile> it = projectiles.iterator();
        while (it.hasNext()) {
            SparkProjectile projectile = it.next();
            if (projectile.isActive()) {
                projectile.update(delta);
                checkProjectileCollision(projectile, playing);
            } else {
                it.remove(); // Xóa projectile đã kết thúc
            }
        }
        
        // Kiểm tra thời gian tồn tại
        if (currentTime - startTime >= duration) {
            // Kích hoạt tất cả projectiles còn lại
            for (SparkProjectile projectile : projectiles) {
                if (projectile.isActive()) {
                    projectile.explode();
                }
            }
            active = false;
        }
    }
    
    private void checkProjectileCollision(SparkProjectile projectile, Playing playing) {
        // Kiểm tra va chạm với Skeleton
        if (playing.getMapManager().getCurrentMap().getSkeletonArrayList() != null) {
            for (Skeleton skeleton : playing.getMapManager().getCurrentMap().getSkeletonArrayList()) {
                if (!skeleton.isActive()) continue;
                if (android.graphics.RectF.intersects(projectile.getHitbox(), skeleton.getHitbox())) {
                    skeleton.damageCharacter(projectile.getDamage());
                    if (skeleton.getCurrentHealth() <= 0) {
                        skeleton.setSkeletonInactive();
                        playing.enemyKilled(); // THÊM DÒNG NÀY để tăng killCount
                        System.out.println("💀 Skeleton chết bởi SparkSkill! Kill count tăng!");
                        // Thử drop item khi skeleton chết bởi SparkSkill
                        com.tutorial.androidgametutorial.entities.items.Item droppedItem = com.tutorial.androidgametutorial.helpers.HelpMethods.tryDropItem(new android.graphics.PointF(skeleton.getHitbox().centerX(), skeleton.getHitbox().centerY()));
                        if (droppedItem != null && playing.getMapManager().getCurrentMap().getItemArrayList() != null) {
                            playing.getMapManager().getCurrentMap().getItemArrayList().add(droppedItem);
                            System.out.println("🎁 Skeleton chết bởi SparkSkill! Drop item: " + droppedItem.getItemType());
                        } else if (droppedItem != null && playing.getMapManager().getCurrentMap().getItemArrayList() == null) {
                            System.out.println("❌ ItemArrayList is null - không thể drop item từ SparkSkill");
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
                if (android.graphics.RectF.intersects(projectile.getHitbox(), monster.getHitbox())) {
                    monster.damageCharacter(projectile.getDamage());
                    if (monster.getCurrentHealth() <= 0) {
                        monster.setMonsterInactive();
                        playing.enemyKilled(); // THÊM DÒNG NÀY để tăng killCount
                        System.out.println("💀 Monster chết bởi SparkSkill! Kill count tăng!");
                        // Thử drop item khi monster chết bởi SparkSkill
                        com.tutorial.androidgametutorial.entities.items.Item droppedItem = com.tutorial.androidgametutorial.helpers.HelpMethods.tryDropItem(new android.graphics.PointF(monster.getHitbox().centerX(), monster.getHitbox().centerY()));
                        if (droppedItem != null) {
                            playing.getMapManager().getCurrentMap().getItemArrayList().add(droppedItem);
                            System.out.println("🎁 Monster chết bởi SparkSkill! Drop item: " + droppedItem.getItemType());
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
                if (android.graphics.RectF.intersects(projectile.getHitbox(), boom.getHitbox())) {
                    boom.damageCharacter(projectile.getDamage());
                    if (boom.getCurrentHealth() <= 0) {
                        boom.setBoomInactive();
                        playing.enemyKilled(); // THÊM DÒNG NÀY để tăng killCount
                        System.out.println("💀 Boom chết bởi SparkSkill! Kill count tăng!");
                    }
                    projectile.explode();
                    break;
                }
            }
        }
    }
    
    public void render(Canvas canvas, float cameraX, float cameraY) {
        for (SparkProjectile projectile : projectiles) {
            if (projectile.isActive()) {
                projectile.render(canvas, cameraX, cameraY);
            }
        }
    }
    
    public boolean isActive() {
        return active;
    }
}
