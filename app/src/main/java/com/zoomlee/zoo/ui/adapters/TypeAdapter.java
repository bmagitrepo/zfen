package com.zoomlee.zoo.ui.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.net.model.Category;
import com.zoomlee.zoo.net.model.DocumentsType;
import com.zoomlee.zoo.net.model.NamedItem;
import com.zoomlee.zoo.ui.view.ZMTextView;
import com.zoomlee.zoo.utils.DeveloperUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * Created by
 *
 * @author Michael Oknov <misha.oknov@alterplay.com>
 * @since 2/19/15
 */
public class TypeAdapter extends BaseAdapter implements Filterable {

    public static final int TYPE = 0;
    public static final int CATEGORY = 1;
    private final int selectedTypeId;

    private List<NamedItem> itemsOriginal = new ArrayList<>();
    private TreeSet<Integer> sectionHeaderOriginal = new TreeSet<>();
    private List<NamedItem> itemsFiltered = new ArrayList<>();
    private TreeSet<Integer> sectionHeader = new TreeSet<>();

    private LayoutInflater mInflater;
    private ItemFilter mFilter = new ItemFilter();

    public TypeAdapter(Context context, List<Category> categoryList, int selectedTypeId) {
        this.selectedTypeId = selectedTypeId;
        mInflater = LayoutInflater.from(context);

        for (Category category : categoryList) {
            itemsOriginal.add(category);
            sectionHeaderOriginal.add(itemsOriginal.size() - 2);
            List<DocumentsType> typesList = category.getDocumentsTypeList();
            for (DocumentsType type : typesList) {
                itemsOriginal.add(type);
            }
        }
        sectionHeaderOriginal.remove(0);
        itemsOriginal.remove(0);

        sectionHeader.addAll(sectionHeaderOriginal);
        itemsFiltered.addAll(itemsOriginal);
    }

    @Override
    public int getItemViewType(int position) {
        return sectionHeader.contains(position) ? CATEGORY : TYPE;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getCount() {
        return itemsFiltered.size();
    }

    @Override
    public NamedItem getItem(int position) {
        return itemsFiltered.get(position);
    }

    public DocumentsType getType(int position) {
        return (DocumentsType) itemsFiltered.get(position);
    }

    public Category getCategory(int position) {
        int categoryPosition = sectionHeader.lower(position);
        if (categoryPosition == -1)
            return Category.OTHER_CATEGORY;
        return (Category) itemsFiltered.get(categoryPosition);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View view, ViewGroup parent) {
        int rowType = getItemViewType(position);

        ViewHolder holder;
        if (view == null) {
            switch (rowType) {
                case CATEGORY:
                    view = mInflater.inflate(R.layout.item_categories_header, parent, false);
                    break;

                default:
                case TYPE:
                    view = mInflater.inflate(R.layout.item_checkable, parent, false);
                    break;
            }

            holder = new ViewHolder();
            holder.textView = (ZMTextView) view.findViewById(R.id.nameTv);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        NamedItem curItem = itemsFiltered.get(position);

        holder.textView.setText(curItem.getName());
        holder.textView.setChecked(curItem.getRemoteId() == selectedTypeId);

        return view;
    }

    public Filter getFilter() {
        return mFilter;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            DeveloperUtil.michaelLog();
            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            final List<NamedItem> list = itemsOriginal;

            int count = list.size();
            sectionHeader.clear();

            if (TextUtils.isEmpty(filterString)) {
                sectionHeader.addAll(sectionHeaderOriginal);
                results.values = list;
                results.count = count;
                return results;
            }
            final ArrayList<NamedItem> nlist = new ArrayList<>(count);

            NamedItem filterableItem;
            for (int i = 0; i < count; i++) {
                filterableItem = list.get(i);
                if (filterableItem instanceof DocumentsType && filterableItem.getName().toLowerCase().contains(filterString)) {
                    nlist.add(filterableItem);
                } else if (filterableItem instanceof Category) {
                    if (nlist.size() != 0 && nlist.get(nlist.size() - 1) instanceof Category)
                        nlist.remove(nlist.size() - 1);

                    sectionHeader.add(nlist.size());
                    nlist.add(filterableItem);
                }
            }
            if (nlist.size() != 0 && nlist.get(nlist.size() - 1) instanceof Category)
                nlist.remove(nlist.size() - 1);

            results.values = nlist;
            results.count = nlist.size();

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            itemsFiltered = (ArrayList<NamedItem>) results.values;
            notifyDataSetChanged();
        }
    }

    private static class ViewHolder {

        ZMTextView textView;
    }
}