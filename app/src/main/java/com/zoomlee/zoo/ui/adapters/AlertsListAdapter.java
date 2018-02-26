package com.zoomlee.zoo.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.incitations.Incitation;
import com.zoomlee.zoo.incitations.IncitationsController;
import com.zoomlee.zoo.net.model.Category;
import com.zoomlee.zoo.utils.TimeUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static com.zoomlee.zoo.dao.AlertsDaoHelper.Alert;

/**
 * Created by
 *
 * @author Evgen Marinin <ievgen.marinin@alterplay.com>
 * @date 03.02.15.
 */
public class AlertsListAdapter extends ArrayAdapter<Alert> implements IncitationsAdapter.Incitated, NoDivider {

    private static final int MIN_POSITION = 1;
    private static final int MAX_POSITION = 4;
    private boolean isShowIncitations = false;
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("d MMM yyyy", Locale.US);

    static {
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private final IncitationsController incitationsController = new IncitationsController();
    private IncitationsAdapter.Incitated incitated;

    public AlertsListAdapter(Context context, List<Alert> items) {
        super(context, R.layout.categories_doc_list_item, items);

        updateIncitations();
    }

    private void updateIncitations() {
        incitated = incitationsController.createIncitated(getCount(), IncitationsController.Screen.NOTIFICATIONS, MIN_POSITION, MAX_POSITION);
    }

    public void showIncitations(boolean isShow) {
        isShowIncitations = isShow;
    }

    @Override
    public void notifyDataSetChanged() {
        updateIncitations();

        super.notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder viewHolder;

        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            view = inflater.inflate(R.layout.alert_list_item, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.ivIcon = (ImageView) view.findViewById(R.id.categoryIconIv);
            viewHolder.tvTitle = (TextView) view.findViewById(R.id.alertNameTv);
            viewHolder.tvDescription = (TextView) view.findViewById(R.id.alertValueTv);
            viewHolder.tvAlerts = (TextView) view.findViewById(R.id.alertsTv);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        Alert item = getItem(position);
        viewHolder.ivIcon.setImageResource(Category.getIconRes(item.getCategoryRemoteId()));
        viewHolder.tvTitle.setText(item.getDocumentName() + " — " + item.getFieldName());
        viewHolder.tvDescription.setText(getFormattedDate(item.getFieldValue()));
        viewHolder.tvAlerts.setText(getFormattedDate(item.getNotifyOn() * 1000L));

        int color = getContext().getResources().getColor(item.isViewed()
                ? android.R.color.transparent
                : R.color.item_checked);
        view.setBackgroundColor(color);

        return view;
    }

    public String getFormattedDate(String value) {
        try {
            Calendar calendar = TimeUtil.getCalendarForServerTime(Long.parseLong(value));
            return simpleDateFormat.format(calendar.getTime());
        } catch (NumberFormatException e) {
            return value;
        }

    }

    public String getFormattedDate(long alertsTimeInMilis) {
        Calendar alertTime = Calendar.getInstance();
        alertTime.setTimeInMillis(alertsTimeInMilis);
        Calendar now = Calendar.getInstance();

        int yearsDiff = now.get(Calendar.YEAR) - alertTime.get(Calendar.YEAR);
        if (yearsDiff > 1)
            return yearsDiff + " years ago";
        else if (yearsDiff == 1)
            return yearsDiff + " year ago";

        int monthDiff = now.get(Calendar.MONTH) - alertTime.get(Calendar.MONTH);
        if (monthDiff > 1)
            return monthDiff + " months ago";
        else if (monthDiff == 1)
            return monthDiff + " month ago";

        int weekDiff = now.get(Calendar.WEEK_OF_MONTH) - alertTime.get(Calendar.WEEK_OF_MONTH);
        if (weekDiff > 1)
            return weekDiff + " weeks ago";
        else if (weekDiff == 1)
            return weekDiff + " week ago";

        if (now.get(Calendar.DATE) - alertTime.get(Calendar.DATE) == 1)
            return "yesterday";
        else
            return "today";
    }

    @Override
    public boolean noDivider(int position) {
        int incitationPosition = getIncitationPosition();
        return position - 1 == incitationPosition || position == incitationPosition;
    }

    @Override
    public boolean noFooterLine() {
        return getIncitationPosition() == getCount();
    }

    @Override
    public int getIncitationPosition() {
        return isShowIncitations ? incitated.getIncitationPosition() : AdapterView.INVALID_POSITION;
    }

    @Override
    public Incitation getIncitation() {
        return incitated.getIncitation();
    }

    private static class ViewHolder {
        ImageView ivIcon;
        TextView tvTitle;
        TextView tvDescription;
        TextView tvAlerts;
    }
}