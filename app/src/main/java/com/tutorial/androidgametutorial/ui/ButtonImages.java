package com.tutorial.androidgametutorial.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.tutorial.androidgametutorial.R;
import com.tutorial.androidgametutorial.helpers.interfaces.BitmapMethods;
import com.tutorial.androidgametutorial.main.MainActivity;

public enum ButtonImages implements BitmapMethods {

    MENU_START(R.drawable.mainmenu_button_start, 300, 140, true),
    PLAYING_MENU(R.drawable.playing_button_menu, 140, 140, true),
    MENU_MENU(R.drawable.mainmenu_button_menu, 300, 140, true),
    MENU_REPLAY(R.drawable.mainmenu_button_replay, 300, 140, true),
    MENU_ESCAPE(R.drawable.exit_button, 300, 140, true), // Tạm thời dùng menu button, sẽ thay sau
    DIFFICULTY_EASY(R.drawable.easy_button, 300, 140, true), // Icon nhân vật cho độ khó dễ
    DIFFICULTY_HARD(R.drawable.hard_button, 300, 140, true), // Icon đầu lâu cho độ khó khó
    BACK_BUTTON(R.drawable.back_button, 300, 140, true),
    SCORE_BUTTON(R.drawable.score_button, 300, 140, true); // Icon đầu lâu cho độ khó khó

    private int width, height;
    private Bitmap normal, pushed;

    ButtonImages(int resID, int width, int height, boolean isAtlas) {
        options.inScaled = false;
        this.width = width;
        this.height = height;
        Bitmap buttonAtlas = BitmapFactory.decodeResource(MainActivity.getGameContext().getResources(), resID, options);

        // Add bounds checking to prevent IllegalArgumentException
        if (isAtlas && buttonAtlas != null && buttonAtlas.getWidth() >= width * 2 && buttonAtlas.getHeight() >= height) {
            try {
                normal = Bitmap.createBitmap(buttonAtlas, 0, 0, width, height);
                pushed = Bitmap.createBitmap(buttonAtlas, width, 0, width, height);
            } catch (IllegalArgumentException e) {
                // Fallback: use the whole atlas as normal state
                normal = Bitmap.createScaledBitmap(buttonAtlas, width, height, true);
                pushed = normal;
            }
        } else {
            // Scale the entire bitmap to fit the required dimensions
            if (buttonAtlas != null) {
                normal = Bitmap.createScaledBitmap(buttonAtlas, width, height, true);
            } else {
                // Create a placeholder bitmap if resource loading fails
                normal = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            }
            pushed = normal;
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Bitmap getBtnImg(boolean isBtnPushed) {
        return isBtnPushed ? pushed : normal;
    }
}
