package com.example.mygesture.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mygesture.R;
import com.example.mygesture.manager.AppManager;

import java.util.List;

/**
 * 创建应用列表适配器，用于在ListView中显示应用列表
 */
public class AppListAdapter extends BaseAdapter {
    private Context context;
    private List<AppManager.AppInfo> appList;
    private LayoutInflater inflater;

    public AppListAdapter(Context context, List<AppManager.AppInfo> appList) {
        this.context = context;
        this.appList = appList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return appList != null ? appList.size() : 0;
    }

    @Override
    public AppManager.AppInfo getItem(int position) {
        return appList != null && position < appList.size() ? appList.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_app_list, parent, false);
            holder = new ViewHolder();
            holder.iconImageView = convertView.findViewById(R.id.imageView_app_icon);
            holder.nameTextView = convertView.findViewById(R.id.textView_app_name);
            holder.packageTextView = convertView.findViewById(R.id.textView_package_name);
            holder.typeTextView = convertView.findViewById(R.id.textView_app_type);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        AppManager.AppInfo appInfo = getItem(position);
        if (appInfo != null) {
            // 设置应用图标
            holder.iconImageView.setImageDrawable(appInfo.icon);
            
            // 设置应用名称
            holder.nameTextView.setText(appInfo.appName);
            
            // 设置包名
            holder.packageTextView.setText(appInfo.packageName);
            
            // 设置应用类型
            holder.typeTextView.setText(appInfo.isSystemApp ? "系统应用" : "第三方应用");
            
            // 设置类型文字颜色
            int textColor = appInfo.isSystemApp ? 
                context.getResources().getColor(android.R.color.holo_orange_dark) :
                context.getResources().getColor(android.R.color.holo_green_dark);
            holder.typeTextView.setTextColor(textColor);
        }

        return convertView;
    }

    /**
     * 更新数据
     */
    public void updateData(List<AppManager.AppInfo> newAppList) {
        this.appList = newAppList;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder模式优化性能
     */
    private static class ViewHolder {
        ImageView iconImageView;
        TextView nameTextView;
        TextView packageTextView;
        TextView typeTextView;
    }
}
