package com.example.mygesture.common;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaMetadataRetriever;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class GestureUtils {

  private static final String TAG = "GestureUtils";
  private static GestureUtils instance = new GestureUtils();

  private static int mDeviceHeight = 0;

  public static GestureUtils getInstance() {
    return instance;
  }

  public static int getDeviceHeight(Context context) {
    if (mDeviceHeight != 0) {
      return mDeviceHeight;
    }

    WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    DisplayMetrics displayMetrics = new DisplayMetrics();

    if (wm != null) {
      wm.getDefaultDisplay().getMetrics(displayMetrics);
      mDeviceHeight = displayMetrics.heightPixels;
    }

    return mDeviceHeight;
  }

  public static int getDensity(Context context) {
    WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    DisplayMetrics displayMetrics = new DisplayMetrics();

    if (wm != null) {
      wm.getDefaultDisplay().getMetrics(displayMetrics);
      return displayMetrics.densityDpi;
    }

    return 1;
  }

  // 通知栏展开
  public boolean expandNotificationPanel(Context content) {
    try {
      Object service = content.getSystemService(Context.STATUS_BAR_SERVICE);
      Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");
      Method expand = null;
      if (Build.VERSION.SDK_INT >= 17) {
        expand = statusbarManager.getMethod("expandNotificationsPanel");
      } else {
        expand = statusbarManager.getMethod("expand");
      }
      if (expand != null) {
        expand.invoke(service);
      }
      Log.i(TAG, "通知栏已展开");
      return true;
    } catch (Exception e) {
      Log.d(TAG, "通知栏展开失败", e);
      return false;
    }
  }

  /**
   * 界面截屏
   * 注意：需要 WRITE_EXTERNAL_STORAGE 权限和截屏权限
   */
  public void takeScreenshot(Context context) {
    try {
      // 方法1：使用shell命令截屏
      String screenshotPath =
          Environment.getExternalStorageDirectory() + "/screenshot_" + System.currentTimeMillis() + ".png";
      Process process = Runtime.getRuntime().exec("screencap -p " + screenshotPath);
      process.waitFor();
      Log.i(TAG, "截屏保存到: " + screenshotPath);
      Toast.makeText(context, "截屏已保存", Toast.LENGTH_SHORT).show();
    } catch (Exception e) {
      Log.e(TAG, "截屏失败: " + e.getMessage());
      // 备用方案：打开系统截屏快捷键模拟
      simulateKeyPress(KeyEvent.KEYCODE_SYSRQ);
    }
  }

  /**
   * 打开任务栏（最近任务）
   */
  public void openRecentTasks(Context context) {
    try {
      // 方法1：使用反射调用
      Object service = context.getSystemService(Context.ACTIVITY_SERVICE);
      Class<?> activityManager = Class.forName("android.app.ActivityManager");
      Method method = activityManager.getMethod("getRecentTasks", int.class, int.class);
      method.invoke(service, 20, ActivityManager.RECENT_WITH_EXCLUDED);

      // 方法2：发送系统按键
      simulateKeyPress(KeyEvent.KEYCODE_APP_SWITCH);
      Log.i(TAG, "打开最近任务");
    } catch (Exception e) {
      Log.e(TAG, "打开最近任务失败: " + e.getMessage());
    }
  }

  // 锁屏
  public boolean lockScreen(Context context) {
    try {
      DevicePolicyManager devicePolicyManager =
          (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
      if (devicePolicyManager != null) {
        devicePolicyManager.lockNow();
        Log.i(TAG, "屏幕已锁定");
        return true;
      }
    } catch (Exception e) {
      Log.e(TAG, "使用设备管理员锁屏失败: " + e.getMessage());
    }
    return false;
  }

  /**
   * 获取手机应用列表
   */
  public List<ApplicationInfo> getInstalledApps(Context context) {
    PackageManager packageManager = context.getPackageManager();
    List<ApplicationInfo> apps = new ArrayList<>();

    try {
      List<ApplicationInfo> installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
      for (ApplicationInfo app : installedApps) {
        // 过滤掉系统应用（可选）
        if ((app.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
          apps.add(app);
        }
      }
      Log.i(TAG, "获取到 " + apps.size() + " 个用户应用");
    } catch (Exception e) {
      Log.e(TAG, "获取应用列表失败: " + e.getMessage());
    }

    return apps;
  }

  /**
   * 获取可启动的应用列表
   */
  public List<ResolveInfo> getLaunchableApps(Context context) {
    PackageManager packageManager = context.getPackageManager();
    Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
    mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

    return packageManager.queryIntentActivities(mainIntent, 0);
  }

  /**
   * 应用返回（模拟返回键）
   */
  public boolean goBack(Context context) {
    try {
      simulateKeyPress(KeyEvent.KEYCODE_BACK);
      Log.i(TAG, "执行返回操作");
      return true;
    } catch (Exception e) {
      Log.e(TAG, "返回操作失败: " + e.getMessage());
    }
    return false;
  }

  /**
   * 音乐控制 - 播放/暂停
   */
  public void musicPlayPause(Context context) {
    try {
      Intent intent = new Intent("com.android.music.musicservicecommand");
      intent.putExtra("command", "togglepause");
      context.sendBroadcast(intent);

      // 备用方案：媒体按键
      //      simulateKeyPress(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
      Log.i(TAG, "音乐播放/暂停");
    } catch (Exception e) {
      Log.e(TAG, "音乐控制失败: " + e.getMessage());
    }
  }

  /**
   * 音乐控制 - 上一首
   */
  public void musicPrevious(Context context) {
    try {
      Intent intent = new Intent("com.android.music.musicservicecommand");
      intent.putExtra("command", "previous");
      context.sendBroadcast(intent);

      // 备用方案：媒体按键
      simulateKeyPress(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
      Log.i(TAG, "播放上一首");
    } catch (Exception e) {
      Log.e(TAG, "上一首控制失败: " + e.getMessage());
    }
  }

  /**
   * 音乐控制 - 下一首
   */
  public void musicNext(Context context) {
    try {
      Intent intent = new Intent("com.android.music.musicservicecommand");
      intent.putExtra("command", "next");
      context.sendBroadcast(intent);

      // 备用方案：媒体按键
      simulateKeyPress(KeyEvent.KEYCODE_MEDIA_NEXT);
      Log.i(TAG, "播放下一首");
    } catch (Exception e) {
      Log.e(TAG, "下一首控制失败: " + e.getMessage());
    }
  }

  // 打开/关闭蓝牙
  @SuppressLint("MissingPermission")
  public boolean toggleBluetooth(Context context) {
    try {
      BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
      if (bluetoothAdapter != null) {
        if (bluetoothAdapter.isEnabled()) {
          bluetoothAdapter.disable();
          Log.i(TAG, "蓝牙已关闭");
          Toast.makeText(context, "蓝牙已关闭", Toast.LENGTH_SHORT).show();
        } else {
          bluetoothAdapter.enable();
          Log.i(TAG, "蓝牙已打开");
          Toast.makeText(context, "蓝牙已打开", Toast.LENGTH_SHORT).show();
        }
        return true;
      } else {
        Toast.makeText(context, "设备不支持蓝牙", Toast.LENGTH_SHORT).show();
      }
    } catch (Exception e) {
      Log.e(TAG, "蓝牙控制失败: " + e.getMessage());
    }
    return false;
  }

  /**
   * 打开/关闭WiFi
   */
  @SuppressLint("MissingPermission")
  public void toggleWifi(Context context) {
    try {
      WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
      if (wifiManager != null) {
        if (wifiManager.isWifiEnabled()) {
          wifiManager.setWifiEnabled(false);
          Log.i(TAG, "WiFi已关闭");
          Toast.makeText(context, "WiFi已关闭", Toast.LENGTH_SHORT).show();
        } else {
          wifiManager.setWifiEnabled(true);
          Log.i(TAG, "WiFi已打开");
          Toast.makeText(context, "WiFi已打开", Toast.LENGTH_SHORT).show();
        }
      }
    } catch (Exception e) {
      Log.e(TAG, "WiFi控制失败: " + e.getMessage());
    }
  }

  // 打开设置页面
  public boolean openSettings(Context context) {
    try {
      Intent intent = new Intent(Settings.ACTION_SETTINGS);
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      context.startActivity(intent);
      Log.i(TAG, "打开系统设置");
      return true;
    } catch (Exception e) {
      Log.e(TAG, "打开设置失败: " + e.getMessage());
      return false;
    }
  }

  /**
   * 模拟按键（需要root权限或系统权限）
   */
  private void simulateKeyPress(int keyCode) {
    try {
      Runtime.getRuntime().exec("input keyevent " + keyCode);
    } catch (IOException e) {
      Log.e(TAG, "模拟按键失败: " + e.getMessage());
    }
  }

  /**
   * 获取当前运行的应用
   */
  public String getCurrentApp(Context context) {
    try {
      ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
      if (activityManager != null) {
        List<ActivityManager.RunningAppProcessInfo> runningApps = activityManager.getRunningAppProcesses();
        if (runningApps != null && !runningApps.isEmpty()) {
          return runningApps.get(0).processName;
        }
      }
    } catch (Exception e) {
      Log.e(TAG, "获取当前应用失败: " + e.getMessage());
    }
    return null;
  }

  /**
   * 调整系统音量
   */
  public void adjustVolume(Context context, boolean increase) {
    try {
      int keyCode = increase ? KeyEvent.KEYCODE_VOLUME_UP : KeyEvent.KEYCODE_VOLUME_DOWN;
      simulateKeyPress(keyCode);
      Log.i(TAG, "调整音量: " + (increase ? "增加" : "减少"));
    } catch (Exception e) {
      Log.e(TAG, "调整音量失败: " + e.getMessage());
    }
  }
}
