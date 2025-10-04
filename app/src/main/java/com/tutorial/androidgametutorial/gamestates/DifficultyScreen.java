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

public class DifficultyScreen extends BaseState implements GameStateInterface {

    private CustomButton btnEasy, btnHard, btnBack;

    private int menuX = MainActivity.GAME_WIDTH / 6;
    private int menuY = 200;

    private int btnEasyX = menuX + GameImages.MAINMENU_MENUBG.getImage().getWidth() / 2 - ButtonImages.MENU_START.getWidth() / 2;
    private int btnEasyY = menuY + 80;

    private int btnHardX = btnEasyX;
    private int btnHardY = btnEasyY + 100;

    private int btnBackX = btnEasyX;
    private int btnBackY = btnHardY + 100;

    public DifficultyScreen(Game game) {
        super(game);
        btnEasy = new CustomButton(btnEasyX, btnEasyY, ButtonImages.MENU_START.getWidth(), ButtonImages.MENU_START.getHeight());
        btnHard = new CustomButton(btnHardX, btnHardY, ButtonImages.MENU_START.getWidth(), ButtonImages.MENU_START.getHeight());
        btnBack = new CustomButton(btnBackX, btnBackY, ButtonImages.MENU_START.getWidth(), ButtonImages.MENU_START.getHeight());
    }

    @Override
    public void update(double delta) {

    }

    @Override
    public void render(Canvas c) {
        // Vẽ background menu
        c.drawBitmap(
                GameImages.MAINMENU_MENUBG.getImage(),
                menuX,
                menuY,
                null);

        // Vẽ title
        Paint titlePaint = new Paint();
        titlePaint.setColor(Color.WHITE);
        titlePaint.setTextSize(36);
        titlePaint.setFakeBoldText(true);
        titlePaint.setTextAlign(Paint.Align.CENTER);

        float titleX = MainActivity.GAME_WIDTH / 2f;
        float titleY = menuY + 40;
        c.drawText("CHỌN ĐỘ KHÓ", titleX, titleY, titlePaint);

        // Vẽ nút Easy
        c.drawBitmap(
                ButtonImages.MENU_START.getBtnImg(btnEasy.isPushed()),
                btnEasy.getHitbox().left,
                btnEasy.getHitbox().top,
                null);

        // Vẽ text "DỄ" lên nút easy
        Paint textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(28);
        textPaint.setFakeBoldText(true);
        textPaint.setTextAlign(Paint.Align.CENTER);

        float easyTextX = btnEasy.getHitbox().centerX();
        float easyTextY = btnEasy.getHitbox().centerY() + 10;
        c.drawText("DỄ", easyTextX, easyTextY, textPaint);

        // Vẽ nút Hard
        c.drawBitmap(
                ButtonImages.MENU_START.getBtnImg(btnHard.isPushed()),
                btnHard.getHitbox().left,
                btnHard.getHitbox().top,
                null);

        // Vẽ text "KHÓ" lên nút hard
        float hardTextX = btnHard.getHitbox().centerX();
        float hardTextY = btnHard.getHitbox().centerY() + 10;
        c.drawText("KHÓ", hardTextX, hardTextY, textPaint);

        // Vẽ nút Back
        c.drawBitmap(
                ButtonImages.MENU_START.getBtnImg(btnBack.isPushed()),
                btnBack.getHitbox().left,
                btnBack.getHitbox().top,
                null);

        // Vẽ text "QUAY LẠI" lên nút back
        textPaint.setTextSize(20);
        float backTextX = btnBack.getHitbox().centerX();
        float backTextY = btnBack.getHitbox().centerY() + 8;
        c.drawText("QUAY LẠI", backTextX, backTextY, textPaint);

        // Vẽ mô tả độ khó
        Paint descPaint = new Paint();
        descPaint.setColor(Color.YELLOW);
        descPaint.setTextSize(18);
        descPaint.setTextAlign(Paint.Align.CENTER);

        float descX = MainActivity.GAME_WIDTH / 2f;
        c.drawText("DỄ: Quái không đuổi theo, ít máu, ít sát thương", descX, btnEasy.getHitbox().bottom + 30, descPaint);
        c.drawText("KHÓ: Quái đuổi theo, nhiều máu, sát thương cao", descX, btnHard.getHitbox().bottom + 30, descPaint);
    }

    @Override
    public void touchEvents(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (isIn(event, btnEasy))
                btnEasy.setPushed(true);
            else if (isIn(event, btnHard))
                btnHard.setPushed(true);
            else if (isIn(event, btnBack))
                btnBack.setPushed(true);
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (isIn(event, btnEasy)) {
                if (btnEasy.isPushed()) {
                    // Set difficulty to easy và start game
                    game.setDifficulty(Game.Difficulty.EASY);
                    game.getPlaying().resetGame();
                    game.setCurrentGameState(Game.GameState.PLAYING);
                }
            } else if (isIn(event, btnHard)) {
                if (btnHard.isPushed()) {
                    // Set difficulty to hard và start game
                    game.setDifficulty(Game.Difficulty.HARD);
                    game.getPlaying().resetGame();
                    game.setCurrentGameState(Game.GameState.PLAYING);
                }
            } else if (isIn(event, btnBack)) {
                if (btnBack.isPushed()) {
                    // Quay lại menu
                    game.setCurrentGameState(Game.GameState.MENU);
                }
            }

            btnEasy.setPushed(false);
            btnHard.setPushed(false);
            btnBack.setPushed(false);
        }
    }
}
