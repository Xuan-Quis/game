package com.tutorial.androidgametutorial.entities;

import static com.tutorial.androidgametutorial.helpers.GameConstants.Sprite.SCALE_MULTIPLIER;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;

import com.tutorial.androidgametutorial.R;
import com.tutorial.androidgametutorial.helpers.interfaces.BitmapMethods;
import com.tutorial.androidgametutorial.main.MainActivity;

public enum Buildings implements BitmapMethods {


    HOUSE_ONE(0, 0, 64, 48, 23, 42, 12, 36),
    HOUSE_TWO(64, 4, 62, 44, 23, 36, 11, 31),
    HOUSE_SIX(304, 0, 64, 48, 39, 40, 18, 35);


    Bitmap houseImg;
    PointF doorwayPoint;
    int hitboxRoof, hitboxFloor, hitboxHeight, hitboxWidth;


    Buildings(int x, int y, int width, int height, int doorwayX, int doorwayY, int hitboxRoof, int hitboxFloor) {
        options.inScaled = false;

        this.hitboxRoof = hitboxRoof * SCALE_MULTIPLIER;
        this.hitboxFloor = hitboxFloor * SCALE_MULTIPLIER;
        this.hitboxHeight = this.hitboxFloor - this.hitboxRoof;
        this.hitboxWidth = width * SCALE_MULTIPLIER;

        Bitmap atlas = BitmapFactory.decodeResource(MainActivity.getGameContext().getResources(), R.drawable.buildings_atlas, options);
        houseImg = getScaledBitmap(Bitmap.createBitmap(atlas, x, y, width, height));

        doorwayPoint = new PointF(doorwayX * SCALE_MULTIPLIER, doorwayY * SCALE_MULTIPLIER);


    }

    public PointF getDoorwayPoint() {
        return doorwayPoint;
    }

    public int getHitboxRoof() {
        return hitboxRoof;
    }


    public Bitmap getHouseImg() {
        return houseImg;
    }
}
