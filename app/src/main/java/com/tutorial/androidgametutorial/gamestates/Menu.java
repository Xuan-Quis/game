package com.tutorial.androidgametutorial.gamestates;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;

import com.tutorial.androidgametutorial.helpers.interfaces.GameStateInterface;
import com.tutorial.androidgametutorial.main.Game;
import com.tutorial.androidgametutorial.main.MainActivity;
import com.tutorial.androidgametutorial.ui.ButtonImages;
import com.tutorial.androidgametutorial.ui.CustomButton;
import com.tutorial.androidgametutorial.ui.GameImages;

public class Menu extends BaseState implements GameStateInterface {

    private CustomButton btnStart, btnLeaderboard;

    private int menuX = MainActivity.GAME_WIDTH / 6;
    private int menuY = 200;

    private int btnStartX = menuX + GameImages.MAINMENU_MENUBG.getImage().getWidth() / 2 - ButtonImages.MENU_START.getWidth() / 2;
    private int btnStartY = menuY + 100;

    private int btnLeaderboardX = btnStartX;
    private int btnLeaderboardY = btnStartY + 120; // Đặt nút leaderboard dưới nút start

    public Menu(Game game) {
        super(game);
        btnStart = new CustomButton(btnStartX, btnStartY, ButtonImages.MENU_START.getWidth(), ButtonImages.MENU_START.getHeight());
        btnLeaderboard = new CustomButton(btnLeaderboardX, btnLeaderboardY, ButtonImages.MENU_START.getWidth(), ButtonImages.MENU_START.getHeight());
    }

    @Override
    public void update(double delta) {

    }

    @Override
    public void render(Canvas c) {
        c.drawBitmap(
                GameImages.MAINMENU_MENUBG.getImage(),
                menuX,
                menuY,
                null);


        c.drawBitmap(
                ButtonImages.MENU_START.getBtnImg(btnStart.isPushed()),
                btnStart.getHitbox().left,
                btnStart.getHitbox().top,
                null);

        // Vẽ nút Leaderboard (sử dụng cùng style với nút Start)
        c.drawBitmap(
                ButtonImages.MENU_START.getBtnImg(btnLeaderboard.isPushed()),
                btnLeaderboard.getHitbox().left,
                btnLeaderboard.getHitbox().top,
                null);

        // Vẽ text "BẢNG XẾP HẠNG" lên nút leaderboard
        Paint textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(24);
        textPaint.setFakeBoldText(true);
        textPaint.setTextAlign(Paint.Align.CENTER);

        float textX = btnLeaderboard.getHitbox().centerX();
        float textY = btnLeaderboard.getHitbox().centerY() + 8;
        c.drawText("BẢNG XẾP HẠNG", textX, textY, textPaint);
    }

    @Override
    public void touchEvents(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (isIn(event, btnStart))
                btnStart.setPushed(true);
            else if (isIn(event, btnLeaderboard))
                btnLeaderboard.setPushed(true);
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (isIn(event, btnStart)) {
                if (btnStart.isPushed()) {
                    // Reset game về trạng thái ban đầu trước khi start
                    game.getPlaying().resetGame();
                    game.setCurrentGameState(Game.GameState.PLAYING);
                }
            } else if (isIn(event, btnLeaderboard)) {
                if (btnLeaderboard.isPushed()) {
                    // Cập nhật leaderboard và chuyển đến màn hình leaderboard
                    game.getLeaderboardScreen().updateLeaderboard();
                    game.setCurrentGameState(Game.GameState.LEADERBOARD);
                }
            }

            btnStart.setPushed(false);
            btnLeaderboard.setPushed(false);
        }
    }
}
