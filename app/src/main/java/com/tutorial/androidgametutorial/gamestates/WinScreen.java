package com.tutorial.androidgametutorial.gamestates;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;

import com.tutorial.androidgametutorial.helpers.LeaderboardManager;
import com.tutorial.androidgametutorial.helpers.interfaces.GameStateInterface;
import com.tutorial.androidgametutorial.main.Game;
import com.tutorial.androidgametutorial.ui.CustomButton;

import static com.tutorial.androidgametutorial.main.MainActivity.GAME_HEIGHT;
import static com.tutorial.androidgametutorial.main.MainActivity.GAME_WIDTH;

public class WinScreen extends BaseState implements GameStateInterface {

    private CustomButton playAgainButton, menuButton, leaderboardButton;
    private Paint titlePaint, statsPaint, buttonPaint;
    private int killCount = 0;
    private int bestKillCount = 0;
    private SharedPreferences sharedPrefs;
    private LeaderboardManager leaderboardManager;
    private boolean isNewRecord = false;

    public WinScreen(Game game) {
        super(game);
        initButtons();
        initPaints();

        // Khởi tạo SharedPreferences để lưu kỷ lục (tương thích ngược)
        sharedPrefs = game.getContext().getSharedPreferences("GameStats", Context.MODE_PRIVATE);
        bestKillCount = sharedPrefs.getInt("bestKillCount", 0);

        // Khởi tạo LeaderboardManager
        leaderboardManager = new LeaderboardManager(game.getContext());
    }

    private void initButtons() {
        float buttonWidth = 200;
        float buttonHeight = 80;
        float centerX = GAME_WIDTH / 2f;
        float centerY = GAME_HEIGHT / 2f;

        playAgainButton = new CustomButton(centerX - buttonWidth / 2, centerY + 100, buttonWidth, buttonHeight);
        menuButton = new CustomButton(centerX - buttonWidth / 2, centerY + 200, buttonWidth, buttonHeight);
        leaderboardButton = new CustomButton(centerX - buttonWidth / 2, centerY + 300, buttonWidth, buttonHeight);
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

        // Draw victory title với hiệu ứng kỷ lục mới
        if (isNewRecord) {
            Paint newRecordPaint = new Paint();
            newRecordPaint.setColor(Color.rgb(255, 215, 0)); // Gold color
            newRecordPaint.setTextSize(90);
            newRecordPaint.setFakeBoldText(true);
            newRecordPaint.setTextAlign(Paint.Align.CENTER);
            c.drawText("🎉 KỶ LỤC MỚI! 🎉", GAME_WIDTH / 2f, GAME_HEIGHT / 2f - 200, newRecordPaint);
        }

        c.drawText("🏆 CHIẾN THẮNG! 🏆", GAME_WIDTH / 2f, GAME_HEIGHT / 2f - 150, titlePaint);

        // Draw stats - cập nhật để hiển thị hoàn thành 3 maps
        c.drawText("Bạn đã sống sót qua cả 3 maps!", GAME_WIDTH / 2f, GAME_HEIGHT / 2f - 100, statsPaint);
        c.drawText("🗺️ Map 1 (Outdoor) ✅", GAME_WIDTH / 2f, GAME_HEIGHT / 2f - 70, statsPaint);
        c.drawText("🏔️ Map 2 (Snow) ✅", GAME_WIDTH / 2f, GAME_HEIGHT / 2f - 40, statsPaint);
        c.drawText("🏜️ Map 3 (Desert) ✅", GAME_WIDTH / 2f, GAME_HEIGHT / 2f - 10, statsPaint);
        c.drawText("Tổng quái tiêu diệt: " + killCount + " 👹", GAME_WIDTH / 2f, GAME_HEIGHT / 2f + 20, statsPaint);

        // Hiển thị kỷ lục cao nhất - sử dụng giá trị trực tiếp từ LeaderboardManager
        Paint recordPaint = new Paint();
        recordPaint.setColor(Color.YELLOW);
        recordPaint.setTextSize(35);
        recordPaint.setFakeBoldText(true);
        recordPaint.setTextAlign(Paint.Align.CENTER);

        int bestScore = leaderboardManager.getBestScore();
        c.drawText("🥇 KỶ LỤC CÁ NHÂN: " + bestScore + " quái", GAME_WIDTH / 2f, GAME_HEIGHT / 2f + 50, recordPaint);

        // Debug info (có thể bỏ sau khi test xong)
        Paint debugPaint = new Paint();
        debugPaint.setColor(Color.CYAN);
        debugPaint.setTextSize(20);
        debugPaint.setTextAlign(Paint.Align.LEFT);
        c.drawText("Debug: killCount=" + killCount + ", bestScore=" + bestScore, 50, 50, debugPaint);

        // Draw buttons
        drawButton(c, playAgainButton, "CHƠI LẠI", Color.GREEN);
        drawButton(c, menuButton, "MENU CHÍNH", Color.BLUE);
        drawButton(c, leaderboardButton, "BẢNG XẾP HẠNG", Color.MAGENTA);
    }

    private void drawButton(Canvas c, CustomButton button, String text, int color) {
        RectF buttonRect = button.getHitbox();

        // Draw button background
        Paint bgPaint = new Paint();
        bgPaint.setColor(color);
        bgPaint.setStyle(Paint.Style.FILL);
        c.drawRect(buttonRect, bgPaint);

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
            } else if (leaderboardButton.getHitbox().contains(event.getX(), event.getY())) {
                // Go to leaderboard screen
                game.getLeaderboardScreen().updateLeaderboard();
                game.setCurrentGameState(Game.GameState.LEADERBOARD);
            }
        }
    }

    public void setKillCount(int killCount) {
        this.killCount = killCount;

        // Debug để kiểm tra giá trị
        System.out.println("🏆 WinScreen: Nhận killCount = " + killCount);

        // Kiểm tra xem có phải kỷ lục mới không
        isNewRecord = leaderboardManager.isNewRecord(killCount);

        // Thêm điểm số mới vào leaderboard
        leaderboardManager.addScore(killCount);

        // Cập nhật kỷ lục cao nhất nếu cần (tương thích ngược)
        if (killCount > bestKillCount) {
            bestKillCount = killCount;
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putInt("bestKillCount", bestKillCount);
            editor.apply();
            System.out.println("🥇 KỶ LỤC MỚI! " + bestKillCount + " quái bị tiêu diệt!");
        }

        System.out.println("🏆 WinScreen: Đã lưu killCount = " + this.killCount + ", isNewRecord = " + isNewRecord);
    }

    public int getKillCount() {
        return killCount;
    }

    public int getBestKillCount() {
        return leaderboardManager.getBestScore();
    }
}
