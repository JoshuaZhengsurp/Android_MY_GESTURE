package com.example.mygesture.gesture.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mygesture.R;

import java.util.List;

/**
 * 创建手势选项列表适配器
 */
public class GestureOptionAdapter extends BaseAdapter {
    private Context context;
    private List<GestureOption> options;
    private LayoutInflater inflater;

    public GestureOptionAdapter(Context context, List<GestureOption> options) {
        this.context = context;
        this.options = options;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return options.size();
    }

    @Override
    public Object getItem(int position) {
        return options.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_gesture_option, parent, false);
            holder = new ViewHolder();
            holder.icon = convertView.findViewById(R.id.icon);
            holder.title = convertView.findViewById(R.id.title);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        GestureOption option = options.get(position);
        holder.icon.setImageResource(option.getIconResId());
        holder.title.setText(option.getTitle());

        return convertView;
    }

    /**
     * 更新数据并通知适配器数据已更改
     * @param newOptions 新的手势选项列表
     */
    public void updateData(List<GestureOption> newOptions) {
        this.options = newOptions;
        notifyDataSetChanged();
    }

    private static class ViewHolder {
        ImageView icon;
        TextView title;
    }
}
