package com.tutorial.androidgametutorial.gamestates;

import android.view.MotionEvent;

import com.tutorial.androidgametutorial.main.Game;
import com.tutorial.androidgametutorial.ui.CustomButton;

public abstract class BaseState {
    protected Game game;

    public BaseState(Game game) {
        this.game = game;
    }

    public Game getGame() {
        return game;
    }

    public boolean isIn(MotionEvent e, CustomButton b) {
        return b.getHitbox().contains(e.getX(), e.getY());
    }
}
