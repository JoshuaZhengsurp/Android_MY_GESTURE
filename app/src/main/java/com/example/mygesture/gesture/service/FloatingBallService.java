package com.example.mygesture.gesture.service;

import android.animation.ObjectAnimator;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.example.mygesture.R;
import com.example.mygesture.common.GestureConst;
import com.example.mygesture.gesture.helper.FloatingBallManager;
import com.example.mygesture.gesture.helper.GestureManager;

public class FloatingBallService extends Service {
    
    private static final String TAG = "FloatingBallService";
    
    private WindowManager windowManager;
    private ImageView floatingBall;
    private WindowManager.LayoutParams params;
    
    // 用于拖拽的变量
    private int initialX;
    private int initialY;
    private float initialTouchX;
    private float initialTouchY;
    private boolean isDragging = false;
    
    // 手势检测相关变量
    private long firstTouchTime = 0;
    private long secondTouchTime = 0;
    private int touchCount = 0;
    private boolean isLongPressTriggered = false;
    private Handler gestureHandler = new Handler(Looper.getMainLooper());
    private Runnable longPressRunnable;
    private Runnable doubleClickTimeoutRunnable;
    
    // 手势检测阈值
    private static final long LONG_PRESS_THRESHOLD = 500;
    private static final long DOUBLE_CLICK_THRESHOLD = 300;
    private static final float MOVE_THRESHOLD = 10;
    
    // 屏幕尺寸
    private int screenWidth;
    private int screenHeight;
    private int ballSize = 150;

    private GestureManager mGestureManager;
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        getScreenSize();
        createFloatingBall();
    }
    
    private void getScreenSize() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;
    }
    
    private void createFloatingBall() {
        // 创建悬浮球视图
        floatingBall = new ImageView(this);
        floatingBall.setImageResource(R.drawable.ic_floating_ball);
        floatingBall.setScaleType(ImageView.ScaleType.CENTER_CROP);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            floatingBall.setElevation(8f);
        }

        params = new WindowManager.LayoutParams(
                ballSize,
                ballSize,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                        WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        
        // 设置初始位置（右边缘中间）
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = screenWidth - ballSize;
        params.y = screenHeight / 2 - ballSize / 2;
        
        // 设置触摸监听器
        floatingBall.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        return handleTouchDown(event);
                        
                    case MotionEvent.ACTION_MOVE:
                        return handleTouchMove(event);
                        
                    case MotionEvent.ACTION_UP:
                        return handleTouchUp(event);
                }
                return false;
            }
        });
        
        // 添加悬浮球到窗口
        try {
            windowManager.addView(floatingBall, params);
            Log.d(TAG, "Floating ball created successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "Error creating floating ball", e);
        }
    }
    
    private boolean handleTouchDown(MotionEvent event) {
        long currentTime = System.currentTimeMillis();
        isDragging = false;
        isLongPressTriggered = false;
        
        // 记录初始位置
        initialX = params.x;
        initialY = params.y;
        initialTouchX = event.getRawX();
        initialTouchY = event.getRawY();

        floatingBall.setScaleX(0.9f);
        floatingBall.setScaleY(0.9f);
        floatingBall.setAlpha(0.8f);
        
        // 处理双击检测
        if (touchCount == 0) {
            firstTouchTime = currentTime;
            touchCount = 1;
        } else if (touchCount == 1 && (currentTime - firstTouchTime) <= DOUBLE_CLICK_THRESHOLD) {
            secondTouchTime = currentTime;
            touchCount = 2;
        } else {
            firstTouchTime = currentTime;
            touchCount = 1;
        }

        cancelGestureCallbacks();
        longPressRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isDragging && !isLongPressTriggered) {
                    isLongPressTriggered = true;
                    onLongPress();
                }
            }
        };
        gestureHandler.postDelayed(longPressRunnable, LONG_PRESS_THRESHOLD);
        
        return true;
    }
    
    private boolean handleTouchMove(MotionEvent event) {
        // 计算移动距离
        float deltaX = event.getRawX() - initialTouchX;
        float deltaY = event.getRawY() - initialTouchY;

        if (Math.abs(deltaX) > MOVE_THRESHOLD || Math.abs(deltaY) > MOVE_THRESHOLD) {
            if (!isDragging) {
                isDragging = true;
                cancelGestureCallbacks();

                floatingBall.setScaleX(1.0f);
                floatingBall.setScaleY(1.0f);
                floatingBall.setAlpha(0.7f);
            }

            params.x = initialX + (int) deltaX;
            params.y = initialY + (int) deltaY;

            params.x = Math.max(0, Math.min(params.x, screenWidth - ballSize));
            params.y = Math.max(0, Math.min(params.y, screenHeight - ballSize));
            
            windowManager.updateViewLayout(floatingBall, params);
        }
        return true;
    }
    
    private boolean handleTouchUp(MotionEvent event) {
        // 恢复正常状态
        floatingBall.setScaleX(1.0f);
        floatingBall.setScaleY(1.0f);
        floatingBall.setAlpha(1.0f);
        
        cancelGestureCallbacks();
        
        if (isDragging) {
            // 拖拽结束，添加边缘吸附效果
            snapToEdge();
            resetGestureState();
        } else if (isLongPressTriggered) {
            resetGestureState();
        } else {
            if (touchCount == 2) {
                onDoubleClick();
                resetGestureState();
            } else {
                doubleClickTimeoutRunnable = new Runnable() {
                    @Override
                    public void run() {
                        if (touchCount == 1) {
                            onSingleClick();
                        }
                        resetGestureState();
                    }
                };
                gestureHandler.postDelayed(doubleClickTimeoutRunnable, DOUBLE_CLICK_THRESHOLD);
            }
        }
        return true;
    }
    
    private void cancelGestureCallbacks() {
        if (longPressRunnable != null) {
            gestureHandler.removeCallbacks(longPressRunnable);
            longPressRunnable = null;
        }
        if (doubleClickTimeoutRunnable != null) {
            gestureHandler.removeCallbacks(doubleClickTimeoutRunnable);
            doubleClickTimeoutRunnable = null;
        }
    }
    
    private void resetGestureState() {
        touchCount = 0;
        firstTouchTime = 0;
        secondTouchTime = 0;
        isLongPressTriggered = false;
    }

    private void onSingleClick() {
        Log.d(TAG, "Floating ball single clicked");
        playClickAnimation();
        
        // 通知FloatingBallManager处理点击手势
        FloatingBallManager.getInstance(this).handleFloatingBallGesture(
                GestureConst.FLOATING_BALL_GESTURE_CLICK);
    }

    private void onDoubleClick() {
        Log.d(TAG, "Floating ball double clicked");
        playDoubleClickAnimation();
        
        // 通知FloatingBallManager处理双击手势
        FloatingBallManager.getInstance(this).handleFloatingBallGesture(
                GestureConst.FLOATING_BALL_GESTURE_DOUBLE_CLICK);
    }

    private void onLongPress() {
        Log.d(TAG, "Floating ball long pressed");
        playLongPressAnimation();

        FloatingBallManager.getInstance(this).handleFloatingBallGesture(
                GestureConst.FLOATING_BALL_GESTURE_LONG_PRESS);
    }

    private void playClickAnimation() {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(floatingBall, "scaleX", 1f, 1.1f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(floatingBall, "scaleY", 1f, 1.1f, 1f);
        scaleX.setDuration(150);
        scaleY.setDuration(150);
        scaleX.start();
        scaleY.start();
    }
    
    /**
     * 双击动画效果
     */
    private void playDoubleClickAnimation() {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(floatingBall, "scaleX", 1f, 1.2f, 0.9f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(floatingBall, "scaleY", 1f, 1.2f, 0.9f, 1f);
        scaleX.setDuration(300);
        scaleY.setDuration(300);
        scaleX.start();
        scaleY.start();
    }
    
    /**
     * 长按动画效果
     */
    private void playLongPressAnimation() {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(floatingBall, "scaleX", 1f, 1.3f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(floatingBall, "scaleY", 1f, 1.3f, 1f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(floatingBall, "alpha", 1f, 0.6f, 1f);
        scaleX.setDuration(400);
        scaleY.setDuration(400);
        alpha.setDuration(400);
        scaleX.start();
        scaleY.start();
        alpha.start();
    }

    private void snapToEdge() {
        int targetX;

        if (params.x < screenWidth / 2) {
            // 靠近左边，吸附到左边
            targetX = 0;
        } else {
            // 靠近右边，吸附到右边
            targetX = screenWidth - ballSize;
        }
        
        int targetY = Math.max(0, Math.min(params.y, screenHeight - ballSize));
        
        ObjectAnimator animatorX = ObjectAnimator.ofInt(params, "x", params.x, targetX);
        ObjectAnimator animatorY = ObjectAnimator.ofInt(params, "y", params.y, targetY);
        
        animatorX.setDuration(300);
        animatorY.setDuration(300);
        animatorX.setInterpolator(new DecelerateInterpolator());
        animatorY.setInterpolator(new DecelerateInterpolator());
        
        animatorX.addUpdateListener(animation -> {
            params.x = (int) animation.getAnimatedValue();
            windowManager.updateViewLayout(floatingBall, params);
        });
        
        animatorY.addUpdateListener(animation -> {
            params.y = (int) animation.getAnimatedValue();
            windowManager.updateViewLayout(floatingBall, params);
        });
        
        animatorX.start();
        animatorY.start();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        cancelGestureCallbacks();
        
        // 通知FloatingBallManager服务已停止
        FloatingBallManager.getInstance(this).setFloatingBallShowing(false);
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
}
