package com.tutorial.androidgametutorial.gamestates;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;

import com.tutorial.androidgametutorial.helpers.interfaces.GameStateInterface;
import com.tutorial.androidgametutorial.main.Game;
import com.tutorial.androidgametutorial.ui.CustomButton;

import static com.tutorial.androidgametutorial.main.MainActivity.GAME_HEIGHT;
import static com.tutorial.androidgametutorial.main.MainActivity.GAME_WIDTH;

public class WinScreen extends BaseState implements GameStateInterface {

    private CustomButton playAgainButton, menuButton;
    private Paint titlePaint, statsPaint, buttonPaint;
    private int killCount = 0;
    private int bestKillCount = 0;
    private SharedPreferences sharedPrefs;

    public WinScreen(Game game) {
        super(game);
        initButtons();
        initPaints();

        // Khởi tạo SharedPreferences để lưu kỷ lục
        sharedPrefs = game.getContext().getSharedPreferences("GameStats", Context.MODE_PRIVATE);
        bestKillCount = sharedPrefs.getInt("bestKillCount", 0);
    }

    private void initButtons() {
        float buttonWidth = 200;
        float buttonHeight = 80;
        float centerX = GAME_WIDTH / 2f;
        float centerY = GAME_HEIGHT / 2f;

        playAgainButton = new CustomButton(centerX - buttonWidth / 2, centerY + 100, buttonWidth, buttonHeight);
        menuButton = new CustomButton(centerX - buttonWidth / 2, centerY + 200, buttonWidth, buttonHeight);
    }

    private void initPaints() {
        titlePaint = new Paint();
        titlePaint.setColor(Color.YELLOW);
        titlePaint.setTextSize(80);
        titlePaint.setFakeBoldText(true);
        titlePaint.setTextAlign(Paint.Align.CENTER);

        statsPaint = new Paint();
        statsPaint.setColor(Color.WHITE);
        statsPaint.setTextSize(40);
        statsPaint.setFakeBoldText(true);
        statsPaint.setTextAlign(Paint.Align.CENTER);

        buttonPaint = new Paint();
        buttonPaint.setColor(Color.GREEN);
        buttonPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    public void update(double delta) {
        // Victory screen doesn't need update logic
    }

    @Override
    public void render(Canvas c) {
        // Draw background
        c.drawColor(Color.BLACK);

        // Draw victory title
        c.drawText("🏆 CHIẾN THẮNG! 🏆", GAME_WIDTH / 2f, GAME_HEIGHT / 2f - 150, titlePaint);

        // Draw stats
        c.drawText("Bạn đã sống sót 20 giây!", GAME_WIDTH / 2f, GAME_HEIGHT / 2f - 80, statsPaint);
        c.drawText("Lần này: " + killCount + " quái bị tiêu diệt", GAME_WIDTH / 2f, GAME_HEIGHT / 2f - 30, statsPaint);

        // Hiển thị kỷ lục cao nhất
        Paint recordPaint = new Paint();
        recordPaint.setColor(Color.YELLOW);
        recordPaint.setTextSize(35);
        recordPaint.setFakeBoldText(true);
        recordPaint.setTextAlign(Paint.Align.CENTER);
        c.drawText("🥇 KỶ LỤC CÁ NHÂN: " + bestKillCount + " quái", GAME_WIDTH / 2f, GAME_HEIGHT / 2f + 20, recordPaint);

        // Draw buttons
        drawButton(c, playAgainButton, "CHƠI LẠI", buttonPaint);
        drawButton(c, menuButton, "MENU CHÍNH", buttonPaint);
    }

    private void drawButton(Canvas c, CustomButton button, String text, Paint paint) {
        RectF buttonRect = button.getHitbox();

        // Draw button background
        c.drawRect(buttonRect, paint);

        // Draw button border
        Paint borderPaint = new Paint();
        borderPaint.setColor(Color.WHITE);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(3);
        c.drawRect(buttonRect, borderPaint);

        // Draw button text
        Paint textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(30);
        textPaint.setFakeBoldText(true);
        textPaint.setTextAlign(Paint.Align.CENTER);

        float textX = buttonRect.centerX();
        float textY = buttonRect.centerY() + 10; // Offset for better centering
        c.drawText(text, textX, textY, textPaint);
    }

    @Override
    public void touchEvents(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (playAgainButton.getHitbox().contains(event.getX(), event.getY())) {
                // Reset game and start playing again
                game.getPlaying().resetGame();
                game.setCurrentGameState(Game.GameState.PLAYING);
            } else if (menuButton.getHitbox().contains(event.getX(), event.getY())) {
                // Go back to main menu
                game.setCurrentGameState(Game.GameState.MENU);
            }
        }
    }

    public void setKillCount(int killCount) {
        this.killCount = killCount;

        // Cập nhật kỷ lục cao nhất nếu cần
        if (killCount > bestKillCount) {
            bestKillCount = killCount;
            // Lưu kỷ lục mới vào SharedPreferences
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putInt("bestKillCount", bestKillCount);
            editor.apply();
            System.out.println("🥇 KỶ LỤC MỚI! " + bestKillCount + " quái bị tiêu diệt!");
        }
    }

    public int getKillCount() {
        return killCount;
    }

    public int getBestKillCount() {
        return bestKillCount;
    }
}
