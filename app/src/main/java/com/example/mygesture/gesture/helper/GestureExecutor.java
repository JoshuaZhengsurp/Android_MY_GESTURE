package com.example.mygesture.gesture.helper;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.example.mygesture.common.GestureConst;
import com.example.mygesture.manager.AppManager;

/**
 * 手势执行器 - 负责执行各种手势动作
 * todo：后续将手势回调执行收束到这个类里
 */
public class GestureExecutor {
    private static final String TAG = "GestureExecutor";
    private static final String PREFS_NAME = "gesture_settings";
    
    private Context context;
    private AppManager appManager;
    private SharedPreferences preferences;

    public GestureExecutor(Context context) {
        this.context = context.getApplicationContext();
        this.appManager = AppManager.getInstance(this.context);
        this.preferences = this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * 执行手势动作
     */
    public boolean executeGesture(String edgeType, String gestureType) {
        String key = getGestureKey(edgeType, gestureType);
        String actionType = preferences.getString(key + "_type", "");
        String actionValue = preferences.getString(key + "_value", "");
        String appPackage = preferences.getString(key + "_package", "");

        Log.d(TAG, "Executing gesture: " + edgeType + " " + gestureType + 
              " -> " + actionType + ":" + actionValue);

        switch (actionType) {
            case GestureConst.OPERATOR_TYPE_OPERATION:
                return executeSystemOperation(actionValue);
                
            case GestureConst.OPERATOR_TYPE_APP:
                return executeAppLaunch(appPackage, actionValue);
                
            case GestureConst.OPERATOR_TYPE_SHORTCUT:
                return executeShortcut(actionValue);
                
            default:
                Log.w(TAG, "Unknown action type: " + actionType);
                return false;
        }
    }

    /**
     * 保存手势设置
     */
    public void saveGestureSetting(String edgeType, String gestureType, 
                                 String actionType, String actionValue, String appPackage) {
        String key = getGestureKey(edgeType, gestureType);
        
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key + "_type", actionType);
        editor.putString(key + "_value", actionValue);
        if (appPackage != null) {
            editor.putString(key + "_package", appPackage);
        }
        editor.apply();
        
        Log.d(TAG, "Saved gesture setting: " + key + " -> " + actionType + ":" + actionValue);
    }

    /**
     * 获取手势设置
     */
    public GestureSetting getGestureSetting(String edgeType, String gestureType) {
        String key = getGestureKey(edgeType, gestureType);
        String actionType = preferences.getString(key + "_type", "");
        String actionValue = preferences.getString(key + "_value", "");
        String appPackage = preferences.getString(key + "_package", "");
        
        return new GestureSetting(actionType, actionValue, appPackage);
    }

    /**
     * 执行系统操作
     */
    private boolean executeSystemOperation(String operation) {
        try {
            switch (operation) {
                case "back":
                    return performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                    
                case "home":
                    return performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
                    
                case "recent_tasks":
                    return performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS);
                    
                case "expand_notification":
                    return expandNotificationPanel();
                    
                case "quick_settings":
                    return expandQuickSettings();
                    
                case "power_menu":
                    return performGlobalAction(AccessibilityService.GLOBAL_ACTION_POWER_DIALOG);
                    
                case "split_screen":
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        return performGlobalAction(AccessibilityService.GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN);
                    }
                    return false;
                    
                case "lock_screen":
                    return performGlobalAction(AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN);
                    
                case "screenshot":
                    return takeScreenshot();
                    
                default:
                    Log.w(TAG, "Unknown system operation: " + operation);
                    return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to execute system operation: " + operation, e);
            return false;
        }
    }

    /**
     * 启动应用
     */
    private boolean executeAppLaunch(String packageName, String appName) {
        try {
            if (packageName == null || packageName.isEmpty()) {
                Log.w(TAG, "Empty package name for app: " + appName);
                return false;
            }
            
            boolean success = appManager.launchApp(packageName);
            if (success) {
                showToast("启动应用: " + appName);
            } else {
                showToast("无法启动应用: " + appName);
            }
            return success;
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch app: " + packageName, e);
            showToast("启动应用失败: " + appName);
            return false;
        }
    }

    /**
     * 执行快捷操作
     */
    private boolean executeShortcut(String shortcut) {
        try {
            switch (shortcut) {
                case "toggle_wifi":
                    return toggleWifi();
                    
                case "toggle_bluetooth":
                    return toggleBluetooth();
                    
                case "toggle_flashlight":
                    return toggleFlashlight();
                    
                case "open_camera":
                    return openCamera();
                    
                case "volume_control":
                    return showVolumeControl();
                    
                case "brightness_control":
                    return showBrightnessControl();
                    
                default:
                    Log.w(TAG, "Unknown shortcut: " + shortcut);
                    return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to execute shortcut: " + shortcut, e);
            return false;
        }
    }

    /**
     * 执行无障碍全局动作（需要AccessibilityService）
     */
    private boolean performGlobalAction(int action) {
        // TODO: 需要与AccessibilityService集成
        Log.d(TAG, "Would perform global action: " + action);
        return true;
    }

    /**
     * 展开通知栏
     */
    private boolean expandNotificationPanel() {
        try {
            Intent intent = new Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS");
            context.sendBroadcast(intent);
            
            // 这需要系统权限，在实际应用中可能需要其他方式实现
            Object service = context.getSystemService("statusbar");
            if (service != null) {
                Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");
                java.lang.reflect.Method expand = statusbarManager.getMethod("expandNotificationsPanel");
                expand.invoke(service);
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to expand notification panel", e);
        }
        return false;
    }

    /**
     * 展开快速设置
     */
    private boolean expandQuickSettings() {
        try {
            Object service = context.getSystemService("statusbar");
            if (service != null) {
                Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");
                java.lang.reflect.Method expand = statusbarManager.getMethod("expandSettingsPanel");
                expand.invoke(service);
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to expand quick settings", e);
        }
        return false;
    }

    /**
     * 截图
     */
    private boolean takeScreenshot() {
        // TODO: 集成ScreenshotService
        Log.d(TAG, "Would take screenshot");
        showToast("截图功能");
        return true;
    }

    /**
     * WiFi开关
     */
    private boolean toggleWifi() {
        try {
            Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            showToast("打开WiFi设置");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to open WiFi settings", e);
            return false;
        }
    }

    /**
     * 蓝牙开关
     */
    private boolean toggleBluetooth() {
        try {
            Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            showToast("打开蓝牙设置");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to open Bluetooth settings", e);
            return false;
        }
    }

    /**
     * 手电筒开关
     */
    private boolean toggleFlashlight() {
        // TODO: 实现手电筒控制
        Log.d(TAG, "Would toggle flashlight");
        showToast("手电筒功能");
        return true;
    }

    /**
     * 打开相机
     */
    private boolean openCamera() {
        try {
            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            showToast("打开相机");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to open camera", e);
            return false;
        }
    }

    /**
     * 音量控制
     */
    private boolean showVolumeControl() {
        // TODO: 显示音量控制
        Log.d(TAG, "Would show volume control");
        showToast("音量控制");
        return true;
    }

    /**
     * 亮度控制
     */
    private boolean showBrightnessControl() {
        try {
            Intent intent = new Intent(Settings.ACTION_DISPLAY_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            showToast("打开显示设置");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to open display settings", e);
            return false;
        }
    }

    /**
     * 显示Toast消息
     */
    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * 生成手势唯一标识
     */
    private String getGestureKey(String edgeType, String gestureType) {
        return edgeType + "_" + gestureType;
    }

    /**
     * 手势设置数据类
     */
    public static class GestureSetting {
        public final String actionType;
        public final String actionValue;
        public final String appPackage;

        public GestureSetting(String actionType, String actionValue, String appPackage) {
            this.actionType = actionType;
            this.actionValue = actionValue;
            this.appPackage = appPackage;
        }

        public boolean isEmpty() {
            return actionType == null || actionType.isEmpty();
        }
    }
}
