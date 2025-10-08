package com.tutorial.androidgametutorial.entities.enemies;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.tutorial.androidgametutorial.R;
import com.tutorial.androidgametutorial.helpers.GameConstants;
import com.tutorial.androidgametutorial.helpers.interfaces.BitmapMethods;
import com.tutorial.androidgametutorial.main.MainActivity;

public enum BossAnimation implements BitmapMethods {

    BOSS_IDLE(R.drawable.boss_dungyen, 1, 6),
    BOSS_WALK(R.drawable.bosswalk, 1, 6),
    BOSS_PREPARE_ATTACK_LEFT(R.drawable.bosspreviewattackleft, 1, 3),
    BOSS_PREPARE_ATTACK_RIGHT(R.drawable.bosspreviewattackright, 1, 3),
    BOSS_ATTACK_LEFT(R.drawable.bossattackleft, 1, 4),
    BOSS_ATTACK_RIGHT(R.drawable.bossattackright, 1, 4);

    private final Bitmap[][] sprites;
    private final int rows;
    private final int cols;
    private final Bitmap spriteSheet;

    BossAnimation(int resID, int rows, int cols) {
        this.rows = rows;
        this.cols = cols;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;

        spriteSheet = BitmapFactory.decodeResource(
                MainActivity.getGameContext().getResources(),
                resID,
                options
        );

        sprites = new Bitmap[rows][cols];
        for (int j = 0; j < rows; j++) {
            for (int i = 0; i < cols; i++) {
                // getScaledBitmap(...) giả sử là method từ BitmapMethods (do project đã dùng trước đó)
                sprites[j][i] = getScaledBitmap(Bitmap.createBitmap(
                        spriteSheet,
                        GameConstants.Sprite.DEFAULT_SIZE * i,
                        GameConstants.Sprite.DEFAULT_SIZE * j,
                        GameConstants.Sprite.DEFAULT_SIZE,
                        GameConstants.Sprite.DEFAULT_SIZE
                ));
            }
        }
    }

    /**
     * Trả về toàn bộ mảng sprites (rows x cols).
     */
    public Bitmap[][] getSprites() {
        return sprites;
    }

    /**
     * Trả về 1 frame cụ thể (hàm tên giống GameCharacters để đồng nhất).
     */
    public Bitmap getSprite(int row, int col) {
        return sprites[row][col];
    }

    public Bitmap getSprites(int y, int x) {
        return getSprite(y, x);
    }

    public int getCols() {
        return cols;
    }

    public int getRows() {
        return rows;
    }
}
