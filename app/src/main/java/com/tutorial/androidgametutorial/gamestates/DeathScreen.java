package com.tutorial.androidgametutorial.gamestates;

import android.graphics.Canvas;
import android.view.MotionEvent;

import com.tutorial.androidgametutorial.helpers.interfaces.GameStateInterface;
import com.tutorial.androidgametutorial.main.Game;
import com.tutorial.androidgametutorial.main.MainActivity;
import com.tutorial.androidgametutorial.ui.ButtonImages;
import com.tutorial.androidgametutorial.ui.CustomButton;
import com.tutorial.androidgametutorial.ui.GameImages;

public class DeathScreen extends BaseState implements GameStateInterface {

    private CustomButton btnReplay, btnMainMenu, btnEscape;

    private int menuX = MainActivity.GAME_WIDTH / 6;
    private int menuY = 200;

    private int buttonsX = menuX + GameImages.DEATH_MENU_MENUBG.getImage().getWidth() / 2 - ButtonImages.MENU_START.getWidth() / 2;
    private int btnReplayY = menuY + 200, btnMainMenuY = btnReplayY + 150, btnEscapeY = btnMainMenuY + 150;


    public DeathScreen(Game game) {
        super(game);
        btnReplay = new CustomButton(buttonsX, btnReplayY, ButtonImages.MENU_REPLAY.getWidth(), ButtonImages.MENU_REPLAY.getHeight());
        btnMainMenu = new CustomButton(buttonsX, btnMainMenuY, ButtonImages.MENU_MENU.getWidth(), ButtonImages.MENU_MENU.getHeight());
        btnEscape = new CustomButton(buttonsX, btnEscapeY, ButtonImages.MENU_ESCAPE.getWidth(), ButtonImages.MENU_ESCAPE.getHeight());
    }


    @Override
    public void render(Canvas c) {
        drawBackground(c);
        drawButtons(c);
    }

    private void drawButtons(Canvas c) {
        c.drawBitmap(ButtonImages.MENU_REPLAY.getBtnImg(btnReplay.isPushed()),
                btnReplay.getHitbox().left,
                btnReplay.getHitbox().top,
                null);

        c.drawBitmap(ButtonImages.MENU_MENU.getBtnImg(btnMainMenu.isPushed()),
                btnMainMenu.getHitbox().left,
                btnMainMenu.getHitbox().top,
                null);

        c.drawBitmap(ButtonImages.MENU_ESCAPE.getBtnImg(btnEscape.isPushed()),
                btnEscape.getHitbox().left,
                btnEscape.getHitbox().top,
                null);
    }

    private void drawBackground(Canvas c) {
        c.drawBitmap(GameImages.DEATH_MENU_MENUBG.getImage(),
                menuX, menuY, null);
    }

    @Override
    public void touchEvents(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (isIn(event, btnReplay))
                btnReplay.setPushed(true);
            else if (isIn(event, btnMainMenu))
                btnMainMenu.setPushed(true);
            else if (isIn(event, btnEscape))
                btnEscape.setPushed(true);

        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (isIn(event, btnReplay)) {
                if (btnReplay.isPushed()) {
                    // Reset game về trạng thái ban đầu trước khi chuyển state
                    game.getPlaying().resetGame();
                    game.setCurrentGameState(Game.GameState.PLAYING);
                }
            } else if (isIn(event, btnMainMenu)) {
                if (btnMainMenu.isPushed())
                    game.setCurrentGameState(Game.GameState.MENU);
            } else if (isIn(event, btnEscape)) {
                if (btnEscape.isPushed())
                    System.exit(0); // Thoát game
            }

            btnReplay.setPushed(false);
            btnMainMenu.setPushed(false);
            btnEscape.setPushed(false);
        }

    }


    @Override
    public void update(double delta) {
    }

}
