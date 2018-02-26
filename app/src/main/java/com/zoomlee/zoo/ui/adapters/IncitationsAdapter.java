package com.zoomlee.zoo.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.incitations.Incitation;
import com.zoomlee.zoo.incitations.IncitationsController;

/**
 * Author vbevans94.
 */
public class IncitationsAdapter<T extends BaseAdapter> extends BaseAdapter {

    private final T wrapped;
    private final Incitated incitated;
    private final LayoutInflater inflater;

    public IncitationsAdapter(T adapter, Incitated incitated) {
        wrapped = adapter;
        this.incitated = incitated;
        inflater = LayoutInflater.from(incitated.getContext());
    }

    public static <T extends BaseAdapter> IncitationsAdapter<T> wrap(T adapter, Incitated incitated) {
        return new IncitationsAdapter<>(adapter, incitated);
    }

    public T getWrapped() {
        return wrapped;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        wrapped.registerDataSetObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        wrapped.unregisterDataSetObserver(observer);
    }

    @Override
    public void notifyDataSetChanged() {
        wrapped.notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetInvalidated() {
        wrapped.notifyDataSetInvalidated();
    }

    @Override
    public boolean isEmpty() {
        return wrapped.isEmpty();
    }

    @Override
    public int getCount() {
        int incitationPosition = incitated.getIncitationPosition();
        return wrapped.getCount() + (incitationPosition == AdapterView.INVALID_POSITION ? 0 : 1);
    }

    @Override
    public Object getItem(int position) {
        int incitationPosition = incitated.getIncitationPosition();
        if (incitationPosition != AdapterView.INVALID_POSITION) {
            if (position == incitationPosition) {
                return null; // here is no item
            } else if (position > incitationPosition) {
                position--; // wrapped no nothing about incitation
            }
        }
        return wrapped.getItem(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return wrapped.getViewTypeCount() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        int incitationPosition = incitated.getIncitationPosition();
        if (incitationPosition != AdapterView.INVALID_POSITION) {
            if (position == incitationPosition) {
                return wrapped.getViewTypeCount();
            } else {
                if (position > incitationPosition) {
                    position--; // shift minding incitation
                }
            }
        }
        return wrapped.getItemViewType(position);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        int incitationPosition = incitated.getIncitationPosition();
        if (incitationPosition != AdapterView.INVALID_POSITION) {
            if (incitationPosition == position) {
                if (view == null) {
                    view = inflater.inflate(R.layout.view_incitation, parent, false);
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Activity activity = (Activity) incitated.getContext();
                            IncitationsController.processIncitation(activity, incitated.getIncitation());
                        }
                    });

                }
                TextView textIncitation = (TextView) view;
                textIncitation.setText(incitated.getIncitation().textResId);

                return view;
            } else if (position > incitationPosition) {
                position--;
            }

        }
        return wrapped.getView(position, view, parent);
    }

    public interface Incitated {

        /**
         * @return position where incitation should appear minding that count includes incitation or -1 if nowhere
         */
        int getIncitationPosition();

        /**
         * @return context that is used for wrapped adapter
         */
        Context getContext();

        /**
         * @return incitation to be shown
         */
        Incitation getIncitation();
    }
}
