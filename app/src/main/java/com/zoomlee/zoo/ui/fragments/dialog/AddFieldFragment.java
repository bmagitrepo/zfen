package com.zoomlee.zoo.ui.fragments.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.net.model.Field;
import com.zoomlee.zoo.ui.view.BottomSheet;


public class AddFieldFragment extends DialogFragment implements View.OnClickListener {
    private final static String TAG = "AddFieldFragment";

    private OnChoosedFieldType listener;

    public static AddFieldFragment newInstance(OnChoosedFieldType listener) {
        AddFieldFragment fragment = new AddFieldFragment();
        fragment.setListener(listener);
        return fragment;
    }

    public AddFieldFragment() {
        // Required empty public constructor
    }

    private void setListener(OnChoosedFieldType listener) {
        this.listener = listener;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View mView = View.inflate(getActivity(), R.layout.fragment_dialog_add_field, null);


        mView.findViewById(R.id.addTextField).setOnClickListener(this);
        mView.findViewById(R.id.addDateField).setOnClickListener(this);

        return new BottomSheet(mView);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addDateField:
                listener.onChoosed(Field.DATE_TYPE);
                break;
            case R.id.addTextField:
                listener.onChoosed(Field.TEXT_TYPE);
                break;
        }
    }

    public interface OnChoosedFieldType {
        void onChoosed(int type);
    }
}
