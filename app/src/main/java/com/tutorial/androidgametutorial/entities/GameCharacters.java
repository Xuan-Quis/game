package com.tutorial.androidgametutorial.entities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


import com.tutorial.androidgametutorial.main.MainActivity;
import com.tutorial.androidgametutorial.R;
import com.tutorial.androidgametutorial.helpers.GameConstants;
import com.tutorial.androidgametutorial.helpers.interfaces.BitmapMethods;

public enum GameCharacters implements BitmapMethods {

    PLAYER(R.drawable.player_spritesheet, 7, 4),
    SKELETON(R.drawable.skeleton_spritesheet, 7, 4),
    BOOM(R.drawable.boom_smile, 1, 1);
    private Bitmap spriteSheet;
    private Bitmap[][] sprites;
    private BitmapFactory.Options options = new BitmapFactory.Options();
    private final int rows;
    private final int cols;

    GameCharacters(int resID, int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        options.inScaled = false;
        spriteSheet = BitmapFactory.decodeResource(MainActivity.getGameContext().getResources(), resID, options);
        sprites = new Bitmap[rows][cols];
        for (int j = 0; j < rows; j++)
            for (int i = 0; i < cols; i++)
                sprites[j][i] = getScaledBitmap(Bitmap.createBitmap(spriteSheet, GameConstants.Sprite.DEFAULT_SIZE * i, GameConstants.Sprite.DEFAULT_SIZE * j, GameConstants.Sprite.DEFAULT_SIZE, GameConstants.Sprite.DEFAULT_SIZE));
    }

    public int getRows() { return rows; }
    public int getCols() { return cols; }

    public Bitmap getSpriteSheet() {
        return spriteSheet;
    }

    public Bitmap getSprite(int yPos, int xPos) {
        return sprites[yPos][xPos];
    }


}