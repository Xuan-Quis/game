package com.tutorial.androidgametutorial.ui;

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
        drawUI(c);
    }

    private void drawUI(Canvas c) {
        // Joystick
        c.drawCircle(joystickCenterPos.x, joystickCenterPos.y, joystickRadius, circlePaint);

        // Attack
        c.drawCircle(attackBtnCenterPos.x, attackBtnCenterPos.y, attackBtnRadius, circlePaint);

        // Skill
        c.drawCircle(skillBtnCenterPos.x, skillBtnCenterPos.y, skillBtnRadius, circlePaint);

        // Spark Skill
        c.drawCircle(sparkSkillBtnCenterPos.x, sparkSkillBtnCenterPos.y, sparkSkillBtnRadius, circlePaint);

        // Menu
        c.drawBitmap(
                ButtonImages.PLAYING_MENU.getBtnImg(btnMenu.isPushed(btnMenu.getPointerId())),
                btnMenu.getHitbox().left,
                btnMenu.getHitbox().top,
                null);

        // Health
        drawHealth(c);
    }

    private void drawHealth(Canvas c) {
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
