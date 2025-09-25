// 创建手势选项数据模型类
package com.example.mygesture.gesture.adapter;

public class GestureOption {
    private int iconResId;
    private String title;
    private String action;

    public GestureOption(int iconResId, String title, String action) {
        this.iconResId = iconResId;
        this.title = title;
        this.action = action;
    }

    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
