package com.zoomlee.zoo.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.ui.view.ZMTextView;
import com.zoomlee.zoo.utils.DeveloperUtil;

/**
 * Created by
 *
 * @author Michael Oknov <misha.oknov@alterplay.com>
 * @date 2/24/15
 */
public class RadioListAdapter extends BaseAdapter {

    private int selectedItem = -1;
    private String[] items;
    private LayoutInflater inflater;

    public RadioListAdapter(Context context, String[] items, int selectedItem) {
        this.items = items;
        this.selectedItem = selectedItem;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return items.length;
    }

    @Override
    public Object getItem(int position) {
        return items[position];
    }

    @Override
    public long getItemId(int position) {
        return items[position].hashCode();
    }

    public int getSelectedPosition() {
        return selectedItem;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_alert_time, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.tvTitle = (ZMTextView) convertView;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        String item = items[position];
        viewHolder.tvTitle.setText(item);
        viewHolder.tvTitle.setChecked(position == selectedItem);

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position == selectedItem)
                    return;
                if (selectedItem != -1) {
                    AdapterView adapterView = (AdapterView) parent;
                    int currentViewPosition = selectedItem - adapterView.getFirstVisiblePosition();
                    if (currentViewPosition >= 0 && currentViewPosition <= adapterView.getLastVisiblePosition()) {
                        adapterView.getChildAt(currentViewPosition).setBackgroundColor(0x00000000);
                        ViewHolder viewHolder = (ViewHolder) adapterView.getChildAt(currentViewPosition).getTag();
                        viewHolder.tvTitle.unsetAllDrawables();
                    }
                }
                selectedItem = position;
                v.setBackgroundResource(R.color.item_checked);
                ViewHolder viewHolder = (ViewHolder) v.getTag();
                viewHolder.tvTitle.setRightDrawable(R.drawable.check_green);
                DeveloperUtil.michaelLog();
                DeveloperUtil.michaelLog(selectedItem);
            }
        });

        return convertView;
    }

    private static class ViewHolder {

        ZMTextView tvTitle;
    }
}
