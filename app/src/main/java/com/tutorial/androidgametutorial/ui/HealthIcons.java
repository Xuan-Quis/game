package com.tutorial.androidgametutorial.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.tutorial.androidgametutorial.R;
import com.tutorial.androidgametutorial.helpers.interfaces.BitmapMethods;
import com.tutorial.androidgametutorial.main.MainActivity;

public enum HealthIcons implements BitmapMethods {

    HEART_FULL(0),
    HEART_3Q(1),
    HEART_HALF(2),
    HEART_1Q(3),
    HEART_EMPTY(4);

    private Bitmap icon;
    private int widthHeight = 16;

    HealthIcons(int xPos) {
        options.inScaled = false;
        Bitmap atlas = BitmapFactory.decodeResource(MainActivity.getGameContext().getResources(), R.drawable.health_icons, options);
        icon = getScaledBitmap(Bitmap.createBitmap(atlas, xPos * widthHeight, 0, widthHeight, widthHeight));
    }
    public Bitmap getIcon() {
        return icon;
    }
}
