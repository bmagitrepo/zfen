package com.zoomlee.zoo.ui.fragments;


import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.cocosw.undobar.UndoBarController;
import com.zoomlee.zoo.R;
import com.zoomlee.zoo.net.model.Document;
import com.zoomlee.zoo.net.model.Field;
import com.zoomlee.zoo.ui.activity.CreateEditDocActivity;
import com.zoomlee.zoo.ui.fragments.dialog.AddFieldFragment;
import com.zoomlee.zoo.ui.view.field.CustomFieldTextView;
import com.zoomlee.zoo.ui.view.field.FieldEditView;
import com.zoomlee.zoo.ui.view.field.FieldViewFactory;
import com.zoomlee.zoo.utils.DeveloperUtil;

import java.util.ArrayList;
import java.util.List;


public class EditDocDataFieldsFragment extends FragmentWithBottomDialog implements View.OnClickListener, UndoBarController.UndoListener {

    private static final String DELETE_POSITION = "delete_position";
    private LinearLayout containerView;
    private ArrayList<FieldEditView> fieldViews = new ArrayList<>();
    private FieldEditView deleteView;

    private AddFieldFragment.OnChoosedFieldType onChoosedFieldType = new AddFieldFragment.OnChoosedFieldType() {
        @Override
        public void onChoosed(int type) {
            if (type == Field.TEXT_TYPE)
                addCustomTextField();
            else if (type == Field.DATE_TYPE)
                addCustomDateField();
            closeDialog();
        }
    };

    public static EditDocDataFieldsFragment newInstance() {
        EditDocDataFieldsFragment fragment = new EditDocDataFieldsFragment();
        return fragment;
    }

    public EditDocDataFieldsFragment() {
        fragment = AddFieldFragment.newInstance(onChoosedFieldType);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_edit_doc_data_fields, container, false);
        containerView = (LinearLayout) mView.findViewById(R.id.containerView);
        fieldViews.clear();
        mView.findViewById(R.id.addDataField).setOnClickListener(this);

        return mView;
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        initFields();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Document document = ((CreateEditDocActivity) getActivity()).curDocument;
        document.setFieldsList(getFields());
    }

    private void initFields() {
        Document document = ((CreateEditDocActivity) getActivity()).curDocument;
        List<Field> fields = document.getFieldsList();

        if (fields == null)
            return;//TODO:

        for (Field field : fields)
            addFieldView(field);
    }

    private FieldEditView addFieldView(Field field) {
        DeveloperUtil.michaelLog("field.getCreateTime() - " + field.getCreateTime());
        int position = containerView.getChildCount() - 1;
        FieldEditView fieldView = FieldViewFactory.makeFieldView(getActivity(), field);
        fieldView.setField(field);
        containerView.addView(fieldView, position);
        fieldViews.add(fieldView);
        fieldView.setOnDeleteFieldListener(new CustomFieldTextView.OnDeleteFieldListener() {
            @Override
            public void onDeleteField(FieldEditView view) {
                removeFieldView(view);
            }
        });
        return fieldView;
    }

    private boolean removeFieldView(FieldEditView view) {
        deleteView = view;
        Bundle token = new Bundle();
        token.putInt(DELETE_POSITION, containerView.indexOfChild(view));

        new UndoBarController.UndoBar(getActivity())
                .message(getString(R.string.message_field_deleted))
                .listener(EditDocDataFieldsFragment.this)
                .token(token)
                .show();

        containerView.removeView(view);
        return fieldViews.remove(view);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addDataField:
                addDataField();
                break;
        }
    }

    private void addCustomTextField() {
        addCustomField(Field.TEXT_TYPE);
    }

    private void addCustomDateField() {
        addCustomField(Field.DATE_TYPE);
    }

    private void addCustomField(int type) {
        Document document = ((CreateEditDocActivity) getActivity()).curDocument;
        Field field = Field.createCustomField(type);
        document.getFieldsList().add(field);
        addFieldView(field);
    }

    private void addDataField() {
        DeveloperUtil.michaelLog();
        openDialog();
    }

    public void updateCategoryType() {
        containerView.removeViews(0, containerView.getChildCount() - 1);
        fieldViews.clear();
        initFields();
    }

    public List<Field> getFields() {
        List<Field> fields = new ArrayList<>(fieldViews.size());
        for (FieldEditView fieldView : fieldViews)
            fields.add(fieldView.getField());
        return fields;
    }

    @Override
    public void onUndo(Parcelable parcelable) {
        Bundle token = (Bundle) parcelable;
        int position = token.getInt(DELETE_POSITION);

        if (deleteView != null) {
            containerView.addView(deleteView, position);
            fieldViews.add(position, deleteView);

            deleteView = null;
        }
    }
}
