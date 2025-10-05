package com.tutorial.androidgametutorial.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.view.MotionEvent;

import com.tutorial.androidgametutorial.entities.Player;
import com.tutorial.androidgametutorial.gamestates.Playing;
import com.tutorial.androidgametutorial.main.Game;

public class PlayingUI {

    // Joystick
    private final PointF joystickCenterPos = new PointF(250, 800);
    private final int joystickRadius = 120;
    private int joystickPointerId = -1;
    private boolean touchDown;

    // Attack button
    private final PointF attackBtnCenterPos = new PointF(1700, 800);
    private final int attackBtnRadius = 80;
    private int attackBtnPointerId = -1;

    // Skill button (ném kiếm)
    private final PointF skillBtnCenterPos = new PointF(attackBtnCenterPos.x - 200, attackBtnCenterPos.y);
    private final int skillBtnRadius = attackBtnRadius;
    private int skillBtnPointerId = -1;

    // Spark skill button (phía trên nút đánh thường)
    private final PointF sparkSkillBtnCenterPos = new PointF(attackBtnCenterPos.x, attackBtnCenterPos.y - 200);
    private final int sparkSkillBtnRadius = skillBtnRadius;
    private int sparkSkillBtnPointerId = -1;

    // Paint để vẽ
    private final Paint circlePaint;

    // Menu
    private final CustomButton btnMenu;

    // Health
    private final int healthIconX = 150, healthIconY = 25;

    private final Playing playing;


    public PlayingUI(Playing playing) {
        this.playing = playing;

        circlePaint = new Paint();
        circlePaint.setColor(Color.RED);
        circlePaint.setStrokeWidth(5);
        circlePaint.setStyle(Paint.Style.STROKE);

        btnMenu = new CustomButton(5, 5,
                ButtonImages.PLAYING_MENU.getWidth(),
                ButtonImages.PLAYING_MENU.getHeight());
    }

    public void draw(Canvas c) {
        drawJoystick(c);
        drawAttackBtn(c);
        drawSkillBtn(c);
        drawSparkSkillBtn(c);
        drawMenuButton(c); // Fixed: use custom draw method instead of btnMenu.draw(c)
        drawHealthBar(c);
        drawStatusEffects(c);
        drawMapLevel(c); // Add map level indicator
    }

    private void drawJoystick(Canvas c) {
        c.drawCircle(joystickCenterPos.x, joystickCenterPos.y, joystickRadius, circlePaint);
    }

    private void drawAttackBtn(Canvas c) {
        c.drawCircle(attackBtnCenterPos.x, attackBtnCenterPos.y, attackBtnRadius, circlePaint);
    }

    private void drawSkillBtn(Canvas c) {
        c.drawCircle(skillBtnCenterPos.x, skillBtnCenterPos.y, skillBtnRadius, circlePaint);
    }

    private void drawSparkSkillBtn(Canvas c) {
        c.drawCircle(sparkSkillBtnCenterPos.x, sparkSkillBtnCenterPos.y, sparkSkillBtnRadius, circlePaint);
    }

    private void drawMenuButton(Canvas c) {
        // Draw the menu button using ButtonImages
        Bitmap buttonImage = ButtonImages.PLAYING_MENU.getBtnImg(btnMenu.isPushed());
        c.drawBitmap(buttonImage, btnMenu.getHitbox().left, btnMenu.getHitbox().top, null);
    }

    private void drawHealthBar(Canvas c) {
        Player player = playing.getPlayer();
        for (int i = 0; i < player.getMaxHealth() / 100; i++) {
            int x = healthIconX + 100 * i;
            int heartValue = player.getCurrentHealth() - 100 * i;

            if (heartValue < 100) {
                if (heartValue <= 0)
                    c.drawBitmap(HealthIcons.HEART_EMPTY.getIcon(), x, healthIconY, null);
                else if (heartValue == 25)
                    c.drawBitmap(HealthIcons.HEART_1Q.getIcon(), x, healthIconY, null);
                else if (heartValue == 50)
                    c.drawBitmap(HealthIcons.HEART_HALF.getIcon(), x, healthIconY, null);
                else
                    c.drawBitmap(HealthIcons.HEART_3Q.getIcon(), x, healthIconY, null);
            } else {
                c.drawBitmap(HealthIcons.HEART_FULL.getIcon(), x, healthIconY, null);
            }
        }
    }

    private void drawStatusEffects(Canvas c) {
        Player player = playing.getPlayer();

        // Vị trí hiển thị status effects bên dưới thanh máu
        int statusY = healthIconY + 80; // Dưới thanh máu 80px
        int speedX = healthIconX; // Cùng vị trí X với thanh máu
        int armorX = healthIconX + 200; // Cách Speed 200px

        // Paint cho text
        Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(30);
        textPaint.setFakeBoldText(true);
        textPaint.setAntiAlias(true);

        // Paint cho background
        Paint bgPaint = new Paint();
        bgPaint.setColor(Color.argb(150, 0, 0, 0)); // Nền đen trong suốt
        bgPaint.setAntiAlias(true);

        // Hiển thị Speed Status
        if (player.hasSpeedBoost()) {
            // Vẽ background cho Speed
            c.drawRoundRect(speedX - 10, statusY - 5, speedX + 150, statusY + 35, 10, 10, bgPaint);

            // Vẽ text Speed với màu vàng
            textPaint.setColor(Color.YELLOW);
            c.drawText("SPEED", speedX, statusY + 25, textPaint);

            // Hiển thị thời gian còn lại (nếu có)
            long timeLeft = player.getSpeedBoostTimeLeft();
            if (timeLeft > 0) {
                textPaint.setTextSize(20);
                c.drawText(timeLeft/1000 + "s", speedX + 80, statusY + 25, textPaint);
                textPaint.setTextSize(30);
            }
        }

        // Hiển thị Armor Status (Shield)
        if (player.hasShield()) {
            // Vẽ background cho Armor
            c.drawRoundRect(armorX - 10, statusY - 5, armorX + 180, statusY + 35, 10, 10, bgPaint);

            // Vẽ text Armor với màu xanh cyan
            textPaint.setColor(Color.CYAN);
            c.drawText("ARMOR", armorX, statusY + 25, textPaint);

            // Hiển thị số lượng hits còn lại
            textPaint.setTextSize(20);
            textPaint.setColor(Color.WHITE);
            c.drawText("x" + player.getShieldHits(), armorX + 90, statusY + 25, textPaint);
            textPaint.setTextSize(30);
        }
    }

    private void drawMapLevel(Canvas c) {
        try {
            // Get current map level from playing with null safety
            if (playing == null || playing.getMapManager() == null) {
                return; // Exit safely if objects are null
            }

            int currentMapLevel = playing.getMapManager().getCurrentMapLevel();

            // Position at top-right corner
            float mapTextX = 1600;
            float mapTextY = 80;

            // Paint for map level text
            Paint mapLevelPaint = new Paint();
            mapLevelPaint.setColor(Color.WHITE);
            mapLevelPaint.setTextSize(40);
            mapLevelPaint.setFakeBoldText(true);
            mapLevelPaint.setAntiAlias(true);

            // Paint for background
            Paint bgPaint = new Paint();
            bgPaint.setAntiAlias(true);

            String mapText;
            if (currentMapLevel == 1) {
                mapText = "MAP 1";
                bgPaint.setColor(Color.argb(150, 0, 100, 0)); // Green background for Map 1
            } else {
                mapText = "MAP 2 - SNOW";
                bgPaint.setColor(Color.argb(150, 0, 150, 200)); // Blue background for Snow Map
                mapLevelPaint.setColor(Color.CYAN); // Cyan text for snow theme
            }

            // Draw background
            c.drawRoundRect(mapTextX - 20, mapTextY - 35, mapTextX + 250, mapTextY + 15, 15, 15, bgPaint);

            // Draw map level text
            c.drawText(mapText, mapTextX, mapTextY, mapLevelPaint);
        } catch (Exception e) {
            // If any error occurs, just don't draw the map level indicator
            System.err.println("Error drawing map level: " + e.getMessage());
        }
    }

    // ========= INPUT =========
    public void touchEvents(MotionEvent event) {
        final int action = event.getActionMasked();
        final int actionIndex = event.getActionIndex();
        final int pointerId = event.getPointerId(actionIndex);

        final PointF eventPos = new PointF(event.getX(actionIndex), event.getY(actionIndex));

        switch (action) {
            case MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                if (checkInsideJoystick(eventPos, pointerId)) {
                    touchDown = true;
                } else if (checkInsideAttackBtn(eventPos)) {
                    if (attackBtnPointerId < 0) {
                        playing.getPlayer().setAttacking(true);
                        attackBtnPointerId = pointerId;
                    }
                } else if (checkInsideSkillBtn(eventPos)) {
                    if (skillBtnPointerId < 0) {
                        playing.castThrowSwordSkill(); // gọi hàm skill trong Playing
                        skillBtnPointerId = pointerId;
                    }
                } else if (checkInsideSparkSkillBtn(eventPos)) {
                    if (sparkSkillBtnPointerId < 0) {
                        playing.castSparkSkill(); // gọi hàm spark skill trong Playing
                        sparkSkillBtnPointerId = pointerId;
                    }
                } else {
                    if (isIn(eventPos, btnMenu))
                        btnMenu.setPushed(true, pointerId);
                }
            }

            case MotionEvent.ACTION_MOVE -> {
                if (touchDown) {
                    for (int i = 0; i < event.getPointerCount(); i++) {
                        if (event.getPointerId(i) == joystickPointerId) {
                            float xDiff = event.getX(i) - joystickCenterPos.x;
                            float yDiff = event.getY(i) - joystickCenterPos.y;
                            playing.setPlayerMoveTrue(new PointF(xDiff, yDiff));
                        }
                    }
                }
            }

            case MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                if (pointerId == joystickPointerId) {
                    resetJoystick();
                } else {
                    if (isIn(eventPos, btnMenu) && btnMenu.isPushed(pointerId)) {
                        resetJoystick();
                        playing.setGameStateToMenu();
                    }
                    btnMenu.unPush(pointerId);

                    if (pointerId == attackBtnPointerId) {
                        playing.getPlayer().setAttacking(false);
                        attackBtnPointerId = -1;
                    }
                    if (pointerId == skillBtnPointerId) {
                        skillBtnPointerId = -1;
                    }
                    if (pointerId == sparkSkillBtnPointerId) {
                        sparkSkillBtnPointerId = -1;
                    }
                }
            }
        }
    }

    // ========= SUPPORT =========
    private boolean checkInsideJoystick(PointF eventPos, int pointerId) {
        if (isInsideRadius(eventPos, joystickCenterPos, joystickRadius)) {
            joystickPointerId = pointerId;
            return true;
        }
        return false;
    }

    private boolean checkInsideAttackBtn(PointF eventPos) {
        return isInsideRadius(eventPos, attackBtnCenterPos, attackBtnRadius);
    }

    private boolean checkInsideSkillBtn(PointF eventPos) {
        return isInsideRadius(eventPos, skillBtnCenterPos, skillBtnRadius);
    }

    private boolean checkInsideSparkSkillBtn(PointF eventPos) {
        return isInsideRadius(eventPos, sparkSkillBtnCenterPos, sparkSkillBtnRadius);
    }

    private boolean isInsideRadius(PointF eventPos, PointF center, int r) {
        float a = Math.abs(eventPos.x - center.x);
        float b = Math.abs(eventPos.y - center.y);
        float dist = (float) Math.hypot(a, b);
        return dist <= r;
    }

    private void resetJoystick() {
        touchDown = false;
        joystickPointerId = -1;
        playing.setPlayerMoveFalse();
    }

    private boolean isIn(PointF eventPos, CustomButton b) {
        return b.getHitbox().contains(eventPos.x, eventPos.y);
    }
}
