package com.example.mygesture.gesture;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mygesture.R;
import com.example.mygesture.AppSelectionActivity;
import com.example.mygesture.common.GestureConst;
import com.example.mygesture.gesture.adapter.GestureOption;
import com.example.mygesture.gesture.adapter.GestureOptionAdapter;
import com.example.mygesture.manager.AppManager;

import java.util.ArrayList;
import java.util.List;

public class ChooseGestureActivity extends AppCompatActivity {

  private static final String TAG = "ChooseGestureActivity";

  private TextView tabOperation, tabThirdParty, tabSystem;
  private View tabIndicator;
  private ListView listView;
  private GestureOptionAdapter adapter;
  private ImageView btnBack;

  private int currentTabIndex = 0;
  private List<GestureOption> operationOptions;
  private List<GestureOption> thirdPartyOptions;
  private List<GestureOption> systemOptions;

  private String edgeType;
  private String gestureType;
  private AppManager appManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EdgeToEdge.enable(this);
    setContentView(R.layout.activity_choose_gesture);
    ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
      Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
      v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
      return insets;
    });

    Intent intent = getIntent();
    if (intent != null) {
      edgeType = intent.getStringExtra(GestureConst.KEY_EDGE_TYPE);
      gestureType = intent.getStringExtra(GestureConst.KEY_GESTURE_TYPE);
    }

    appManager = AppManager.getInstance(this);

    initViews();
    initData();
    setupListeners();

    // 默认显示操作选项
    showOperationOptions();
//    moveIndicator(0);
  }

  private void initViews() {
    btnBack = findViewById(R.id.btn_back);
    tabOperation = findViewById(R.id.tab_operation);
    tabThirdParty = findViewById(R.id.tab_third_party);
    tabSystem = findViewById(R.id.tab_system);
    listView = findViewById(R.id.list_view);
  }

  private void initData() {
    operationOptions = new ArrayList<>();
    operationOptions.add(new GestureOption(R.drawable.ic_back, getString(R.string.back), "back"));
    operationOptions.add(new GestureOption(R.drawable.ic_home, getString(R.string.home), "home"));
    operationOptions.add(
        new GestureOption(R.drawable.ic_recent_tasks, getString(R.string.recent_tasks), "recent_tasks"));
    operationOptions.add(
        new GestureOption(R.drawable.ic_notification, getString(R.string.expand_notification), "expand_notification"));
    operationOptions.add(
        new GestureOption(R.drawable.ic_settings, getString(R.string.quick_settings), "quick_settings"));
    operationOptions.add(
        new GestureOption(R.drawable.ic_split_screen, getString(R.string.split_screen), "split_screen"));
    operationOptions.add(new GestureOption(R.drawable.ic_lock_screen, getString(R.string.lock_screen), "lock_screen"));
    operationOptions.add(
        new GestureOption(R.drawable.ic_close_gesture, getString(R.string.close_gesture_temp), "close_gesture"));

    thirdPartyOptions = new ArrayList<>();
    loadThirdPartyApps();

    systemOptions = new ArrayList<>();
    loadSystemApps();
  }

  private void loadThirdPartyApps() {
    try {
//      thirdPartyOptions.add(new GestureOption(R.drawable.ic_add, "选择更多应用...", "select_more_apps"));

      // 获取前20个第三方应用作为快捷选项
      List<AppManager.AppInfo> apps = appManager.getThirdPartyApps();
      int count = 0;
      for (AppManager.AppInfo app : apps) {
//        if (count >= 15) break;

        thirdPartyOptions.add(new GestureOption(
            R.drawable.ic_home, // 使用默认图标，实际项目中可以加载真实应用图标
            app.appName,
            app.packageName
        ));
        count++;
      }

      Log.d(TAG, "Loaded " + thirdPartyOptions.size() + " third party app options");
    } catch (Exception e) {
      Log.e(TAG, "Failed to load third party apps", e);
    }
  }

  private void loadSystemApps() {
    try {
      // 添加"选择更多系统应用"选项
//      systemOptions.add(new GestureOption(R.drawable.ic_add, "选择更多系统应用...", "select_more_system_apps"));

      // 获取前20个系统应用作为快捷选项
      List<AppManager.AppInfo> apps = appManager.getSystemApps();
      int count = 0;
      for (AppManager.AppInfo app : apps) {
//        if (count >= 15) break; // 限制显示数量

        systemOptions.add(new GestureOption(
            R.drawable.ic_settings,
            app.appName,
            app.packageName
        ));
        count++;
        Log.d(TAG, "loadSystemApps: " + app.appName + " " + app.packageName);
      }

      Log.d(TAG, "Loaded " + systemOptions.size() + " system app options");
    } catch (Exception e) {
      Log.e(TAG, "Failed to load system apps", e);
    }
  }

  private void setupListeners() {
    btnBack.setOnClickListener(v -> finish());

    tabOperation.setOnClickListener(v -> switchTab(0));
    tabThirdParty.setOnClickListener(v -> switchTab(1));
    tabSystem.setOnClickListener(v -> switchTab(2));

    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        GestureOption option = (GestureOption) adapter.getItem(position);

        // 检查是否是"选择更多应用"选项
        if ("select_more_apps".equals(option.getAction()) || "select_more_system_apps".equals(option.getAction())) {
          // 启动应用选择Activity
          Intent appSelectionIntent = new Intent(ChooseGestureActivity.this, AppSelectionActivity.class);
          startActivityForResult(appSelectionIntent, GestureConst.REQUEST_CODE_SELECT_APP);
          return;
        }

        Toast.makeText(ChooseGestureActivity.this,
            "选择了: " + option.getTitle(), Toast.LENGTH_SHORT).show();

        // 确定操作类型
        String operatorType;
        switch (currentTabIndex) {
          case 1:
          case 2:
            operatorType = GestureConst.OPERATOR_TYPE_APP;
            break;
          case 3:
            operatorType = GestureConst.OPERATOR_TYPE_SHORTCUT;
            break;
          default:
            operatorType = GestureConst.OPERATOR_TYPE_OPERATION;
            break;
        }

        // 创建返回的Intent，携带选择的数据
        Intent resultIntent = new Intent();
        resultIntent.putExtra(GestureConst.KEY_SELECTED_ACTION, option.getTitle());
        resultIntent.putExtra(GestureConst.KEY_SELECTED_ACTION_TYPE, operatorType);
        resultIntent.putExtra(GestureConst.KEY_EDGE_TYPE, edgeType);
        resultIntent.putExtra(GestureConst.KEY_GESTURE_TYPE, gestureType);

        // 如果是应用，添加包名信息
        Log.d(TAG, "Selected app package: " + option.getAction());
        if (operatorType.equals(GestureConst.OPERATOR_TYPE_APP)) {
          resultIntent.putExtra(GestureConst.KEY_APP_PACKAGE, option.getAction());
          resultIntent.putExtra(GestureConst.KEY_APP_NAME, option.getTitle());
        }

        // 设置结果并关闭Activity
        setResult(RESULT_OK, resultIntent);
        finish();
      }
    });
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == GestureConst.REQUEST_CODE_SELECT_APP && resultCode == RESULT_OK && data != null) {
      // 从应用选择Activity返回的结果
      String appName = data.getStringExtra("selected_app_name");
      String appPackage = data.getStringExtra("selected_app_package");
      String actionType = data.getStringExtra(GestureConst.KEY_SELECTED_ACTION_TYPE);

      // 创建返回的Intent
      Intent resultIntent = new Intent();
      resultIntent.putExtra(GestureConst.KEY_SELECTED_ACTION, appName);
      resultIntent.putExtra(GestureConst.KEY_SELECTED_ACTION_TYPE, actionType);
      resultIntent.putExtra(GestureConst.KEY_EDGE_TYPE, edgeType);
      resultIntent.putExtra(GestureConst.KEY_GESTURE_TYPE, gestureType);
      resultIntent.putExtra(GestureConst.KEY_APP_PACKAGE, appPackage);
      resultIntent.putExtra(GestureConst.KEY_APP_NAME, appName);

      // 设置结果并关闭Activity
      setResult(RESULT_OK, resultIntent);
      finish();
    }
  }

  private void switchTab(int tabIndex) {
    if (currentTabIndex == tabIndex) {
      return;
    }

    // 更新标签页样式
    resetTabStyles();
    currentTabIndex = tabIndex;

    switch (tabIndex) {
      case 0:
        tabOperation.setTextColor(ContextCompat.getColor(this, R.color.primary_green));
        showOperationOptions();
        break;
      case 1:
        tabThirdParty.setTextColor(ContextCompat.getColor(this, R.color.primary_green));
        showThirdPartyOptions();
        break;
      case 2:
        tabSystem.setTextColor(ContextCompat.getColor(this, R.color.primary_green));
        showSystemOptions();
        break;
    }

    // 移动指示器
    //    moveIndicator(tabIndex);
  }

  private void resetTabStyles() {
    int defaultColor = ContextCompat.getColor(this, R.color.text_secondary);
    tabOperation.setTextColor(defaultColor);
    tabThirdParty.setTextColor(defaultColor);
    tabSystem.setTextColor(defaultColor);
  }

  private void showOperationOptions() {
    if (adapter == null) {
      adapter = new GestureOptionAdapter(this, operationOptions);
      listView.setAdapter(adapter);
    } else {
      adapter.updateData(operationOptions);
    }
  }

  private void showThirdPartyOptions() {
    if (adapter == null) {
      adapter = new GestureOptionAdapter(this, thirdPartyOptions);
      listView.setAdapter(adapter);
    } else {
      adapter.updateData(thirdPartyOptions);
    }
  }

  private void showSystemOptions() {
    if (adapter == null) {
      adapter = new GestureOptionAdapter(this, systemOptions);
      listView.setAdapter(adapter);
    } else {
      adapter.updateData(systemOptions);
    }
  }
}
