package com.tutorial.androidgametutorial.gamestates;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;

import com.tutorial.androidgametutorial.helpers.LeaderboardManager;
import com.tutorial.androidgametutorial.helpers.interfaces.GameStateInterface;
import com.tutorial.androidgametutorial.main.Game;
import com.tutorial.androidgametutorial.ui.CustomButton;

import java.util.List;

import static com.tutorial.androidgametutorial.main.MainActivity.GAME_HEIGHT;
import static com.tutorial.androidgametutorial.main.MainActivity.GAME_WIDTH;

public class LeaderboardScreen extends BaseState implements GameStateInterface {

    private CustomButton backButton, clearButton;
    private Paint titlePaint, headerPaint, entryPaint, buttonPaint, rankPaint;
    private LeaderboardManager leaderboardManager;
    private List<LeaderboardManager.LeaderboardEntry> topScores;

    public LeaderboardScreen(Game game) {
        super(game);
        initButtons();
        initPaints();

        leaderboardManager = new LeaderboardManager(game.getContext());
        updateLeaderboard();
    }

    private void initButtons() {
        float buttonWidth = 180;
        float buttonHeight = 70;
        float centerX = GAME_WIDTH / 2f;
        float bottomY = GAME_HEIGHT - 100;

        backButton = new CustomButton(centerX - buttonWidth - 20, bottomY, buttonWidth, buttonHeight);
        clearButton = new CustomButton(centerX + 20, bottomY, buttonWidth, buttonHeight);
    }

    private void initPaints() {
        titlePaint = new Paint();
        titlePaint.setColor(Color.YELLOW);
        titlePaint.setTextSize(70);
        titlePaint.setFakeBoldText(true);
        titlePaint.setTextAlign(Paint.Align.CENTER);

        headerPaint = new Paint();
        headerPaint.setColor(Color.CYAN);
        headerPaint.setTextSize(40);
        headerPaint.setFakeBoldText(true);
        headerPaint.setTextAlign(Paint.Align.CENTER);

        entryPaint = new Paint();
        entryPaint.setColor(Color.WHITE);
        entryPaint.setTextSize(35);
        entryPaint.setTextAlign(Paint.Align.LEFT);

        rankPaint = new Paint();
        rankPaint.setColor(Color.YELLOW);
        rankPaint.setTextSize(40);
        rankPaint.setFakeBoldText(true);
        rankPaint.setTextAlign(Paint.Align.CENTER);

        buttonPaint = new Paint();
        buttonPaint.setColor(Color.BLUE);
        buttonPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    public void update(double delta) {
        // Leaderboard screen doesn't need update logic
    }

    @Override
    public void render(Canvas c) {
        // Draw background
        c.drawColor(Color.BLACK);

        // Draw title
        c.drawText("🏆 BẢNG XẾP HẠNG 🏆", GAME_WIDTH / 2f, 120, titlePaint);

        // Draw header
        c.drawText("TOP 6 KỶ LỤC CAO NHẤT", GAME_WIDTH / 2f, 200, headerPaint);

        // Draw leaderboard entries
        float startY = 280;
        float lineHeight = 80;

        if (topScores.isEmpty()) {
            Paint emptyPaint = new Paint();
            emptyPaint.setColor(Color.GRAY);
            emptyPaint.setTextSize(40);
            emptyPaint.setTextAlign(Paint.Align.CENTER);
            c.drawText("Chưa có kỷ lục nào!", GAME_WIDTH / 2f, startY + 100, emptyPaint);
        } else {
            for (int i = 0; i < topScores.size(); i++) {
                LeaderboardManager.LeaderboardEntry entry = topScores.get(i);
                float y = startY + i * lineHeight;

                // Draw rank với màu sắc đặc biệt cho top 3
                Paint currentRankPaint = getRankPaint(entry.rank);
                String rankText = getRankDisplay(entry.rank);
                c.drawText(rankText, 150, y, currentRankPaint);

                // Draw score
                String scoreText = entry.score + " quái";
                c.drawText(scoreText, 300, y, entryPaint);

                // Draw date
                c.drawText(entry.date, GAME_WIDTH - 200, y, entryPaint);

                // Draw separator line
                if (i < topScores.size() - 1) {
                    Paint linePaint = new Paint();
                    linePaint.setColor(Color.GRAY);
                    linePaint.setStrokeWidth(2);
                    c.drawLine(100, y + 25, GAME_WIDTH - 100, y + 25, linePaint);
                }
            }
        }

        // Draw column headers
        Paint columnPaint = new Paint();
        columnPaint.setColor(Color.CYAN);
        columnPaint.setTextSize(30);
        columnPaint.setFakeBoldText(true);
        columnPaint.setTextAlign(Paint.Align.CENTER);

        c.drawText("Hạng", 150, 250, columnPaint);
        columnPaint.setTextAlign(Paint.Align.LEFT);
        c.drawText("Điểm số", 300, 250, columnPaint);
        c.drawText("Ngày", GAME_WIDTH - 200, 250, columnPaint);

        // Draw buttons
        drawButton(c, backButton, "TRỞ VỀ", Color.GREEN);
        drawButton(c, clearButton, "XÓA HẾT", Color.RED);
    }

    private Paint getRankPaint(int rank) {
        Paint paint = new Paint();
        paint.setTextSize(40);
        paint.setFakeBoldText(true);
        paint.setTextAlign(Paint.Align.CENTER);

        switch (rank) {
            case 1:
                paint.setColor(Color.rgb(255, 215, 0)); // Gold
                break;
            case 2:
                paint.setColor(Color.rgb(192, 192, 192)); // Silver
                break;
            case 3:
                paint.setColor(Color.rgb(205, 127, 50)); // Bronze
                break;
            default:
                paint.setColor(Color.WHITE);
                break;
        }
        return paint;
    }

    private String getRankDisplay(int rank) {
        switch (rank) {
            case 1: return "🥇";
            case 2: return "🥈";
            case 3: return "🥉";
            default: return "#" + rank;
        }
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
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(28);
        textPaint.setFakeBoldText(true);
        textPaint.setTextAlign(Paint.Align.CENTER);

        float textX = buttonRect.centerX();
        float textY = buttonRect.centerY() + 10;
        c.drawText(text, textX, textY, textPaint);
    }

    @Override
    public void touchEvents(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (backButton.getHitbox().contains(event.getX(), event.getY())) {
                // Go back to main menu
                game.setCurrentGameState(Game.GameState.MENU);
            } else if (clearButton.getHitbox().contains(event.getX(), event.getY())) {
                // Clear leaderboard
                leaderboardManager.clearLeaderboard();
                updateLeaderboard();
            }
        }
    }

    public void updateLeaderboard() {
        topScores = leaderboardManager.getTop6Scores();
    }
}
