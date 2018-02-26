package com.zoomlee.zoo.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.dao.DaoHelper;
import com.zoomlee.zoo.dao.DaoHelpersContainer;
import com.zoomlee.zoo.net.model.Person;
import com.zoomlee.zoo.ui.fragments.CreateEditPersonFragment;
import com.zoomlee.zoo.ui.fragments.dialog.ImagePickerFragment;
import com.zoomlee.zoo.utils.FileUtil;
import com.zoomlee.zoo.utils.GAEvents;
import com.zoomlee.zoo.utils.GAUtil;
import com.zoomlee.zoo.utils.IntentUtils;
import com.zoomlee.zoo.utils.RequestCodes;

import java.io.File;
import java.io.IOException;

public class CreateEditPersonActivity extends SecuredActionBarActivity implements View.OnClickListener, ImagePickerFragment.OnImagePickedListener {

    public static final String PERSON_ID_EXTRA = "person_id";
    private static final String EXTRA_PERSON = "person_to_edit";

    private boolean editPersonsState;
    private Person person;
    private View toolbarCancelOk;
    private CreateEditPersonFragment createEditPersonFragment;

    public static void startToEditPerson(Activity activity, Person person) {
        Intent intent = new Intent(activity, CreateEditPersonActivity.class);
        intent.putExtra(IntentUtils.EXTRA_OPEN_WITH_PIN, false);
        intent.putExtra(EXTRA_PERSON, person);
        activity.startActivityForResult(intent, RequestCodes.EDIT_PERSON);
    }

    public static void startToCreatePerson(Activity activity) {
        Intent intent = new Intent(activity, CreateEditPersonActivity.class);
        intent.putExtra(IntentUtils.EXTRA_OPEN_WITH_PIN, false);
        activity.startActivityForResult(intent, RequestCodes.CREATE_PERSONS);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCustomizedView(R.layout.activity_create_edit_person);

        person = getIntent().getParcelableExtra(EXTRA_PERSON);
        editPersonsState = (person != null);

        createEditPersonFragment = (CreateEditPersonFragment) getSupportFragmentManager()
                .findFragmentById(R.id.create_edit_person_fragment);
        createEditPersonFragment.setPerson(person);

        initUI();

        String action = editPersonsState ? GAEvents.ACTION_EDIT_MEMBER_SCREEN : GAEvents.ACTION_ADD_MEMBER_SCREEN;
        GAUtil.getUtil().timeSpent(action);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        int fragmentRequestCode = requestCode & 0xffff;
        switch (fragmentRequestCode) {
            case RequestCodes.PICK_FROM_CAMERA:
            case RequestCodes.PICK_FROM_GALLERY:
            case RequestCodes.CROP_IMAGE_REQUEST:
                unpin();
                return;
        }
    }

    private void initUI() {
        toolbarCancelOk = findViewById(R.id.toolbarCancelOk);

        if (editPersonsState) {
            toolbarCancelOk.setVisibility(View.GONE);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.family_member);
        } else {
            toolbar.setVisibility(View.GONE);
            TextView btnAdd = (TextView) findViewById(R.id.submitText);
            btnAdd.setText(R.string.add_btn_text);
            findViewById(R.id.submitFrame).setOnClickListener(this);
            findViewById(R.id.cancelFrame).setOnClickListener(this);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        savePersonChanges();
    }

    private void savePersonChanges() {
        File newIcon = new File(getFilesDir(), CreateEditPersonFragment.TMP_IMG);
        File newCroppedIcon = new File(getFilesDir(), CreateEditPersonFragment.TMP_CROPPED_IMG);
        boolean changed = false;

        if (editPersonsState) {
            String name = getPersonName();
            if (!name.equals(person.getName())) changed = true;

            person.setName(name);
            if (newIcon.exists()) {
                changed = true;
                String localFilePath = person.getImageLocalPath();
                if (TextUtils.isEmpty(localFilePath))
                    localFilePath = getFilesDir().getAbsolutePath() + "/person_icon_" + System.currentTimeMillis() + ".png";
                person.setImageLocalPath(localFilePath);
                try {
                    FileUtil.copyFileUsingFileChannels(newIcon, new File(localFilePath));
                    FileUtil.copyFileUsingFileChannels(newCroppedIcon, new File(person.getImageLocal144Path()));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (!new File(localFilePath).exists()) person.setImageLocalPath(null);
            }

            if (changed) {
                DaoHelper<Person> personDaoHelper = DaoHelpersContainer.getInstance().getDaoHelper(Person.class);
                personDaoHelper.saveLocalChanges(this, person);
            }
        }

        newIcon.delete();
        newCroppedIcon.delete();
    }

    private void saveNewPerson() {
        File newIcon = new File(getFilesDir(), CreateEditPersonFragment.TMP_IMG);
        File newCroppedIcon = new File(getFilesDir(), CreateEditPersonFragment.TMP_CROPPED_IMG);

        Person person = new Person();
        person.setName(getPersonName());

        if (newIcon.exists()) {
            String localFilePath = getFilesDir().getAbsolutePath() + "/person_icon_" + System.currentTimeMillis() + ".png";
            person.setImageLocalPath(localFilePath);
            try {
                FileUtil.copyFileUsingFileChannels(newIcon, new File(localFilePath));
                FileUtil.copyFileUsingFileChannels(newCroppedIcon, new File(person.getImageLocal144Path()));
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (!new File(localFilePath).exists()) person.setImageLocalPath(null);
        }

        DaoHelper<Person> personDaoHelper = DaoHelpersContainer.getInstance().getDaoHelper(Person.class);
        personDaoHelper.saveLocalChanges(this, person);
        GAUtil.getUtil().eventMade(GAEvents.ACTION_FAMILY_MEMBERS_ADDED, GAEvents.LABEL_FAMILY_MEMBERS_ADDED);

        newIcon.delete();
        newCroppedIcon.delete();

        Intent intent = new Intent();
        intent.putExtra(PERSON_ID_EXTRA, person.getId());
        setResult(RESULT_OK, intent);

        finish();
    }

    private String getPersonName() {
        String name = createEditPersonFragment.getName();
        if (TextUtils.isEmpty(name)) {
            name = "Name";
        }

        return name;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancelFrame:
                onBackPressed();
                break;
            case R.id.submitFrame:
                saveNewPerson();
                break;
            default:
                break;
        }
    }

    @Override
    public void onImagePicked(File image) {
        if (createEditPersonFragment != null) {
            createEditPersonFragment.onImageObtained(image);
        }
    }
}
