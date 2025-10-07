package com.tutorial.androidgametutorial.entities.enemies;

import android.graphics.PointF;
import com.tutorial.androidgametutorial.entities.GameCharacters;

public class Boss extends Skeleton {
    public Boss(PointF position) {
        super(position, GameCharacters.BOSS);
    }
    // Có thể mở rộng thêm logic riêng cho Boss ở đây
}

