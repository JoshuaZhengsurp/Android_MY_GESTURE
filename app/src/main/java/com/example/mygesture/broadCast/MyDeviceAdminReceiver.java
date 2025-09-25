package com.example.mygesture.broadCast;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

// 创建设备管理员接收器类，用于锁屏功能
public class MyDeviceAdminReceiver extends DeviceAdminReceiver {
    private static final String TAG = "MyDeviceAdminReceiver";

    @Override
    public void onEnabled(Context context, Intent intent) {
        super.onEnabled(context, intent);
        Log.i(TAG, "设备管理员已启用");
        Toast.makeText(context, "设备管理员已启用", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        super.onDisabled(context, intent);
        Log.i(TAG, "设备管理员已禁用");
        Toast.makeText(context, "设备管理员已禁用", Toast.LENGTH_SHORT).show();
    }
}
