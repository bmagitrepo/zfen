package com.zoomlee.zoo.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.nhaarman.listviewanimations.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.net.model.Category;

import java.util.List;
import java.util.Set;

import static com.zoomlee.zoo.dao.CategoriesDocAlertsDaoHelper.CategoriesDocAlerts;

/**
 * Created by
 *
 * @author Evgen Marinin <ievgen.marinin@alterplay.com>
 * @date 03.02.15.
 */
public class CategoriesListAdapter extends ArrayAdapter<CategoriesDocAlerts> {

    private Context context;

    public CategoriesListAdapter(Context context, List<CategoriesDocAlerts> items) {
        super(items);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.categories_doc_list_item, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.ivIcon = (ImageView) convertView.findViewById(R.id.categoryIconIv);
            viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.categoryNameTv);
            viewHolder.tvDescription = (TextView) convertView.findViewById(R.id.docsTypesTv);
            viewHolder.tvAlerts = (TextView) convertView.findViewById(R.id.alertsTv);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        CategoriesDocAlerts item = getItem(position);
        viewHolder.ivIcon.setImageResource(Category.getIconRes(item.getCategoryRemoteId()));
        viewHolder.tvTitle.setText(item.getCategoryName().toUpperCase());
        viewHolder.tvDescription.setText(formatDocsTypes(item.getDocsTypesNames()));

        int alertsCount = item.getAlertsCount();
        if (alertsCount == 0)
            viewHolder.tvAlerts.setVisibility(View.INVISIBLE);
        else {
            if (alertsCount < 10)
                viewHolder.tvAlerts.setBackgroundResource(R.drawable.updates_background);
            else
                viewHolder.tvAlerts.setBackgroundResource(R.drawable.updates_background_long);

            viewHolder.tvAlerts.setVisibility(View.VISIBLE);
            viewHolder.tvAlerts.setText(String.valueOf(alertsCount));
        }

        return convertView;
    }

    private String formatDocsTypes(Set<String> docTypes) {
        StringBuilder sb = new StringBuilder();
        for (String docType : docTypes) {
            sb.append(docType);
            sb.append(", ");
        }
        sb.delete(sb.length() - 2, sb.length());

        return sb.toString();
    }

    private static class ViewHolder {
        ImageView ivIcon;
        TextView tvTitle;
        TextView tvDescription;
        TextView tvAlerts;
    }
}