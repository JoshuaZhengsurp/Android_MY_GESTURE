package com.example.mygesture.gesture.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mygesture.R;
import com.example.mygesture.common.GestureConst;
import com.example.mygesture.common.OnEdgeGestureConfigListener;
import com.example.mygesture.gesture.helper.FloatingBallManager;

public class FloatingBallGestureConfigView extends BaseGestureConfigView {

  // 悬浮球特有的视图组件
  private TextView tvClick, tvDoubleClick, tvLongPress;
  private FloatingBallManager mFloatingBallManager;
  private Switch mSwitchFloatingBall;
  private Button mBtnFloatingBallPermission;

  public FloatingBallGestureConfigView(Context context) {
    super(context);
    init();
  }

  public FloatingBallGestureConfigView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public FloatingBallGestureConfigView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  @Override
  protected int getLayoutResource() {
    return R.layout.view_float_ball_gesture_config;
  }

  private void init() {
    initViews();
    setupListeners();
  }

  private void initViews() {
    initFloatingBallViews();

    // 手势功能视图
    tvClick = findViewById(R.id.tv_swipe_horizontal);
    tvDoubleClick = findViewById(R.id.tv_swipe_up);
    tvLongPress = findViewById(R.id.tv_swipe_down);

    gestureConfigContainer = findViewById(R.id.gesture_config_container);
  }

  protected void setupListeners() {
    tvClick.setOnClickListener(v -> {
      if (mListener != null) {
        mListener.onGestureActionClicked(mEdgeType, GestureConst.FLOATING_BALL_GESTURE_CLICK, tvClick);
      }
    });

    tvDoubleClick.setOnClickListener(v -> {
      if (mListener != null) {
        mListener.onGestureActionClicked(mEdgeType, GestureConst.FLOATING_BALL_GESTURE_DOUBLE_CLICK, tvDoubleClick);
      }
    });

    tvLongPress.setOnClickListener(v -> {
      if (mListener != null) {
        mListener.onGestureActionClicked(mEdgeType, GestureConst.FLOATING_BALL_GESTURE_LONG_PRESS, tvLongPress);
      }
    });
  }

  @Override
  protected TextView getGestureActionView(String gestureType) {
    switch (gestureType) {
      case GestureConst.FLOATING_BALL_GESTURE_CLICK:
        return tvClick;
      case GestureConst.FLOATING_BALL_GESTURE_DOUBLE_CLICK:
        return tvDoubleClick;
      case GestureConst.FLOATING_BALL_GESTURE_LONG_PRESS:
        return tvLongPress;
      default:
        return null;
    }
  }

  private void initFloatingBallViews() {
    mFloatingBallManager = FloatingBallManager.getInstance(mContext);
    mSwitchFloatingBall = findViewById(R.id.switch_floating_ball);
    mBtnFloatingBallPermission = findViewById(R.id.btn_floating_ball_permission);

    mSwitchFloatingBall.setChecked(mFloatingBallManager.isFloatingBallShowing());

    mSwitchFloatingBall.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
      if (isChecked) {
        if (mFloatingBallManager.canDrawOverlays()) {
          mFloatingBallManager.showFloatingBall();
          gestureConfigContainer.setVisibility(VISIBLE);
        } else {
          // 没有权限，取消选中状态
          mSwitchFloatingBall.setChecked(false);
          Toast.makeText(mContext, "请先授予悬浮窗权限", Toast.LENGTH_SHORT).show();
          mFloatingBallManager.requestOverlayPermission();
          gestureConfigContainer.setVisibility(GONE);
        }
      } else {
        mFloatingBallManager.hideFloatingBall();
        gestureConfigContainer.setVisibility(GONE);
      }
    });

    mBtnFloatingBallPermission.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mFloatingBallManager.requestOverlayPermission();
      }
    });
    mFloatingBallManager.setGestureConfigView(this);
  }

  public void checkFloatingState() {
    if (mSwitchFloatingBall != null && mFloatingBallManager != null) {
      if (!mFloatingBallManager.canDrawOverlays() && mSwitchFloatingBall.isChecked()) {
        mSwitchFloatingBall.setChecked(false);
        mFloatingBallManager.setFloatingBallShowing(false);
      }
    }
  }

  public void setupFloatingBallConfigs(String edgeType, OnEdgeGestureConfigListener listener) {
    setEdgeType(edgeType);
    setGestureConfigListener(listener);
  }
}
