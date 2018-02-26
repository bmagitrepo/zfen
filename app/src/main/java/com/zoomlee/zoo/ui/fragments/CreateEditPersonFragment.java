package com.zoomlee.zoo.ui.fragments;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.dao.DaoHelper;
import com.zoomlee.zoo.dao.DaoHelpersContainer;
import com.zoomlee.zoo.net.model.Person;
import com.zoomlee.zoo.ui.MaterialDialog;
import com.zoomlee.zoo.ui.view.ZMEditText;
import com.zoomlee.zoo.utils.FileUtil;
import com.zoomlee.zoo.utils.UiUtil;
import com.zoomlee.zoo.utils.Util;

import java.io.File;
import java.io.IOException;

public class CreateEditPersonFragment extends FragmentWithImagePicker implements View.OnClickListener{

    public static final String TMP_IMG = "person_tmp.png";
    public static final String TMP_CROPPED_IMG = "cropped_person_tmp.png";

    private boolean editPersonsState;
    private Person person;
    private ZMEditText tvName;
    private Button btnDelete;
    private TextView tvAddPhoto;
    private ImageView avatarIv;
    private View tvDeleteNote;
    private View editPictureView;

    public CreateEditPersonFragment() {
        super(TMP_IMG, true);
    }

    public void setPerson(Person person) {
        this.person = person;
        this.editPersonsState = (person != null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_create_edit_person, container, false);
        initUi(mView);
        initListeners();

        return mView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadUi();
    }

    private void initUi(View mView) {
        avatarIv = (ImageView) mView.findViewById(R.id.avatarIv);
        tvAddPhoto = (TextView) mView.findViewById(R.id.edit_picture_text);
        editPictureView = mView.findViewById(R.id.edit_picture_view);
        tvName = (ZMEditText) mView.findViewById(R.id.nameTv);
        btnDelete = (Button) mView.findViewById(R.id.deleteBtn);
        tvDeleteNote = mView.findViewById(R.id.deleteNoteTv);
    }

    private void initListeners() {
        editPictureView.setOnClickListener(this);
        btnDelete.setOnClickListener(this);
    }

    private void loadUi() {
        if (editPersonsState) {
            tvName.setText(person.getName());
            tvName.setSelection(tvName.length());
            if (!TextUtils.isEmpty(person.getImageLocalPath()) || !TextUtils.isEmpty(person.getImageRemote144Path()))
                tvAddPhoto.setText(R.string.edit_photo);
            else
                tvAddPhoto.setText(R.string.add_photo);
            UiUtil.loadPersonIcon(person, avatarIv, R.drawable.settings_me);
        } else {
            tvAddPhoto.setText(R.string.add_photo);
            tvDeleteNote.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);
        }
    }

    @Override
    public void onImageObtained(final File image) {
        if (image == null) {
            closeDialog();
        } else {
            new AsyncTask<Void, Bitmap, Void>() {

                @Override
                protected Void doInBackground(Void... params) {
                    Bitmap croppedBitmap = Util.roundBitmap(image);
                    publishProgress(croppedBitmap);

                    try {
                        FileUtil.writeBitmapToFile(croppedBitmap, new File(getActivity().getFilesDir(), TMP_CROPPED_IMG));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onProgressUpdate(Bitmap... values) {
                    avatarIv.setImageBitmap(values[0]);
                    tvAddPhoto.setText(R.string.edit_photo);
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    closeDialog();
                }
            }.execute();
        }
    }

    public String getName() {
        return tvName.getText().toString();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.edit_picture_view:
                openDialog();
                hideKeyboard();
                break;
            case R.id.deleteBtn:
                MaterialDialog mMaterialDialog = new MaterialDialog(getActivity())
                        .setTitle(R.string.delete_person_title)
                        .setMessage(R.string.delete_person_alert)
                        .setPositiveButton(R.string.delete_uc, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                DaoHelper<Person> personDaoHelper = DaoHelpersContainer.getInstance().getDaoHelper(Person.class);
                                personDaoHelper.deleteItem(getActivity(), person);
                                getActivity().finish();
                            }
                        })
                        .setNegativeButton(R.string.cancel_uc, null);
                mMaterialDialog.show();
                break;
            default:
                break;
        }
    }
}