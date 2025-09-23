package com.tutorial.androidgametutorial.effects;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.media.AudioAttributes;
import android.media.SoundPool;
import com.tutorial.androidgametutorial.main.MainActivity;
import com.tutorial.androidgametutorial.R;

public class ExplosionEffect {
    private static final int FRAME_COUNT = 5;
    private static final int[] FRAME_RES_IDS = {
        R.drawable.hits_11,
        R.drawable.hits_12,
        R.drawable.hits_13,
        R.drawable.hits_14,
        R.drawable.hits_15
    };
    private static Bitmap[] frames;
    private int currentFrame = 0;
    private long lastFrameTime = 0;
    private static final long FRAME_DURATION = 50; // ms per frame
    private boolean active = true;
    private PointF pos;
    private static SoundPool soundPool;
    private static int explosionSoundId;

    static {
        frames = new Bitmap[FRAME_COUNT];
        for (int i = 0; i < FRAME_COUNT; i++) {
            frames[i] = BitmapFactory.decodeResource(
                MainActivity.getGameContext().getResources(),
                FRAME_RES_IDS[i]
            );
        }

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(audioAttributes)
                .build();

        explosionSoundId = soundPool.load(MainActivity.getGameContext(), R.raw.explosion, 1);
    }

    public ExplosionEffect(PointF pos) {
        this.pos = pos;
        this.lastFrameTime = System.currentTimeMillis();
        soundPool.play(explosionSoundId, 1, 1, 1, 0, 1f);
    }

    public void update() {
        if (!active) return;
        long now = System.currentTimeMillis();
        if (now - lastFrameTime > FRAME_DURATION) {
            currentFrame++;
            lastFrameTime = now;
            if (currentFrame >= FRAME_COUNT) {
                active = false;
            }
        }
    }

    public void render(Canvas c, float cameraX, float cameraY) {
        if (!active) return;
        Bitmap frame = frames[Math.min(currentFrame, FRAME_COUNT - 1)];
        if (frame != null) {
            float drawX = pos.x + cameraX - frame.getWidth() / 2f;
            float drawY = pos.y + cameraY - frame.getHeight() / 2f;
            c.drawBitmap(frame, drawX, drawY, null);
        }
    }

    public boolean isActive() {
        return active;
    }

    public static void dispose() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}
