package com.example.mygesture.gesture.helper;

import java.util.ArrayList;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;

import com.example.mygesture.common.GestureConst;

public class GestureManager {
  private final static String TAG = "GestureHelper";

  private final Handler mHandler = new Handler();

  private final static int MIN_SCROLL_GAP = 32;
  // 滑动检测延迟时间（毫秒）
  private final static int SCROLL_DETECTION_DELAY = 48;

  private ArrayList<String> mEventCollector = new ArrayList<String>();
  private GestureDetector mGestureDetector;
  private Context mContext;
  private boolean mIsDown = false;
  private boolean mIsScrolling = false;
  private Runnable mScrollHandler = null;
  private boolean mHasLongPress = false;
  private boolean mHasEventHandle = false;
  private Pair<Float, Float> mLongPressStartPoint = null;
  // 滑动向量
  private Pair<Float, Float> mScrollVector = new Pair<>(0f, 0f);

  private OnScrollListener mListener;

  public interface OnScrollListener {
    public boolean onScrollDetected(String gestureType);
  }

  public GestureManager(Context context, OnScrollListener listener) {
    if (listener == null) {
      mListener = (String gestureType) -> {
        return false;
      };
    } else {
      mListener = listener;
    }
    mContext = context;
    mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
      // 手指按下时触发; 只有消费了down事件，后续事件才可触发；
      @Override
      public boolean onDown(@NonNull MotionEvent e) {
        Log.i(TAG, "onDown");
        mIsDown = true;
        mIsScrolling = false;
        mHasEventHandle = false;
        return true;
      }

      // 长按
      @Override
      public void onLongPress(@NonNull MotionEvent e) {
        mHasLongPress = true;
        // 记录长按开始位置
        mLongPressStartPoint = new Pair<>(e.getX(), e.getY());
        Log.i(TAG,
            "onLongPress " + e + " start point: " + mLongPressStartPoint.first + "," + mLongPressStartPoint.second);
      }

      @Override
      public boolean onScroll(MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
        mIsScrolling = true;
        mScrollVector = new Pair<>(mScrollVector.first + distanceX, mScrollVector.second + distanceY);
        if (mScrollHandler == null) {
          mScrollHandler = generateScrollRunnable();
          mHandler.postDelayed(mScrollHandler, SCROLL_DETECTION_DELAY);
        }
        return true;
      }

      @Override
      public boolean onDoubleTap(@NonNull MotionEvent e) {
        Log.i(TAG, "onDoubleTap");
        return false;
      }
    });
  }

  public GestureDetector getGestureDetector() {
    return mGestureDetector;
  }

  public boolean onTouchEvent(MotionEvent event) {
    int action = event.getAction() & MotionEvent.ACTION_MASK;
    boolean handled = !mHasEventHandle && mGestureDetector.onTouchEvent(event);

    if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
      resetStatus();
      if (mHasEventHandle) {
        mGestureDetector.onTouchEvent(event);
      }
    } else if (mHasLongPress && action == MotionEvent.ACTION_MOVE) {
      onLongScroll(event);
    }

    Log.i(TAG,
        "onTouchEvent: " + (event.getAction() & MotionEvent.ACTION_MASK) + " " + handled + " " + !mHasEventHandle);
    return handled;
  }

  private void onLongScroll(MotionEvent event) {
    // 长按后滑动事件处理
    if (mLongPressStartPoint != null) {
      float currentX = event.getX();
      float currentY = event.getY();

      // 计算相对于长按起始点的滑动距离
      float deltaX = currentX - mLongPressStartPoint.first;
      float deltaY = currentY - mLongPressStartPoint.second;

      // 更新滑动向量（注意：这里使用的是位置差，而不是距离差）
      mScrollVector = new Pair<>(-deltaX, -deltaY); // 负号是为了保持与onScroll的距离方向一致

      // 启动滑动检测
      if (mScrollHandler == null && !mHasEventHandle) {
        mScrollHandler = generateScrollRunnable();
        mHandler.postDelayed(mScrollHandler, SCROLL_DETECTION_DELAY);
      }
    }
  }

  private Runnable generateScrollRunnable () {
    return () -> {
      String scrollType = scrollDetector();
      Log.i(TAG, "longPress scrollType = " + scrollType);
      if (!scrollType.isEmpty()) {
        // 长按滑动手势识别成功
        if (mListener.onScrollDetected(scrollType)) {
          mHasEventHandle = true;
        }
        Log.i(TAG, scrollType + "手势识别成功");
      }
      if (mScrollHandler != null) {
        mScrollHandler = null;
      }
    };
  }

  private String scrollDetector() {
    String scrollType = "";

    float x = mScrollVector.first;
    float y = mScrollVector.second;

    float cosVectorSqrt = y * y / (x * x + y * y);


    if (Math.abs(x) > MIN_SCROLL_GAP || Math.abs(y) > MIN_SCROLL_GAP) {
      if (cosVectorSqrt >= 0.75f /* cos60°^2 */) {
        if (y > 0) {
          return mHasLongPress ? GestureConst.GESTURE_TYPE_LONG_PRESS_SWIPE_UP : GestureConst.GESTURE_TYPE_SWIPE_UP;
        } else {
          return mHasLongPress ? GestureConst.GESTURE_TYPE_LONG_PRESS_SWIPE_DOWN : GestureConst.GESTURE_TYPE_SWIPE_DOWN;
        }
      } else if (0 <= cosVectorSqrt && cosVectorSqrt < 0.25f) {
        if (x > 0) {
          return mHasLongPress ? GestureConst.GESTURE_TYPE_LONG_PRESS_SWIPE_LEFT : GestureConst.GESTURE_TYPE_SWIPE_LEFT;
        } else {
          return mHasLongPress ? GestureConst.GESTURE_TYPE_LONG_PRESS_SWIPE_RIGHT
              : GestureConst.GESTURE_TYPE_SWIPE_RIGHT;
        }
      }
    }
    return scrollType;
  }

  private void resetStatus() {
    mScrollVector = new Pair<>(0f, 0f);
    mIsDown = false;
    mIsScrolling = false;
    mHasEventHandle = false;
    mHasLongPress = false;
    mLongPressStartPoint = null;
    // 取消未执行的定时任务
    if (mScrollHandler != null) {
      mHandler.removeCallbacks(mScrollHandler);
      mScrollHandler = null;
    }
  }
}
