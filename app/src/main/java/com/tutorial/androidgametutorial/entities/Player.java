package com.tutorial.androidgametutorial.entities;

import static com.tutorial.androidgametutorial.main.MainActivity.GAME_HEIGHT;
import static com.tutorial.androidgametutorial.main.MainActivity.GAME_WIDTH;

import android.graphics.PointF;

import com.tutorial.androidgametutorial.entities.enemies.Skeleton;
import com.tutorial.androidgametutorial.gamestates.Playing;

public class Player extends Character {

    private long lastAttackTime = 0;
    private long attackCooldown = 500; // milliseconds

    private long lastSkillTime = 0;
    private long skillCooldown = 3000; // 3 giây hồi chiêu
    private int skillRange = 500;
    private int skillDamage = 100;

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
                200f // tốc độ bay (px / s nếu delta tính theo giây)
        );

        playing.addProjectile(sword);

    }
}