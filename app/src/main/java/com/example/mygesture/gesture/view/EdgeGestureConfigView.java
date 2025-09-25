package com.example.mygesture.gesture.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.example.mygesture.R;
import com.example.mygesture.common.GestureConst;
import com.example.mygesture.common.GestureUtils;
import com.example.mygesture.common.OnEdgeGestureConfigListener;
import com.example.mygesture.manager.AppManager;

import java.util.HashMap;

public class EdgeGestureConfigView extends BaseGestureConfigView {
  private final static String TAG = "EdgeGestureConfigView";

  private Switch mSwitchEdge;
  private TextView tvEdgeTitle;

  // 手势功能配置
  private TextView tvSwipeHorizontal, tvSwipeUp, tvSwipeDown;
  private TextView tvLongPressSwipeRight, tvLongPressSwipeUp, tvLongPressSwipeDown;
  private SeekBar seekBarTriggerHeight, seekBarTriggerWidth, seekBarTriggerPosition;
  private TextView tvTriggerHeight, tvTriggerWidth, tvTriggerPosition;
  private LinearLayout gestureConfigContainer;  // 手势配置容器
  private final HashMap<String, Pair<String, String>> gestureActionMap = new HashMap<>();

  public EdgeGestureConfigView(Context context) {
    super(context);
    init();
  }

  public EdgeGestureConfigView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public EdgeGestureConfigView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  private void init() {
    initViews();
    setupListeners();
  }

  // todo: 还可以继续拆
  private void initViews() {
    // 开关相关
    mSwitchEdge = findViewById(R.id.switch_edge);
    tvEdgeTitle = findViewById(R.id.tv_edge_title);

    // 手势功能
    tvSwipeHorizontal = findViewById(R.id.tv_swipe_horizontal);
    tvSwipeUp = findViewById(R.id.tv_swipe_up);
    tvSwipeDown = findViewById(R.id.tv_swipe_down);
    tvLongPressSwipeRight = findViewById(R.id.tv_long_press_swipe_right);
    tvLongPressSwipeUp = findViewById(R.id.tv_long_press_swipe_up);
    tvLongPressSwipeDown = findViewById(R.id.tv_long_press_swipe_down);

    // 手势配置容器
    gestureConfigContainer = findViewById(R.id.gesture_config_container);

    // 触发区域设置
    seekBarTriggerHeight = findViewById(R.id.seekbar_trigger_height);
    seekBarTriggerWidth = findViewById(R.id.seekbar_trigger_width);
    seekBarTriggerPosition = findViewById(R.id.seekbar_trigger_position);
    tvTriggerHeight = findViewById(R.id.tv_trigger_height);
    tvTriggerWidth = findViewById(R.id.tv_trigger_width);
    tvTriggerPosition = findViewById(R.id.tv_trigger_position);

    // 设置初始值
    updateSeekBarLabels();
  }

  private void setupListeners() {
    // 边缘开关监听
    mSwitchEdge.setOnCheckedChangeListener((buttonView, isChecked) -> {
      gestureConfigContainer.setVisibility(isChecked ? VISIBLE : GONE);
      if (mListener != null) {
        mListener.onEdgeToggled(isChecked);
      }
    });

    tvSwipeHorizontal.setOnClickListener(v -> {
      if (mListener != null) {
        mListener.onGestureActionClicked(mEdgeType, GestureConst.GESTURE_TYPE_SWIPE_RIGHT, tvSwipeHorizontal);
      }
    });

    tvSwipeUp.setOnClickListener(v -> {
      if (mListener != null) {
        mListener.onGestureActionClicked(mEdgeType, GestureConst.GESTURE_TYPE_SWIPE_UP, tvSwipeUp);
      }
    });

    tvSwipeDown.setOnClickListener(v -> {
      if (mListener != null) {
        mListener.onGestureActionClicked(mEdgeType, GestureConst.GESTURE_TYPE_SWIPE_DOWN, tvSwipeDown);
      }
    });

    tvLongPressSwipeRight.setOnClickListener(v -> {
      if (mListener != null) {
        mListener.onGestureActionClicked(mEdgeType, GestureConst.GESTURE_TYPE_LONG_PRESS_SWIPE_RIGHT,
            tvLongPressSwipeRight);
      }
    });

    tvLongPressSwipeUp.setOnClickListener(v -> {
      if (mListener != null) {
        mListener.onGestureActionClicked(mEdgeType, GestureConst.GESTURE_TYPE_LONG_PRESS_SWIPE_UP, tvLongPressSwipeUp);
      }
    });

    tvLongPressSwipeDown.setOnClickListener(v -> {
      if (mListener != null) {
        mListener.onGestureActionClicked(mEdgeType, GestureConst.GESTURE_TYPE_LONG_PRESS_SWIPE_DOWN,
            tvLongPressSwipeDown);
      }
    });

    // SeekBar 监听
    setupSeekBarListener(seekBarTriggerHeight, tvTriggerHeight, GestureConst.TRIGGER_SETTING_HEIGHT);
    setupSeekBarListener(seekBarTriggerWidth, tvTriggerWidth, GestureConst.TRIGGER_SETTING_WIDTH);
    setupSeekBarListener(seekBarTriggerPosition, tvTriggerPosition, GestureConst.TRIGGER_SETTING_POSITION);
  }

  private void setupSeekBarListener(SeekBar seekBar, TextView textView, String label) {
    seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        textView.setText(label + ": " + progress + "%");

        if (mListener != null && fromUser) {
          mListener.onTriggerSettingChanged(mEdgeType, label, progress);
        }
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {}

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {}
    });
  }

  private void updateSeekBarLabels() {
    tvTriggerHeight.setText(GestureConst.TRIGGER_SETTING_HEIGHT + ": " + seekBarTriggerHeight.getProgress() + "%");
    tvTriggerWidth.setText(GestureConst.TRIGGER_SETTING_WIDTH + ": " + seekBarTriggerWidth.getProgress() + "%");
    tvTriggerPosition.setText(
        GestureConst.TRIGGER_SETTING_POSITION + ": " + seekBarTriggerPosition.getProgress() + "%");
  }

  public void setupEdgeConfigs(String edgeType, OnEdgeGestureConfigListener listener) {
    setEdgeType(edgeType);
    setGestureConfigListener(listener);
    tvEdgeTitle.setText(edgeType);

    if (edgeType.equals(GestureConst.EDGE_TYPE_LEFT)) {
      mSwitchEdge.setChecked(true);
      ((TextView)findViewById(R.id.swipe_horizontal_title)).setText(GestureConst.GESTURE_TYPE_SWIPE_LEFT);
      ((TextView)findViewById(R.id.long_press_swipe_right_title)).setText(GestureConst.GESTURE_TYPE_LONG_PRESS_SWIPE_RIGHT);
    } else {
      ((TextView)findViewById(R.id.swipe_horizontal_title)).setText(GestureConst.GESTURE_TYPE_SWIPE_RIGHT);
      ((TextView)findViewById(R.id.long_press_swipe_right_title)).setText(GestureConst.GESTURE_TYPE_LONG_PRESS_SWIPE_LEFT);
    }
  }

  @Override
  protected int getLayoutResource() {
    return R.layout.view_edge_gesture_config;
  }

  public boolean isEdgeEnabled() {
    return mSwitchEdge.isChecked();
  }

  @Override
  protected TextView getGestureActionView(String gestureType) {
    switch (gestureType) {
      case GestureConst.GESTURE_TYPE_SWIPE_RIGHT:
        return tvSwipeHorizontal;
      case GestureConst.GESTURE_TYPE_SWIPE_UP:
        return tvSwipeUp;
      case GestureConst.GESTURE_TYPE_SWIPE_DOWN:
        return tvSwipeDown;
      case GestureConst.GESTURE_TYPE_LONG_PRESS_SWIPE_RIGHT:
        return tvLongPressSwipeRight;
      case GestureConst.GESTURE_TYPE_LONG_PRESS_SWIPE_UP:
        return tvLongPressSwipeUp;
      case GestureConst.GESTURE_TYPE_LONG_PRESS_SWIPE_DOWN:
        return tvLongPressSwipeDown;
      default:
        return null;
    }
  }
}
