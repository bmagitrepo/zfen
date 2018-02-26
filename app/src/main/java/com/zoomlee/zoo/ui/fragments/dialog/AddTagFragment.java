package com.zoomlee.zoo.ui.fragments.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.ui.view.BottomSheet;

/**
 * Created by
 *
 * @author Michael Oknov <misha.oknov@alterplay.com>
 * @since 4/28/15
 */
public class AddTagFragment extends DialogFragment implements View.OnClickListener {
    private final static String TAG = "AddFieldFragment";

    private OnSelectListener listener;

    public static AddTagFragment newInstance(OnSelectListener listener) {
        AddTagFragment fragment = new AddTagFragment();
        fragment.setListener(listener);
        return fragment;
    }

    public AddTagFragment() {
        // Required empty public constructor
    }

    private void setListener(OnSelectListener listener) {
        this.listener = listener;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View mView = View.inflate(getActivity(), R.layout.fragment_dialog_add_tag, null);


        mView.findViewById(R.id.addToNew).setOnClickListener(this);
        mView.findViewById(R.id.addToExist).setOnClickListener(this);

        return new BottomSheet(mView);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addToExist:
                listener.onAddTagToExist();
                break;
            case R.id.addToNew:
                listener.onAddTagToNew();
                break;
        }
    }

    public interface OnSelectListener {
        void onAddTagToNew();

        void onAddTagToExist();
    }
}

