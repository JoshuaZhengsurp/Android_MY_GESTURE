package com.example.mygesture.gesture.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.example.mygesture.R;
import com.example.mygesture.common.GestureUtils;
import com.example.mygesture.common.OnGestureDetectionListener;
import com.example.mygesture.gesture.helper.GestureManager;

/**
 * 手势检测区域视图
 * 显示左右边缘的蒙层，表示手势触发区域
 */
public class GestureDetectionAreaView extends LinearLayout {

  private final String TAG = "GestureDetectionAreaView";

  private View mGestureDetectionAreaView = null;
  private int leftTriggerHeight = 60;
  private int leftTriggerWidth = 25;
  private int leftTriggerPosition = 25;
  private boolean leftEdgeEnabled = false;
  private GestureManager mGestureManager;
  private Context mActivity;
  private String mEdgeType;
  private OnGestureDetectionListener mListener;

  public GestureDetectionAreaView(Context context, String edgeType, OnGestureDetectionListener listener) {
    this(context, edgeType, listener, null);
  }

  public GestureDetectionAreaView(Context context, String edgeType, OnGestureDetectionListener listener,
      @Nullable AttributeSet attrs) {
    super(context, attrs);
    init(context, edgeType, listener);
  }

  private void init(Context context, String edgeType, OnGestureDetectionListener listener) {
    Log.d(TAG, "init");
    mGestureDetectionAreaView = LayoutInflater.from(context).inflate(R.layout.view_left_gesture_detection_area, this);
    mActivity = context;
    leftEdgeEnabled = true;
    mEdgeType = edgeType;
    mListener = listener;
    mGestureManager = new GestureManager(getContext(), (String gestureType) -> {
      if (mListener != null) {
        Log.d(TAG, "init: " + mEdgeType + " " + gestureType);
        mListener.onGestureDetected(mEdgeType, gestureType);
      }
      return false;
    });

    post(new Runnable() {
      @Override
      public void run() {
        Log.d(TAG,
            "View dimensions: " + mGestureDetectionAreaView.getWidth() + " x " + mGestureDetectionAreaView.getHeight());
        updateGestureDetectionArea(leftTriggerPosition);
      }
    });
  }

  private void updateGestureDetectionArea(int position) {
    if (mGestureDetectionAreaView != null) {
      int displayHeight = GestureUtils.getInstance().getDeviceHeight(getContext());

      int topMargin = displayHeight * position / 100;
      int areaHeight = displayHeight * leftTriggerHeight / 100;
      Log.d(TAG, "Calculated dimensions: topMargin=" + topMargin + ", areaHeight=" + areaHeight);

      ViewGroup.LayoutParams layoutParams = mGestureDetectionAreaView.getLayoutParams();

      if (layoutParams instanceof MarginLayoutParams) {
        MarginLayoutParams marginParams = (MarginLayoutParams) layoutParams;

        marginParams.height = areaHeight;
        marginParams.topMargin = topMargin;
        marginParams.leftMargin = 0;

        mGestureDetectionAreaView.setLayoutParams(marginParams);

        Log.d(TAG, "Layout params updated successfully");
      }
      // 请求重新布局
      requestLayout();
      invalidate();
    }
  }


  @Override
  public boolean dispatchTouchEvent(MotionEvent event) {
    super.dispatchTouchEvent(event);
    if (leftEdgeEnabled) {
      return mGestureManager.onTouchEvent(event);
    }
    return false;
  }
}
