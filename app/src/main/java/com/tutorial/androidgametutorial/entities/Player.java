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

    private static SoundPool soundPool;
    private static int skillSoundId;

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
}