package com.tutorial.androidgametutorial.entities.items;

import android.graphics.Canvas;
import android.graphics.PointF;

import com.tutorial.androidgametutorial.entities.Entity;
import com.tutorial.androidgametutorial.helpers.GameConstants;

public class Item extends Entity {

    private final Items itemType;
    private long spawnTime;
    private long duration = 30000; // 30 giây tồn tại

    public Item(Items itemType, PointF pos) {
        super(pos, itemType.getWidth(), itemType.getHeight());
        this.itemType = itemType;
        this.spawnTime = System.currentTimeMillis();
    }

    public Items getItemType() {
        return itemType;
    }
    
    public void update(double delta) {
        if (!isActive()) return;
        
        // Kiểm tra thời gian tồn tại
        if (System.currentTimeMillis() - spawnTime >= duration) {
            setActive(false);
        }
    }
    
    public void render(Canvas canvas, float cameraX, float cameraY) {
        if (!isActive()) return;
        
        canvas.drawBitmap(itemType.getImage(),
            getHitbox().left + cameraX - GameConstants.Sprite.X_DRAW_OFFSET,
            getHitbox().top + cameraY - GameConstants.Sprite.Y_DRAW_OFFSET,
            null);
    }
    
    public void deactivate() {
        setActive(false);
    }
}