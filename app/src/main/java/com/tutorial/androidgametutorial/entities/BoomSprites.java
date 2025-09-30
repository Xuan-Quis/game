package com.tutorial.androidgametutorial.entities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.tutorial.androidgametutorial.main.MainActivity;
import com.tutorial.androidgametutorial.R;
import com.tutorial.androidgametutorial.helpers.GameConstants;
import com.tutorial.androidgametutorial.helpers.interfaces.BitmapMethods;

public class BoomSprites implements BitmapMethods {
    
    private Bitmap[] moveSprites = new Bitmap[4]; // boom_front, boom_left, boom_right, boom_behind
    private Bitmap[] explosionSprites = new Bitmap[7]; // boom_smile, boom_bum, boom_bum_2, ..., boom_bum_6
    
    public BoomSprites() {
        loadMoveSprites();
        loadExplosionSprites();
    }
    
    private void loadMoveSprites() {
        int[] moveResIds = {
            R.drawable.boom_front,
            R.drawable.boom_left, 
            R.drawable.boom_right,
            R.drawable.boom_behind
        };
        
        for (int i = 0; i < moveSprites.length; i++) {
            options.inScaled = false;
            Bitmap original = BitmapFactory.decodeResource(
                MainActivity.getGameContext().getResources(), 
                moveResIds[i], 
                options
            );
            // Scale trực tiếp từ kích thước gốc lên kích thước game (96x96)
            moveSprites[i] = Bitmap.createScaledBitmap(original, 
                GameConstants.Sprite.SIZE, 
                GameConstants.Sprite.SIZE, 
                false);
        }
    }
    
    private void loadExplosionSprites() {
        int[] explosionResIds = {
            R.drawable.boom_smile,
            R.drawable.boom_bum,
            R.drawable.boom_bum_2,
            R.drawable.boom_bum_3,
            R.drawable.boom_bum_4,
            R.drawable.boom_bum_5,
            R.drawable.boom_bum_6
        };
        
        for (int i = 0; i < explosionSprites.length; i++) {
            options.inScaled = false;
            Bitmap original = BitmapFactory.decodeResource(
                MainActivity.getGameContext().getResources(), 
                explosionResIds[i], 
                options
            );
            // Scale trực tiếp từ kích thước gốc lên kích thước game (96x96)
            explosionSprites[i] = Bitmap.createScaledBitmap(original, 
                GameConstants.Sprite.SIZE, 
                GameConstants.Sprite.SIZE, 
                false);
        }
    }
    
    public Bitmap getMoveSprite(int direction) {
        if (direction < 0 || direction >= moveSprites.length) {
            return moveSprites[0]; // Default to front
        }
        return moveSprites[direction];
    }
    
    public Bitmap getExplosionSprite(int frame) {
        if (frame < 0 || frame >= explosionSprites.length) {
            return explosionSprites[0]; // Default to smile
        }
        return explosionSprites[frame];
    }
    
    public int getExplosionFrameCount() {
        return explosionSprites.length;
    }
}
