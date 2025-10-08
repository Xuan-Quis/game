package com.tutorial.androidgametutorial.entities.enemies;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.tutorial.androidgametutorial.R;
import com.tutorial.androidgametutorial.helpers.interfaces.BitmapMethods;
import com.tutorial.androidgametutorial.main.MainActivity;

public enum BossAnimation implements BitmapMethods {

    // SỬA ĐỔI: Bỏ đi các tham số kích thước, code sẽ tự tính toán
    BOSS_IDLE(R.drawable.boss_dungyen, 1, 6),
    BOSS_WALK(R.drawable.bosswalk, 1, 6),
    BOSS_PREPARE_ATTACK_LEFT(R.drawable.bosspreviewattackleft, 1, 3),
    BOSS_PREPARE_ATTACK_RIGHT(R.drawable.bosspreviewattackright, 1, 3),
    BOSS_ATTACK_LEFT(R.drawable.bossattackleft, 1, 4),
    BOSS_ATTACK_RIGHT(R.drawable.bossattackright, 1, 4);

    private final Bitmap[][] sprites;

    // SỬA ĐỔI: Hàm khởi tạo không cần chiều rộng và cao của sprite nữa
    BossAnimation(int resID, int rows, int cols) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;

        Bitmap spriteSheet = BitmapFactory.decodeResource(
                MainActivity.getGameContext().getResources(),
                resID,
                options
        );

        sprites = new Bitmap[rows][cols];
        if (spriteSheet == null) {
            Log.e("BossAnimation", "LỖI: Không thể tải ảnh từ resID: " + resID);
            return;
        }

        // --- LOGIC TỰ ĐỘNG TÍNH TOÁN KÍCH THƯỚC ---
        final int spriteWidth = spriteSheet.getWidth() / cols;
        final int spriteHeight = spriteSheet.getHeight() / rows;
        // -----------------------------------------

        Log.d("BossAnimation", "Tải ảnh resID " + resID + " - Kích thước frame tính toán: " + spriteWidth + "x" + spriteHeight);

        for (int j = 0; j < rows; j++) {
            for (int i = 0; i < cols; i++) {
                try {
                    Bitmap originalSprite = Bitmap.createBitmap(
                            spriteSheet,
                            spriteWidth * i,      // Vị trí x
                            spriteHeight * j,     // Vị trí y
                            spriteWidth,          // Chiều rộng để cắt
                            spriteHeight          // Chiều cao để cắt
                    );
                    sprites[j][i] = getScaledBitmap(originalSprite);
                } catch (IllegalArgumentException e) {
                    Log.e("BossAnimation", "CRASH khi cắt ảnh resID: " + resID + ". " + e.getMessage());
                    // Nếu có lỗi, game vẫn sẽ chạy tiếp thay vì crash
                }
            }
        }
    }

    public Bitmap[][] getSprites() {
        return sprites;
    }

    public Bitmap getSprite(int row, int col) {
        if (sprites == null || row >= sprites.length || col >= sprites[row].length) return null;
        return sprites[row][col];
    }
}