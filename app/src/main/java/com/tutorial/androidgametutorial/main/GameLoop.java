package com.tutorial.androidgametutorial.main;

public class GameLoop implements Runnable {

    private Thread gameThread;
    private Game game;
    private volatile boolean running = false;

    public GameLoop(Game game) {
        this.game = game;
    }

    @Override
    public void run() {

        long lastFPScheck = System.currentTimeMillis();
        int fps = 0;

        long lastDelta = System.nanoTime();
        long nanoSec = 1_000_000_000;

        while (running) {
            long nowDelta = System.nanoTime();
            double timeSinceLastDelta = nowDelta - lastDelta;
            double delta = timeSinceLastDelta / nanoSec;

            game.update(delta);
            game.render();
            lastDelta = nowDelta;

//            fps++;
//            long now = System.currentTimeMillis();
//            if (now - lastFPScheck >= 1000) {
//                System.out.println("FPS: " + fps);
//                fps = 0;
//                lastFPScheck += 1000;
//            }

            // Add small sleep to prevent excessive CPU usage
            try {
                Thread.sleep(16); // ~60 FPS
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void startGameLoop() {
        if (!running) {
            running = true;
            gameThread = new Thread(this);
            gameThread.start();
        }
    }

    public void stopGameLoop() {
        running = false;
        if (gameThread != null) {
            try {
                gameThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
