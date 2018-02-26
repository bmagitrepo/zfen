package com.zoomlee.zoo.ui.fragments;


import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.dao.DaoHelper;
import com.zoomlee.zoo.dao.DaoHelpersContainer;
import com.zoomlee.zoo.net.model.Category;
import com.zoomlee.zoo.net.model.Color;
import com.zoomlee.zoo.net.model.Document;
import com.zoomlee.zoo.net.model.DocumentsType;
import com.zoomlee.zoo.net.model.Person;
import com.zoomlee.zoo.provider.helpers.DocumentsTypesHelper;
import com.zoomlee.zoo.ui.MaterialDialog;
import com.zoomlee.zoo.ui.activity.CategoryTypeListActivity;
import com.zoomlee.zoo.ui.activity.CreateEditDocActivity;
import com.zoomlee.zoo.ui.activity.PersonListActivity;
import com.zoomlee.zoo.ui.view.ColorPickerView;
import com.zoomlee.zoo.ui.view.ZMEditText;
import com.zoomlee.zoo.utils.BillingUtils;
import com.zoomlee.zoo.utils.DBUtil;
import com.zoomlee.zoo.utils.SharedPreferenceUtils;
import com.zoomlee.zoo.utils.UiUtil;
import com.zoomlee.zoo.utils.Util;

import java.util.List;
import java.util.Random;


public class EditDocGeneralFragment extends Fragment implements View.OnClickListener, ColorPickerView.OnColorSelectedListener {

    private ColorPickerView colorPicker;
    private TextView personName;
    private ImageView personAvatar;
    private ZMEditText nameTv;
    private TextView categoryTypeTv;
    private ImageView categoryTypeIv;
    private Person selectedPerson;
    private EditText notesEt;


    public static EditDocGeneralFragment newInstance() {
        EditDocGeneralFragment fragment = new EditDocGeneralFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_edit_doc_general, container, false);
        Document document = ((CreateEditDocActivity) getActivity()).curDocument;
        colorPicker = (ColorPickerView) mView.findViewById(R.id.colorPicker);
        DaoHelper<Color> daoHelperColor = DaoHelpersContainer.getInstance().getDaoHelper(Color.class);
        List<Color> colors = daoHelperColor.getAllItems(getActivity());
        colorPicker.setColors(colors);
        colorPicker.selectColor(new Random().nextInt(colors.size()));
        colorPicker.setOnColorSelectedListener(this);

        categoryTypeTv = (TextView) mView.findViewById(R.id.categoryTypeTv);
        categoryTypeIv = (ImageView) mView.findViewById(R.id.categoryTypeIv);
        mView.findViewById(R.id.categoryType).setOnClickListener(this);

        personName = (TextView) mView.findViewById(R.id.personNameTv);
        notesEt = (EditText) mView.findViewById(R.id.notesEt);
        personAvatar = (ImageView) mView.findViewById(R.id.personAvatarIv);
        mView.findViewById(R.id.changePerson).setOnClickListener(this);
        nameTv = (ZMEditText) mView.findViewById(R.id.documentNameEt);
        nameTv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ((CreateEditDocActivity) getActivity()).getSupportActionBar().setTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        View deleteBtn = mView.findViewById(R.id.deleteDocument);
        if (document.getId() == -1)
            deleteBtn.setVisibility(View.GONE);
        else
            deleteBtn.setOnClickListener(this);
        try2Fill(document);
        return mView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        String name = getDocumentName();
        Color color = getDocumentColor();
        Person owner = getOwner();
        String notes = getDocumentNotes();

        Document curDocument = ((CreateEditDocActivity) getActivity()).curDocument;
        curDocument.setName(TextUtils.isEmpty(name) ? getString(R.string.other_document) : name);
        curDocument.setColorHEX(color.getHex());
        curDocument.setColorId(color.getRemoteId());
        curDocument.setColorName(color.getName());
        curDocument.setLocalPersonId(owner.getId());
        curDocument.setNotes(TextUtils.isEmpty(notes) ? "" : notes);
    }

    private void try2Fill(Document document) {
        Person owner = getOwnerForDocument(document, getActivity());
        nameTv.setText(document.getName());
        colorPicker.selectColor(document.getColorHEX());
        notesEt.setText(document.getNotes());
        updatePerson(owner);
        updateCategoryType();
    }

    private Person getOwnerForDocument(Document document, Activity activity) {
        DaoHelper<Person> daoHelperPerson = DaoHelpersContainer.getInstance().getDaoHelper(Person.class);
        int ownerPersonId = document.getLocalPersonId();

        return ownerPersonId == Person.ME_ID ? SharedPreferenceUtils.getUtils().getUserSettings()
                : daoHelperPerson.getItemByLocalId(activity, ownerPersonId);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.changePerson:
                changePerson();
                break;
            case R.id.deleteDocument:
                showDeleteDialog();
                break;
            case R.id.categoryType:
                changeType();
                break;
        }
    }

    public void changePerson() {
        if (getActivity() != null &&
                BillingUtils.canStart(getActivity(), BillingUtils.ActionType.SELECT_PERSON)) {
            PersonListActivity.startToSelectPerson(getActivity(), selectedPerson);
        }
    }

    private void showDeleteDialog() {
        MaterialDialog mMaterialDialog = new MaterialDialog(getActivity())
                .setTitle(R.string.title_warning)
                .setMessage(R.string.are_you_sure_you_want_to_delete_this_document)
                .setPositiveButton(R.string.delete_uc, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteDocument();
                    }
                })
                .setNegativeButton(R.string.cancel_uc, null);

        mMaterialDialog.show();
    }

    private void deleteDocument() {
        CreateEditDocActivity curActivity = (CreateEditDocActivity) getActivity();
        DaoHelper<Document> daoHelper = DaoHelpersContainer.getInstance().getDaoHelper(Document.class);
        daoHelper.deleteItem(curActivity, curActivity.curDocument);
        curActivity.finish();
    }

    private void changeType() {
        Document document = ((CreateEditDocActivity) getActivity()).curDocument;
        CategoryTypeListActivity.startForResult(getActivity(), document.getTypeId(), document.getLocalPersonId());
    }

    public void updatePerson(Person owner) {
        selectedPerson = owner;
        personName.setText(owner.getName());
        UiUtil.loadPersonIcon(owner, personAvatar, false);
    }

    public void updateCategoryType() {
        Document document = ((CreateEditDocActivity) getActivity()).curDocument;

        String categoryTypeStr;
        if (document.getTypeId() == DocumentsType.OTHER_DOC_TYPE.getRemoteId())
            categoryTypeStr = document.getTypeName();
        else
            categoryTypeStr = document.getCategoryName() + ": " + document.getTypeName();

        categoryTypeTv.setText(categoryTypeStr);
        categoryTypeIv.setImageResource(Category.getIconRes(document.getCategoryId()));
        updateCategoryIconColor();

        ContentResolver resolver = getActivity().getContentResolver();
        Cursor cursor = resolver.query(DocumentsTypesHelper.DocumentsTypesContract.CONTENT_URI, null,
                DocumentsTypesHelper.DocumentsTypesContract.NAME + "=?", DBUtil.getArgsArray(nameTv.getText()), null);
        if (cursor != null && cursor.getCount() > 0)
            nameTv.setText(document.getTypeName());
    }

    public String getDocumentName() {
        return nameTv.getText().toString();
    }

    public Color getDocumentColor() {
        return colorPicker.getSelectedColor();
    }

    public Person getOwner() {
        return selectedPerson;
    }

    public String getDocumentNotes() {
        String notes = notesEt.getText().toString();
        notes = Util.trimEnd(notes);
        return notes;
    }

    private void updateCategoryIconColor() {
        int color = android.graphics.Color.parseColor("#" + getDocumentColor().getHex());
        categoryTypeIv.setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

    @Override
    public void onColorSelected(Color color) {
        updateCategoryIconColor();
    }
}
