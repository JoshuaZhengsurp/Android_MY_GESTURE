package com.example.mygesture.gesture.view;

import java.util.HashMap;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.mygesture.R;
import com.example.mygesture.common.GestureConst;
import com.example.mygesture.common.GestureUtils;
import com.example.mygesture.common.OnEdgeGestureConfigListener;
import com.example.mygesture.manager.AppManager;

/**
 * 手势配置视图的抽象基类
 * 提供通用的手势配置功能和界面管理
 */
public abstract class BaseGestureConfigView extends LinearLayout {
    
    protected final String TAG = getClass().getSimpleName();
    
    // 共同属性
    public Context mContext;
    public LinearLayout gestureConfigContainer;
    public OnEdgeGestureConfigListener mListener;
    public String mEdgeType;
    
    // 手势动作映射 - 存储手势类型到动作的映射
    public final HashMap<String, Pair<String, String>> gestureActionMap = new HashMap<>();
    
    public BaseGestureConfigView(Context context) {
        super(context);
        init(context);
    }
    
    public BaseGestureConfigView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    
    public BaseGestureConfigView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
        LayoutInflater.from(context).inflate(getLayoutResource(), this, true);
    }

    protected abstract int getLayoutResource();

    public void updateGestureAction(String gestureType, String operatorType, String selectedAction) {
        gestureActionMap.put(gestureType, new Pair<>(operatorType, selectedAction));
    }

    public void handleGestureActionDetected(String gestureType) {
        Pair<String, String> gestureActionPair = gestureActionMap.get(gestureType);
        if (gestureActionPair != null) {
            runGestureAction(gestureActionPair.first, gestureActionPair.second);
        }
    }

    protected boolean runGestureAction(String operatorType, String selectedAction) {
        Log.d(TAG, "runGestureAction: " + mEdgeType + " " + operatorType + " " + selectedAction);
        
        if (operatorType.equals(GestureConst.OPERATOR_TYPE_OPERATION)) {
            if (selectedAction.equals(mContext.getString(R.string.quick_settings))) {
                return GestureUtils.getInstance().openSettings(mContext);
            } else if (selectedAction.equals(mContext.getString(R.string.expand_notification))) {
                return GestureUtils.getInstance().expandNotificationPanel(mContext);
            } else if (selectedAction.equals(mContext.getString(R.string.lock_screen))) {
                return GestureUtils.getInstance().lockScreen(mContext);
            } else if (selectedAction.equals(mContext.getString(R.string.back))) {
                return GestureUtils.getInstance().goBack(mContext);
            } else if (selectedAction.equals(mContext.getString(R.string.split_screen))) {
              return GestureUtils.getInstance().toggleBluetooth(mContext);
            }
        } else if (operatorType.equals(GestureConst.OPERATOR_TYPE_APP)) {
            return AppManager.getInstance(mContext).launchApp(selectedAction);
        }
        return false;
    }
    
    /**
     * 根据手势类型获取对应的动作视图 - 子类需要实现
     */
    protected abstract TextView getGestureActionView(String gestureType);
    
    /**
     * 获取手势动作配置映射
     */
    public HashMap<String, Pair<String, String>> getGestureActionMap() {
        return gestureActionMap;
    }
    
    /**
     * 设置手势动作配置映射（用于恢复配置）
     */
    public void setGestureActionMap(HashMap<String, Pair<String, String>> actionMap) {
        if (actionMap != null) {
            gestureActionMap.clear();
            gestureActionMap.putAll(actionMap);
            
            // 更新UI显示
            for (String gestureType : actionMap.keySet()) {
                Pair<String, String> action = actionMap.get(gestureType);
                if (action != null) {
                    TextView actionView = getGestureActionView(gestureType);
                    if (actionView != null) {
                        actionView.setText(action.second);
                    }
                }
            }
        }
    }

    public void setGestureConfigListener(OnEdgeGestureConfigListener listener) {
        this.mListener = listener;
    }

    public void setEdgeType(String edgeType) {
        this.mEdgeType = edgeType;
    }
}
