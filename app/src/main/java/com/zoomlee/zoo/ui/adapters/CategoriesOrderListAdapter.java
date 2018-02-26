package com.zoomlee.zoo.ui.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nhaarman.listviewanimations.ArrayAdapter;
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
public class CategoriesOrderListAdapter extends ArrayAdapter<CategoriesDocAlerts> {

    final int INVALID_ID = -1;
    private Context context;
    private int titleColor;
    private int descriptionColor;
    private int disabledColor;
    private int bgColor;

    public CategoriesOrderListAdapter(Context context, List<CategoriesDocAlerts> items) {
        super(items);
        this.context = context;
        Resources res = context.getResources();
        this.titleColor = res.getColor(R.color.text_gray);
        this.descriptionColor = res.getColor(R.color.text_empty);
        this.disabledColor = res.getColor(R.color.disabled);
        this.bgColor = res.getColor(R.color.bg_content);
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
            convertView.findViewById(R.id.alertsTv).setVisibility(View.INVISIBLE);
            convertView.findViewById(R.id.dragAndDropIv).setVisibility(View.VISIBLE);
            convertView.setTag(viewHolder);
            convertView.setBackgroundColor(bgColor);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        CategoriesDocAlerts item = getItem(position);
        viewHolder.tvTitle.setText(item.getCategoryName().toUpperCase());
        viewHolder.ivIcon.setImageResource(Category.getIconRes(item.getCategoryRemoteId()));
        if (item.getDocsTypesNames().isEmpty())
            displayNonActiveState(item, viewHolder);
        else
            displayNormalState(item, viewHolder);


        return convertView;
    }

    private void displayNormalState(CategoriesDocAlerts item, ViewHolder viewHolder) {
        viewHolder.tvDescription.setText(formatDocsTypes(item.getDocsTypesNames()));

        viewHolder.ivIcon.clearColorFilter();
        viewHolder.tvTitle.setTextColor(titleColor);
        viewHolder.tvDescription.setTextColor(descriptionColor);
    }

    private void displayNonActiveState(CategoriesDocAlerts item, ViewHolder viewHolder) {
        viewHolder.tvDescription.setText(R.string.no_documents);

        viewHolder.ivIcon.setColorFilter(disabledColor, PorterDuff.Mode.SRC_IN);
        viewHolder.tvTitle.setTextColor(disabledColor);
        viewHolder.tvDescription.setTextColor(disabledColor);
    }

    @Override
    public long getItemId(int position) {
        if (position < 0 || position >= getCount()) {
            return INVALID_ID;
        }
        CategoriesDocAlerts item = getItem(position);
        return item.getCategoryRemoteId();
    }

    @Override
    public boolean hasStableIds() {
        return true;
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
    }
}