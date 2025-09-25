package com.example.mygesture.manager;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 应用管理器 - 负责获取和管理已安装的应用信息
 */
public class AppManager {
    private static final String TAG = "AppManager";
    private static AppManager instance;
    private Context context;
    private Map<String, AppInfo> installedApps;
    private PackageManager packageManager;

    private AppManager(Context context) {
        this.context = context.getApplicationContext();
        this.packageManager = this.context.getPackageManager();
        this.installedApps = new HashMap<>();
        loadInstalledApps();
    }

    public static AppManager getInstance(Context context) {
        if (instance == null) {
            instance = new AppManager(context);
        }
        return instance;
    }

    public static class AppInfo {
        public String packageName;
        public String appName;
        public Drawable icon;
        public boolean isSystemApp;
        public long installTime;

        public AppInfo(String packageName, String appName, Drawable icon, boolean isSystemApp, long installTime) {
            this.packageName = packageName;
            this.appName = appName;
            this.icon = icon;
            this.isSystemApp = isSystemApp;
            this.installTime = installTime;
        }
    }

    /**
     * 加载已安装的应用列表
     */
    private void loadInstalledApps() {
        try {
            List<ApplicationInfo> apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
            
            for (ApplicationInfo appInfo : apps) {
                try {
                    String appName = packageManager.getApplicationLabel(appInfo).toString();
                    Drawable icon = packageManager.getApplicationIcon(appInfo);
                    boolean isSystemApp = (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
                    long installTime = packageManager.getPackageInfo(appInfo.packageName, 0).firstInstallTime;
                    
                    // 检查应用是否有启动Activity
                    Intent launchIntent = packageManager.getLaunchIntentForPackage(appInfo.packageName);
                    if (launchIntent != null) {
                        AppInfo app = new AppInfo(appInfo.packageName, appName, icon, isSystemApp, installTime);
                        installedApps.put(appInfo.packageName, app);
                        Log.d(TAG, "Added app: " + appName + " (" + appInfo.packageName + ")");
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Failed to load app info for: " + appInfo.packageName, e);
                }
            }
            
            Log.i(TAG, "Loaded " + installedApps.size() + " apps");
        } catch (Exception e) {
            Log.e(TAG, "Failed to load installed apps", e);
        }
    }

    public List<AppInfo> getAllApps() {
        return new ArrayList<>(installedApps.values());
    }

    public List<AppInfo> getThirdPartyApps() {
        List<AppInfo> thirdPartyApps = new ArrayList<>();
        for (AppInfo app : installedApps.values()) {
            if (!app.isSystemApp) {
                thirdPartyApps.add(app);
            }
        }
        return thirdPartyApps;
    }

    /**
     * 获取系统应用列表
     */
    public List<AppInfo> getSystemApps() {
        List<AppInfo> systemApps = new ArrayList<>();
        for (AppInfo app : installedApps.values()) {
            if (app.isSystemApp) {
                systemApps.add(app);
            }
        }
        return systemApps;
    }

    public boolean launchApp(String packageName) {
        try {
            Intent launchIntent = packageManager.getLaunchIntentForPackage(packageName);
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(launchIntent);
                Log.i(TAG, "Launched app: " + packageName);
                return true;
            } else {
                Log.w(TAG, "No launch intent found for: " + packageName);
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch app: " + packageName, e);
            return false;
        }
    }

    /**
     * 搜索应用
     */
    public List<AppInfo> searchApps(String query) {
        List<AppInfo> results = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        
        for (AppInfo app : installedApps.values()) {
            if (app.appName.toLowerCase().contains(lowerQuery) || 
                app.packageName.toLowerCase().contains(lowerQuery)) {
                results.add(app);
            }
        }
        
        return results;
    }

    public int getAppCount() {
        return installedApps.size();
    }

    public int getThirdPartyAppCount() {
        int count = 0;
        for (AppInfo app : installedApps.values()) {
            if (!app.isSystemApp) {
                count++;
            }
        }
        return count;
    }

    public int getSystemAppCount() {
        int count = 0;
        for (AppInfo app : installedApps.values()) {
            if (app.isSystemApp) {
                count++;
            }
        }
        return count;
    }
}
