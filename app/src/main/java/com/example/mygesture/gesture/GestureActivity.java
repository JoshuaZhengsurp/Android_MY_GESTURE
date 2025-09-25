package com.example.mygesture.gesture;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mygesture.R;
import com.example.mygesture.broadCast.MyDeviceAdminReceiver;
import com.example.mygesture.common.GestureConst;
import com.example.mygesture.common.OnEdgeGestureConfigListener;
import com.example.mygesture.common.OnGestureDetectionListener;
import com.example.mygesture.gesture.view.BaseGestureConfigView;
import com.example.mygesture.gesture.view.FloatingBallGestureConfigView;
import com.example.mygesture.manager.AppManager;
import com.example.mygesture.gesture.helper.FloatingBallManager;
import com.example.mygesture.gesture.view.EdgeGestureConfigView;
import com.example.mygesture.gesture.view.GestureDetectionAreaView;

public class GestureActivity extends AppCompatActivity {

  private static final String TAG = "GestureActivity";

  private EdgeGestureConfigView mLeftEdgeConfig;
  private EdgeGestureConfigView mRightEdgeConfig;
  private GestureDetectionAreaView mLeftGestureDetectionAreaView;
  private GestureDetectionAreaView mRightGestureDetectionAreaView;
  private FloatingBallGestureConfigView mFloatingBallGestureConfigView;

  private MyDeviceAdminReceiver myDeviceAdminReceiver = new MyDeviceAdminReceiver();
  private DevicePolicyManager mDevicePolicyManager;

  private OnEdgeGestureConfigListener mOnEdgeGestureConfigListener;
  private OnGestureDetectionListener mOnGestureDetectionListener;
  private TextView mCurrentActionView;
  private String mCurrentEdgeType;
  private String mCurrentGestureType;
  private AppManager mAppManager;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_gesture);

    // 设置标题栏
    if (getSupportActionBar() != null) {
      getSupportActionBar().setTitle("手势设置");
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    initListener();
    initViews();

    if (mAppManager == null) {
      mAppManager = AppManager.getInstance(this);
    }

    ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.gesture_main), new OnApplyWindowInsetsListener() {
      @Override
      public @org.jspecify.annotations.NonNull WindowInsetsCompat onApplyWindowInsets(
          @org.jspecify.annotations.NonNull View v, @org.jspecify.annotations.NonNull WindowInsetsCompat insets) {
        Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
        v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
        return insets;
      }
    });
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (mFloatingBallGestureConfigView != null) {
      mFloatingBallGestureConfigView.checkFloatingState();
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    //    unregisterReceiver(myDeviceAdminReceiver);
  }

  private void initListener() {
    mOnEdgeGestureConfigListener = new OnEdgeGestureConfigListener() {
      @Override
      public void onEdgeToggled(boolean isEnabled) {
        // 处理边缘手势开关状态变化
        EdgeGestureConfigView source = isEnabled ?
            (mLeftEdgeConfig.isEdgeEnabled() ? mLeftEdgeConfig : mRightEdgeConfig) :
            (!mLeftEdgeConfig.isEdgeEnabled() ? mLeftEdgeConfig : mRightEdgeConfig);
      }
      @Override
      public void onGestureActionClicked(String edgeType, String gestureType, TextView actionView) {
        // 处理手势动作选择
        setGestureAction(edgeType, gestureType, actionView);
      }
      @Override
      public void onTriggerSettingChanged(String edgeType, String settingType, int value) {
        Log.d(TAG, "onTriggerSettingChanged: " + edgeType + ", " + settingType + ", " + value);
      }
    };

    mOnGestureDetectionListener = (String edgeType, String gestureType) -> {
        handleGestureDetectionResult(edgeType, gestureType);
    };
  }

  private void initViews() {
    mLeftEdgeConfig = findViewById(R.id.left_edge_config);
    mLeftGestureDetectionAreaView = new GestureDetectionAreaView(this, GestureConst.EDGE_TYPE_LEFT, mOnGestureDetectionListener);
    mRightEdgeConfig = findViewById(R.id.right_edge_config);
    mRightGestureDetectionAreaView = new GestureDetectionAreaView(this, GestureConst.EDGE_TYPE_RIGHT, mOnGestureDetectionListener);
    mFloatingBallGestureConfigView = findViewById(R.id.floating_ball_config);
    setupEdgeConfigAndDetectionArea(GestureConst.EDGE_TYPE_LEFT, mLeftEdgeConfig, mLeftGestureDetectionAreaView);
    setupEdgeConfigAndDetectionArea(GestureConst.EDGE_TYPE_RIGHT, mRightEdgeConfig, mRightGestureDetectionAreaView);
    mFloatingBallGestureConfigView.setupFloatingBallConfigs(GestureConst.EDGE_TYPE_FLOATING_BALL ,mOnEdgeGestureConfigListener);
  }

  private void setupEdgeConfigAndDetectionArea (String edgeType, EdgeGestureConfigView configView, GestureDetectionAreaView detectionAreaView) {
    // 创建FrameLayout布局参数，对应XML中的设置
    FrameLayout.LayoutParams detectioAreaLayoutParams = new FrameLayout.LayoutParams(
        FrameLayout.LayoutParams.WRAP_CONTENT,
        FrameLayout.LayoutParams.WRAP_CONTENT
    );
    detectioAreaLayoutParams.gravity = edgeType == GestureConst.EDGE_TYPE_LEFT ? Gravity.LEFT :  Gravity.RIGHT;

    // 将视图添加到主容器中
    FrameLayout mainContainer = findViewById(R.id.gesture_main);
    mainContainer.addView(detectionAreaView, detectioAreaLayoutParams);
    configView.setupEdgeConfigs(edgeType, mOnEdgeGestureConfigListener);
  }

  private void setGestureAction(String edgeType, String gestureType, TextView targetView) {
    Log.d(TAG, "showActionDialog: " + edgeType + ", " + gestureType);
    mCurrentActionView = targetView;
    mCurrentEdgeType = edgeType;
    mCurrentGestureType = gestureType;

    Intent intent = new Intent(this, ChooseGestureActivity.class);
    intent.putExtra(GestureConst.KEY_GESTURE_TYPE, gestureType)
        .putExtra(GestureConst.KEY_EDGE_TYPE, edgeType);
    startActivityForResult(intent, GestureConst.REQUEST_CODE_CHOOSE_GESTURE);
  }

  private void handleGestureDetectionResult(String edgeType, String gestureType) {
    // TODO: 处理手势检测结果
    Log.d(TAG, "Gesture detected: " + edgeType + ", " + gestureType);
    BaseGestureConfigView edgeConfigView = getEdgeGestureConfigView(edgeType);
    if (edgeConfigView != null) {
      edgeConfigView.handleGestureActionDetected(gestureType);
    }
  }

  private BaseGestureConfigView getEdgeGestureConfigView (String edgeType) {
    switch (edgeType) {
      case GestureConst.EDGE_TYPE_LEFT:
        return mLeftEdgeConfig;
      case GestureConst.EDGE_TYPE_RIGHT:
        return mRightEdgeConfig;
      case GestureConst.EDGE_TYPE_FLOATING_BALL:
        return mFloatingBallGestureConfigView;
      default:
        return null;
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == GestureConst.REQUEST_CODE_CHOOSE_GESTURE && resultCode == RESULT_OK && data != null) {
      String selectedAction = data.getStringExtra(GestureConst.KEY_SELECTED_ACTION);
      String operatorType = data.getStringExtra(GestureConst.KEY_SELECTED_ACTION_TYPE);

      if (selectedAction != null && operatorType != null) {
        if (mCurrentEdgeType != null) {
          BaseGestureConfigView edgeConfigView = getEdgeGestureConfigView (mCurrentEdgeType);
          if (edgeConfigView != null) {
            Log.d(TAG, "updateGestureAction: " + mCurrentGestureType + ", " + operatorType + ", " + selectedAction + ", " + (operatorType == GestureConst.OPERATOR_TYPE_APP));
            if (operatorType.equals(GestureConst.OPERATOR_TYPE_APP)) {
              edgeConfigView.updateGestureAction(mCurrentGestureType, operatorType, data.getStringExtra(GestureConst.KEY_APP_PACKAGE));
            } else {
              edgeConfigView.updateGestureAction(mCurrentGestureType, operatorType, selectedAction);
            }
          }
        }

        mCurrentActionView.setText(selectedAction);

        Log.d(TAG, "Selected action: " + selectedAction + ", operator type: " + operatorType);

        mCurrentActionView = null;
      }
    }
  }

  public void registerReceiver() {
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction("android.app.action.DEVICE_ADMIN_ENABLED");
    intentFilter.addAction("android.app.action.DEVICE_ADMIN_DISABLED");
    registerReceiver(myDeviceAdminReceiver, intentFilter);

    mDevicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
    if (mDevicePolicyManager.isAdminActive(new ComponentName(this, MyDeviceAdminReceiver.class))) {
      mDevicePolicyManager.lockNow();
      finish();
    } else {
      Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
      intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, new ComponentName(this, MyDeviceAdminReceiver.class));
      startActivity(intent);
    }
  }
}
