package com.zoomlee.zoo.ui.adapters;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.incitations.Incitation;
import com.zoomlee.zoo.incitations.IncitationsController;
import com.zoomlee.zoo.net.model.Tax;
import com.zoomlee.zoo.utils.CircleTransform;
import com.zoomlee.zoo.utils.DeveloperUtil;
import com.zoomlee.zoo.utils.PicassoUtil;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Created by
 *
 * @author Michael Oknov <misha.oknov@alterplay.com>
 * @since 2/23/15
 */
public class TaxesAdapter extends BaseAdapter implements NoDivider, IncitationsAdapter.Incitated {

    private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM d", Locale.US);
    private final static SimpleDateFormat DAY_FORMAT = new SimpleDateFormat("d", Locale.US);

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private static final int MIN_POSITION = 2;
    private static final int MAX_POSITION = 4;

    private final List<Tax> origin = new ArrayList<>();
    private final List<ViewItem> showed = new ArrayList<>();
    private final LayoutInflater inflater;
    private final Context context;
    private List<String> flagsName;
    private final ItemListener itemListener;
    private final IncitationsController incitationsController = new IncitationsController();
    private IncitationsAdapter.Incitated incitated;
    private boolean showIncitations = true;

    public TaxesAdapter(Context context, ItemListener listener) {
        itemListener = listener;
        inflater = LayoutInflater.from(context);
        this.context = context;

        AssetManager assets = context.getAssets();
        try {
            flagsName = Arrays.asList(assets.list("flags"));
        } catch (IOException ioe) {
            flagsName = new ArrayList<>();
            ioe.printStackTrace();
        }

        updateIncitations();
    }

    private void updateIncitations() {
        incitated = incitationsController.createIncitated(getCount(), IncitationsController.Screen.TAX_TRACKING, MIN_POSITION, MAX_POSITION);
    }

    @Override
    public void notifyDataSetChanged() {
        updateIncitations();

        super.notifyDataSetChanged();
    }

    public void setData(List<Tax> taxes) {
        if (taxes == null || taxes.isEmpty()) {
            origin.clear();
            showed.clear();
            notifyDataSetChanged();
            return;
        }

        origin.clear();
        origin.addAll(taxes);
        Collections.sort(origin, comparator);
        updateYearHeaders();
        notifyDataSetChanged();
    }

    private void updateYearHeaders() {
        if (origin.isEmpty()) {
            showed.clear();
            notifyDataSetChanged();
            return;
        }

        showed.clear();
        int i = 0;
        Tax tax = origin.get(i);
        while (i < origin.size()) {
            int currentYear = tax.getDisplayArrivalYear();
            // form current year trips
            DaysAbroadCounter abroadCounter = new DaysAbroadCounter();
            List<ViewItem> yearTrips = new ArrayList<>();
            while (tax.getDisplayArrivalYear() == currentYear) {
                abroadCounter.addDays(tax.getDaysCount());
                yearTrips.add(new ViewItem(tax));
                i++;
                if (i < origin.size()) {
                    tax = origin.get(i);
                } else {
                    break;
                }
            }
            showed.add(new ViewItem(true, currentYear, abroadCounter.getDaysAbroad()));
            showed.addAll(yearTrips);
        }
    }

    public int getDaysAbroad() {
        int count = 0;
        for (ViewItem item : showed) {
            if (item.isYear) {
                count += item.yearDuration;
            }
        }
        return count;
    }

    public View onCreateView(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            View v = inflater.inflate(R.layout.item_tax, parent, false);
            VHItem holder = new VHItem(v);
            v.setTag(holder);
            return v;
        } else if (viewType == TYPE_HEADER) {
            View v = inflater.inflate(R.layout.item_tax_header, parent, false);
            VHHeader holder = new VHHeader(v);
            v.setTag(holder);
            return v;
        }

        throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");
    }

    public void onBindViewHolder(Object holder, final int position) {
        if (holder instanceof VHItem) {
            VHItem itemHolder = (VHItem) holder;
            itemHolder.bind(getItem(position));
        } else if (holder instanceof VHHeader) {
            VHHeader headerHolder = (VHHeader) holder;
            headerHolder.name.setText(String.valueOf(getItem(position).yearName));
            headerHolder.duration.setText(String.format("%d days total", getItem(position).yearDuration));
        }
    }

    private String getImagePath(String countryCode) {
        countryCode = countryCode.toLowerCase();
        return String.format("file:///android_asset/flags/%s", countryCode);
    }

    @Override
    public int getCount() {
        return showed.size();
    }

    @Override
    public ViewItem getItem(int position) {
        return showed.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position).isYear) {
            return TYPE_HEADER;
        }

        return TYPE_ITEM;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        int viewType = getItemViewType(position);
        if (view == null) {
            view = onCreateView(parent, viewType);
        }
        onBindViewHolder(view.getTag(), position);

        return view;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public boolean noDivider(int position) {
        int incitationPosition = getIncitationPosition();
        if (position - 1 == incitationPosition || position == incitationPosition) {
            return true;
        } else {
            // keep in mind we are incitated, and here call don't mind that
            if (incitationPosition != AdapterView.INVALID_POSITION && position > incitationPosition) {
                position--; // we need it for correct section determining
            }
        }
        return position > 0 && getItem(position - 1).isYear;
    }

    @Override
    public boolean noFooterLine() {
        // we don't need divider if incitation is the last one
        return getIncitationPosition() == getCount();
    }

    public void clear() {
        origin.clear();
        notifyDataSetChanged();
    }

    public List<Tax> getData() {
        return origin;
    }

    public void remove(Tax tax) {
        DeveloperUtil.michaelLog("yyy");
        if (origin.remove(tax)) {
            DeveloperUtil.michaelLog();
            DeveloperUtil.michaelLog(origin);
            updateYearHeaders();
            notifyDataSetChanged();
        }
    }

    public void update(Tax tax) {
        DeveloperUtil.michaelLog("xxx");
        origin.remove(tax);
        origin.add(tax);
        Collections.sort(origin, comparator);
        DeveloperUtil.michaelLog();
        DeveloperUtil.michaelLog(origin);
        updateYearHeaders();
        notifyDataSetChanged();
    }

    public void setShowIncitations(boolean showIncitations) {
        this.showIncitations = showIncitations;
    }

    @Override
    public int getIncitationPosition() {
        return countNotYears() > 1 && showIncitations ? incitated.getIncitationPosition() : AdapterView.INVALID_POSITION;
    }

    private int countNotYears() {
        int count = 0;
        for (ViewItem item : showed) {
            if (!item.isYear) {
                count++;
            }
        }
        return count;
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public Incitation getIncitation() {
        return incitated.getIncitation();
    }

    class VHItem implements View.OnClickListener {

        public TextView days;
        public ImageView icon;
        public TextView name;
        public TextView range;
        public TextView duration;
        private final View itemView;
        private Tax tax;

        public VHItem(View v) {
            days = (TextView) v.findViewById(R.id.text_days);
            icon = (ImageView) v.findViewById(R.id.countryIcon);
            name = (TextView) v.findViewById(R.id.countryName);
            range = (TextView) v.findViewById(R.id.tripRange);
            duration = (TextView) v.findViewById(R.id.tripDuration);
            itemView = v;

            v.setOnClickListener(this);
        }

        public void bind(ViewItem item) {
            this.tax = item.tax;

            Resources resources = days.getResources();
            days.setText(resources.getQuantityString(R.plurals.days, item.tax.getDaysCount()));

            String countryCode = tax.getCountryCode() + ".png";
            if (!flagsName.contains(countryCode))
                countryCode = "unknown_country.png";
            PicassoUtil.getInstance()
                    .load(getImagePath(countryCode))
                    .resize(66, 66)
                    .transform(new CircleTransform())
                    .into(icon);
            name.setText(tax.getCountryName());

            String rangeDisplay;

            Calendar arrival = Calendar.getInstance();
            arrival.setTimeInMillis(tax.getDisplayArrivalMS());

            Calendar departure = Calendar.getInstance();
            departure.setTimeInMillis(tax.getDisplayDepartureMS());
            if (tax.isAutoCheckIn()) {
                // auto check in
                itemView.setBackgroundResource(R.color.item_checked);
                rangeDisplay = String.format("%s - …", DATE_FORMAT.format(arrival.getTime()));
            } else {
                itemView.setBackgroundResource(R.drawable.selectable_item_background);

                if (arrival.get(Calendar.DAY_OF_YEAR) == departure.get(Calendar.DAY_OF_YEAR)) {
                    // same day must be showed as a day not as range
                    rangeDisplay = DATE_FORMAT.format(arrival.getTime());
                } else {
                    String rangeEnd;
                    if (arrival.get(Calendar.MONTH) == departure.get(Calendar.MONTH)) {
                        // same month mustn't be double printed
                        rangeEnd = DAY_FORMAT.format(departure.getTime());
                    } else {
                        rangeEnd = DATE_FORMAT.format(departure.getTime());
                    }
                    rangeDisplay = String.format("%s - %s", DATE_FORMAT.format(arrival.getTime()), rangeEnd);
                }
            }
            range.setText(rangeDisplay);
            duration.setText(String.valueOf(item.tax.getDaysCount()));
        }

        @Override
        public void onClick(View v) {
            itemListener.onItemClicked(tax);
        }
    }

    private class VHHeader {

        private TextView name;
        private TextView duration;

        public VHHeader(View itemView) {
            name = (TextView) itemView.findViewById(R.id.nameTv);
            duration = (TextView) itemView.findViewById(R.id.durationTv);
        }
    }

    private Comparator<Tax> comparator = new Comparator<Tax>() {
        @Override
        public int compare(Tax lhs, Tax rhs) {
            return (int) (lhs.getDisplayArrival() - rhs.getDisplayArrival());
        }
    };

    private class ViewItem {

        final Tax tax;
        final boolean isYear;
        final int yearName;
        final int yearDuration;

        ViewItem(boolean isYear, int yearName, int yearDuration) {
            this.isYear = isYear;
            this.yearName = yearName;
            this.yearDuration = yearDuration;
            tax = null;
        }

        ViewItem(Tax tax) {
            this.tax = tax;
            isYear = false;
            yearDuration = 0;
            yearName = 0;
        }
    }

    private static class DaysAbroadCounter {

        int abroad;

        /**
         * Adds days to count it in this year.
         *
         * @param daysCount days count
         * @return this range days count
         */
        public void addDays(int daysCount) {
            abroad += daysCount;
        }

        /**
         * @return counted number of days abroad
         */
        public int getDaysAbroad() {
            return abroad;
        }
    }

    public interface ItemListener {

        void onItemClicked(Tax tax);
    }
}