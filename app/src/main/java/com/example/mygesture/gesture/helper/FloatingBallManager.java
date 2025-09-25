package com.example.mygesture.gesture.helper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.util.Pair;

import java.util.HashMap;

import com.example.mygesture.common.GestureConst;
import com.example.mygesture.gesture.view.FloatingBallGestureConfigView;
import com.example.mygesture.gesture.service.FloatingBallService;

public class FloatingBallManager {
    
    private static final String TAG = "FloatingBallManager";

    private static FloatingBallManager instance;
    private Context context;
    private boolean isFloatingBallShowing = false;
    private FloatingBallGestureConfigView mGestureConfigView;

    private FloatingBallManager(Context context) {
        this.context = context.getApplicationContext();
    }
    
    public static FloatingBallManager getInstance(Context context) {
        if (instance == null) {
            synchronized (FloatingBallManager.class) {
                if (instance == null) {
                    instance = new FloatingBallManager(context);
                }
            }
        }
        return instance;
    }

    public void setGestureConfigView(FloatingBallGestureConfigView configView) {
        this.mGestureConfigView = configView;
    }

    public void handleFloatingBallGesture(String gestureType) {
        Log.d(TAG, "handleFloatingBallGesture: " + gestureType);
        if (mGestureConfigView != null) {
          mGestureConfigView.handleGestureActionDetected(gestureType);
        }
    }

    public void showFloatingBall() {
        if (!canDrawOverlays()) {
            Log.w(TAG, "No overlay permission, requesting permission");
            requestOverlayPermission();
            return;
        }
        
        if (!isFloatingBallShowing) {
            Intent intent = new Intent(context, FloatingBallService.class);
            context.startService(intent);
            isFloatingBallShowing = true;
            Log.d(TAG, "Floating ball service started");
        }
    }

    public void hideFloatingBall() {
        if (isFloatingBallShowing) {
            Intent intent = new Intent(context, FloatingBallService.class);
            context.stopService(intent);
            isFloatingBallShowing = false;
            Log.d(TAG, "Floating ball service stopped");
        }
    }

    public void toggleFloatingBall() {
        if (isFloatingBallShowing) {
            hideFloatingBall();
        } else {
            showFloatingBall();
        }
    }

    public boolean canDrawOverlays() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(context);
        }
        return true;
    }

    public void requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    public boolean isFloatingBallShowing() {
        return isFloatingBallShowing;
    }
    
    public void setFloatingBallShowing(boolean showing) {
        this.isFloatingBallShowing = showing;
    }
}
