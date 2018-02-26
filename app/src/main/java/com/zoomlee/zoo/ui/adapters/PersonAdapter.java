package com.zoomlee.zoo.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.net.model.Person;
import com.zoomlee.zoo.utils.UiUtil;

import java.util.List;

/**
 * Created by
 *
 * @author Michael Oknov <misha.oknov@alterplay.com>
 * @date 2/16/15
 */
public class PersonAdapter extends ArrayAdapter<Person> implements NoDivider {

    public PersonAdapter(Context context, List<Person> items) {
        super(context, R.layout.item_person, items);
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.item_person, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.ivIcon = (ImageView) convertView.findViewById(R.id.iconIv);
            viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.nameTv);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Person item = getItem(position);
        viewHolder.tvTitle.setText(item.getName().toUpperCase());

        UiUtil.loadPersonIcon(item, viewHolder.ivIcon, false);

        return convertView;
    }

    @Override
    public boolean noDivider(int position) {
        return false;
    }

    @Override
    public boolean noFooterLine() {
        return true;
    }

    private static class ViewHolder {

        ImageView ivIcon;
        TextView tvTitle;
    }
}