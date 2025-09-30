package com.tutorial.androidgametutorial.entities;

import static com.tutorial.androidgametutorial.main.MainActivity.GAME_HEIGHT;
import static com.tutorial.androidgametutorial.main.MainActivity.GAME_WIDTH;

import android.graphics.PointF;
import android.media.AudioAttributes;
import android.media.SoundPool;

import com.tutorial.androidgametutorial.R;
import com.tutorial.androidgametutorial.entities.enemies.Skeleton;
import com.tutorial.androidgametutorial.gamestates.Playing;
import com.tutorial.androidgametutorial.main.MainActivity;

public class Player extends Character {

    private long lastAttackTime = 0;
    private long attackCooldown = 500; // milliseconds

    private long lastSkillTime = 0;
    private final long skillCooldown = 1000; // 1 giây hồi chiêu
    private int skillRange = 500;
    private int skillDamage = 100;

    // EffectExplosion skill
    private long lastExplosionSkillTime = 0;
    private final long explosionSkillCooldown = 10000; // 10 giây hồi chiêu

    // Spark skill
    private long lastSparkSkillTime = 0;
    private final long sparkSkillCooldown = 1000; // 1 giây hồi chiêu

    // Shield system (từ MEDIPACK)
    private int shieldHits = 0; // Số đòn còn lại có thể đỡ
    private long shieldStartTime = 0;
    private final long shieldDuration = 30000; // 30 giây tồn tại
    
    // Speed boost system (từ FISH)
    private float speedMultiplier = 1.0f; // Hệ số tốc độ
    private long speedBoostStartTime = 0;
    private final long speedBoostDuration = 5000; // 5 giây tăng tốc

    private static SoundPool soundPool;
    private static int skillSoundId;
    private static int sparkSkillSoundId;

    static {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(audioAttributes)
                .build();

        skillSoundId = soundPool.load(MainActivity.getGameContext(), R.raw.fast_whoosh, 1);
        sparkSkillSoundId = soundPool.load(MainActivity.getGameContext(), R.raw.spark_voice, 1);
    }

    public Player() {
        super(new PointF(GAME_WIDTH / 2, GAME_HEIGHT / 2), GameCharacters.PLAYER);
        setStartHealth(600);
    }

    public void update(double delta, boolean movePlayer) {
        if (movePlayer)
            updateAnimation();
        updateWepHitbox();
    }

    public boolean canAttack() {
        return System.currentTimeMillis() - lastAttackTime >= attackCooldown;
    }

    public void setLastAttackTime() {
        lastAttackTime = System.currentTimeMillis();
    }

    public void setAttackCooldown(long cooldown) {
        attackCooldown = cooldown;
    }


    public boolean canCastThrow() {
        return System.currentTimeMillis() - lastSkillTime >= skillCooldown;
    }

    public void setLastSkillTime() {
        lastSkillTime = System.currentTimeMillis();
    }

    public void castThrowSword(Playing playing) {
        if (!canCastThrow()) return;
        setLastSkillTime();

        soundPool.play(skillSoundId, 1, 1, 1, 0, 1f);

        // chuyển từ screen coords -> world coords (player hitbox hiện là screen coords)
        float worldPx = getHitbox().centerX() - playing.getCameraX();
        float worldPy = getHitbox().centerY() - playing.getCameraY();

        Skeleton nearest = playing.findNearestSkeleton(
                worldPx,
                worldPy,
                skillRange
        );

        if (nearest == null) return;

        // target của skeleton đã ở world coords
        Projectile sword = new Projectile(
                new PointF(worldPx, worldPy),
                new PointF(nearest.getHitbox().centerX(), nearest.getHitbox().centerY()),
                skillDamage,
                300f // tốc độ bay (px / s nếu delta tính theo giây)
        );

        playing.addProjectile(sword);

    }

    // EffectExplosion skill methods
    public boolean canCastExplosion() {
        return System.currentTimeMillis() - lastExplosionSkillTime >= explosionSkillCooldown;
    }

    public void setLastExplosionSkillTime() {
        lastExplosionSkillTime = System.currentTimeMillis();
    }

    public void castEffectExplosion(Playing playing) {
        if (!canCastExplosion()) return;
        setLastExplosionSkillTime();

        soundPool.play(skillSoundId, 1, 1, 1, 0, 1f);

        // chuyển từ screen coords -> world coords
        float worldPx = getHitbox().centerX() - playing.getCameraX();
        float worldPy = getHitbox().centerY() - playing.getCameraY();

        EffectExplosion explosion = new EffectExplosion(new PointF(worldPx, worldPy));
        playing.addEffectExplosion(explosion);
    }

    // Spark skill methods
    public boolean canCastSpark() {
        return System.currentTimeMillis() - lastSparkSkillTime >= sparkSkillCooldown;
    }

    public void setLastSparkSkillTime() {
        lastSparkSkillTime = System.currentTimeMillis();
    }

    public void castSparkSkill(Playing playing) {
        if (!canCastSpark()) return;
        setLastSparkSkillTime();

        soundPool.play(sparkSkillSoundId, 1, 1, 1, 0, 1f);

        // Player hitbox đang ở screen coordinates, cần chuyển sang world coordinates
        float worldPx = getHitbox().centerX() - playing.getCameraX();
        float worldPy = getHitbox().centerY() - playing.getCameraY();

        SparkSkill sparkSkill = new SparkSkill(new PointF(worldPx, worldPy), playing);
        playing.addSparkSkill(sparkSkill);
    }
    
    // Item effects
    public void useMedipack() {
        // Hồi 3/10 máu (30% máu)
        int healAmount = (int) (getMaxHealth() * 0.3f);
        healCharacter(healAmount);
        System.out.println("❤️ MEDIPACK! Hồi máu +" + healAmount + " HP. Hiện tại: " + getCurrentHealth() + "/" + getMaxHealth());
    }
    
    public void useFish() {
        // Tăng tốc độ di chuyển
        speedMultiplier = 2.0f; // Tăng 100% tốc độ (gấp đôi)
        speedBoostStartTime = System.currentTimeMillis();
        System.out.println("🐟 FISH! Tăng tốc được kích hoạt! Tốc độ tăng 100% trong 5 giây.");
    }
    
    public void useEmptyPot() {
        // Tạo khiên bảo vệ 3 đòn
        shieldHits = 3;
        shieldStartTime = System.currentTimeMillis();
        System.out.println("🛡️ EMPTY_POT! Khiên bảo vệ được kích hoạt! Có thể đỡ 3 đòn.");
    }
    
    // Kiểm tra và cập nhật hiệu ứng
    public void updateEffects() {
        long currentTime = System.currentTimeMillis();
        
        // Kiểm tra shield hết hạn
        if (shieldHits > 0 && currentTime - shieldStartTime >= shieldDuration) {
            shieldHits = 0;
            System.out.println("Khiên bảo vệ đã hết hạn!");
        }
        
        // Kiểm tra speed boost hết hạn
        if (speedMultiplier > 1.0f && currentTime - speedBoostStartTime >= speedBoostDuration) {
            speedMultiplier = 1.0f;
            System.out.println("Tăng tốc đã hết hạn!");
        }
    }
    
    // Override damageCharacter để xử lý shield
    @Override
    public void damageCharacter(int damage) {
        if (shieldHits > 0) {
            // Đỡ đòn bằng khiên
            shieldHits--;
            System.out.println("Khiên đã đỡ đòn! Còn lại " + shieldHits + " đòn.");
            return; // Không bị sát thương
        }
        
        // Bị sát thương bình thường
        super.damageCharacter(damage);
    }
    
    // Getter methods
    public boolean hasSpeedBoost() {
        return speedMultiplier > 1.0f;
    }

    public boolean hasShield() {
        return shieldHits > 0;
    }

    public int getShieldHits() {
        return shieldHits;
    }

    public long getSpeedBoostTimeLeft() {
        if (!hasSpeedBoost()) return 0;
        long elapsed = System.currentTimeMillis() - speedBoostStartTime;
        long timeLeft = speedBoostDuration - elapsed;
        return Math.max(0, timeLeft);
    }

    public float getSpeedMultiplier() {
        return speedMultiplier;
    }
}
