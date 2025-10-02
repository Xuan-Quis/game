package com.tutorial.androidgametutorial.main;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.tutorial.androidgametutorial.R;
import com.tutorial.androidgametutorial.gamestates.DeathScreen;
import com.tutorial.androidgametutorial.gamestates.LeaderboardScreen;
import com.tutorial.androidgametutorial.gamestates.Menu;
import com.tutorial.androidgametutorial.gamestates.Playing;
import com.tutorial.androidgametutorial.gamestates.WinScreen;
import com.tutorial.androidgametutorial.ui.CustomButton;

public class Game {

    private SurfaceHolder holder;
    private Menu menu;
    private Playing playing;
    private DeathScreen deathScreen;
    private WinScreen winScreen;
    private LeaderboardScreen leaderboardScreen;
    private GameLoop gameLoop;
    private GameState currentGameState = GameState.MENU;

    private Context context;
    private MediaPlayer backgroundMusic;
    private CustomButton toggleMusicButton, toggleSwordSoundButton;
    private boolean isMusicOn = true, isSwordSoundOn = true;

    // Update toggle buttons to use images for music and sound
    private Bitmap musicIcon, soundIcon;

    public Game(SurfaceHolder holder, Context context) {
        this.holder = holder;
        this.context = context;
        gameLoop = new GameLoop(this);
        initGameStates();
    }

    private void initGameStates() {
        menu = new Menu(this);
        playing = new Playing(this);
        deathScreen = new DeathScreen(this);
        winScreen = new WinScreen(this);
        leaderboardScreen = new LeaderboardScreen(this);

        // Initialize MediaPlayer for background music from assets folder
        try {
            AssetFileDescriptor afd = context.getAssets().openFd("background.mp3");
            backgroundMusic = new MediaPlayer();
            backgroundMusic.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            backgroundMusic.prepare();
            backgroundMusic.setLooping(true);
            backgroundMusic.setVolume(0.5f, 0.5f);
            afd.close();
        } catch (Exception e) {
            System.err.println("Error initializing MediaPlayer from assets: " + e.getMessage());
            backgroundMusic = null;
        }

        // Load button images
        musicIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.music);
        soundIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.sound);

        // Initialize buttons with much smaller size - 64x64 pixels
        float buttonSize = 64;
        float padding = 20;
        float screenWidth = MainActivity.GAME_WIDTH;

        toggleMusicButton = new CustomButton(screenWidth - buttonSize - padding, padding, buttonSize, buttonSize);
        toggleSwordSoundButton = new CustomButton(screenWidth - buttonSize - padding, padding + buttonSize + 15, buttonSize, buttonSize);
    }

    public void update(double delta) {
        // Only play background music in PLAYING state
        if (currentGameState == GameState.PLAYING) {
            if (isMusicOn && backgroundMusic != null && !backgroundMusic.isPlaying()) {
                try {
                    backgroundMusic.start();
                } catch (Exception e) {
                    System.err.println("Error starting background music: " + e.getMessage());
                }
            }
        } else {
            // Pause music if not in PLAYING state
            if (backgroundMusic != null && backgroundMusic.isPlaying()) {
                try {
                    backgroundMusic.pause();
                } catch (Exception e) {
                    System.err.println("Error pausing background music: " + e.getMessage());
                }
            }
        }

        switch (currentGameState) {
            case MENU -> menu.update(delta);
            case PLAYING -> playing.update(delta);
            case DEATH_SCREEN -> deathScreen.update(delta);
            case WIN_SCREEN -> winScreen.update(delta);
            case LEADERBOARD -> leaderboardScreen.update(delta);
        }
    }

    public void render() {
        Canvas c = holder.lockCanvas();
        c.drawColor(Color.BLACK);

        // Draw the game
        switch (currentGameState) {
            case MENU -> menu.render(c);
            case PLAYING -> {
                playing.render(c);

                if (musicIcon != null) {
                    Bitmap scaledMusicIcon = Bitmap.createScaledBitmap(musicIcon, 64, 64, false);
                    c.drawBitmap(scaledMusicIcon, toggleMusicButton.getHitbox().left, toggleMusicButton.getHitbox().top, null);
                }
                if (soundIcon != null) {
                    Bitmap scaledSoundIcon = Bitmap.createScaledBitmap(soundIcon, 64, 64, false);
                    c.drawBitmap(scaledSoundIcon, toggleSwordSoundButton.getHitbox().left, toggleSwordSoundButton.getHitbox().top, null);
                }
            }
            case DEATH_SCREEN -> deathScreen.render(c);
            case WIN_SCREEN -> winScreen.render(c);
            case LEADERBOARD -> leaderboardScreen.render(c);
        }

        holder.unlockCanvasAndPost(c);
    }

    public boolean touchEvent(MotionEvent event) {
        if (currentGameState == GameState.PLAYING && event.getAction() == MotionEvent.ACTION_DOWN) {
            if (toggleMusicButton.getHitbox().contains(event.getX(), event.getY())) {
                isMusicOn = !isMusicOn;
                if (backgroundMusic != null) {
                    try {
                        if (isMusicOn) {
                            backgroundMusic.start();
                        } else {
                            backgroundMusic.pause();
                        }
                    } catch (Exception e) {
                        System.err.println("Error toggling background music: " + e.getMessage());
                    }
                }
            } else if (toggleSwordSoundButton.getHitbox().contains(event.getX(), event.getY())) {
                isSwordSoundOn = !isSwordSoundOn;
                // Pass the sound setting to Playing class
                playing.setSwordSoundEnabled(isSwordSoundOn);
            }
        }

        switch (currentGameState) {
            case MENU -> menu.touchEvents(event);
            case PLAYING -> playing.touchEvents(event);
            case DEATH_SCREEN -> deathScreen.touchEvents(event);
            case WIN_SCREEN -> winScreen.touchEvents(event);
            case LEADERBOARD -> leaderboardScreen.touchEvents(event);
        }

        return true;
    }

    public void startGameLoop() {
        gameLoop.startGameLoop();
    }

    public enum GameState {
        MENU, PLAYING, DEATH_SCREEN, WIN_SCREEN, LEADERBOARD;
    }

    public GameState getCurrentGameState() {
        return currentGameState;
    }

    public void setCurrentGameState(GameState currentGameState) {
        this.currentGameState = currentGameState;
    }

    public Menu getMenu() {
        return menu;
    }

    public Playing getPlaying() {
        return playing;
    }

    public DeathScreen getDeathScreen() {
        return deathScreen;
    }

    public WinScreen getWinScreen() {
        return winScreen;
    }

    public LeaderboardScreen getLeaderboardScreen() {
        return leaderboardScreen;
    }

    public Context getContext() {
        return context;
    }

    // Clean up MediaPlayer when game is destroyed
    public void cleanup() {
        if (backgroundMusic != null) {
            try {
                backgroundMusic.release();
            } catch (Exception e) {
                System.err.println("Error releasing MediaPlayer: " + e.getMessage());
            }
        }
    }
}
