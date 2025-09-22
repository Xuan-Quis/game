package com.tutorial.androidgametutorial.entities;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;

import com.tutorial.androidgametutorial.helpers.GameConstants;

public class Projectile {
    private PointF pos;
    private float vx, vy;
    private int damage;
    private boolean active = true;
    private float speed;
    private float radius = 15; // bán kính va chạm

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
        c.drawCircle(pos.x + cameraX, pos.y + cameraY, radius, paint);
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