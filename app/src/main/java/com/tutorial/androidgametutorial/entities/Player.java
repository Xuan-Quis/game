package com.tutorial.androidgametutorial.entities;

import static com.tutorial.androidgametutorial.main.MainActivity.GAME_HEIGHT;
import static com.tutorial.androidgametutorial.main.MainActivity.GAME_WIDTH;

import android.graphics.PointF;

public class Player extends Character {

    private long lastAttackTime = 0;
    private long attackCooldown = 500; // milliseconds

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


}