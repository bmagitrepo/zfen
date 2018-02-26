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
import com.zoomlee.zoo.net.model.Country;
import com.zoomlee.zoo.net.model.NamedItem;
import com.zoomlee.zoo.ui.view.ZMTextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by
 *
 * @author Michael Oknov <misha.oknov@alterplay.com>
 * @since 3/31/15
 */
public class CountryAdapter extends BaseAdapter implements Filterable {

    public static final int SEPARATOR = 0;
    public static final int COUNTRY = 1;

    private List<Country> itemsOriginal = new ArrayList<>();
    private List<Country> itemsFiltered = new ArrayList<>();

    private LayoutInflater mInflater;
    private int selectedCountryId;
    private int separatorPositionOriginal;
    private int separatorPositionFiltered;
    private ItemFilter mFilter = new ItemFilter();

    public CountryAdapter(Context context, List<Country> countryList, int selectedCountryId) {
        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        this.selectedCountryId = selectedCountryId;
        int i = 0;
        for (; i < countryList.size(); i++) {
            Country country = countryList.get(i);
            if (country.getPrioritize() == 0) {
                break;
            }
        }
        separatorPositionOriginal = i;
        separatorPositionFiltered = separatorPositionOriginal;
        itemsOriginal.addAll(countryList);
        itemsOriginal.add(separatorPositionFiltered, new Country());
        itemsFiltered.addAll(itemsOriginal);
    }

    @Override
    public int getItemViewType(int position) {
        return position == separatorPositionFiltered ? SEPARATOR : COUNTRY;
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
    public Country getItem(int position) {
        return itemsFiltered.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View view, ViewGroup parent) {
        int rowType = getItemViewType(position);

        switch (rowType) {
            case SEPARATOR:
                if (view == null) {
                    view = mInflater.inflate(R.layout.divider_country_bold, parent, false);
                }
                return view;

            case COUNTRY:
                if (view == null) {
                    view = mInflater.inflate(R.layout.item_checkable, parent, false);
                }
                break;
        }

        if (!(view instanceof ZMTextView)) {
            // somehow it sometimes get not right view from recycle cache
            view = mInflater.inflate(R.layout.item_checkable, parent, false);
        }
        ZMTextView textCountry = (ZMTextView) view;

        Country curItem = itemsFiltered.get(position);

        textCountry.setText(curItem.getName());
        textCountry.setChecked(curItem.getRemoteId() == selectedCountryId);

        return view;
    }

    public Filter getFilter() {
        return mFilter;
    }

    private class ItemFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            final List<Country> list = itemsOriginal;

            int count = list.size();

            if (TextUtils.isEmpty(filterString)) {
                results.values = list;
                results.count = count;
                separatorPositionFiltered = separatorPositionOriginal;
                return results;
            }
            final ArrayList<NamedItem> nlist = new ArrayList<>(count);

            Country filterableItem;
            for (int i = 0; i < count; i++) {
                filterableItem = list.get(i);
                if (filterableItem.getId() == -1) {
                    if (nlist.size() == 0) {
                        separatorPositionFiltered = -1;
                        continue;
                    }
                    separatorPositionFiltered = nlist.size();
                    nlist.add(filterableItem);
                    continue;
                }
                if (filterableItem.getName().toLowerCase().contains(constraint))
                    nlist.add(filterableItem);
            }

            results.values = nlist;
            results.count = nlist.size();

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            itemsFiltered = (ArrayList<Country>) results.values;
            notifyDataSetChanged();
        }
    }
}