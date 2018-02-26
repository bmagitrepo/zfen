package com.zoomlee.zoo.ui.activity;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.ZoomleeApp;
import com.zoomlee.zoo.dao.DaoHelper;
import com.zoomlee.zoo.dao.DaoHelpersContainer;
import com.zoomlee.zoo.net.model.Document;
import com.zoomlee.zoo.net.model.Field;
import com.zoomlee.zoo.net.model.File;
import com.zoomlee.zoo.net.model.Person;
import com.zoomlee.zoo.net.model.Tag;
import com.zoomlee.zoo.provider.FilesProvider;
import com.zoomlee.zoo.ui.MaterialDialog;
import com.zoomlee.zoo.ui.view.DocumentIconView;
import com.zoomlee.zoo.ui.view.ImageFlipper;
import com.zoomlee.zoo.ui.view.field.FieldView;
import com.zoomlee.zoo.ui.view.field.FieldsTableView;
import com.zoomlee.zoo.utils.Events;
import com.zoomlee.zoo.utils.FileLoader;
import com.zoomlee.zoo.utils.GAEvents;
import com.zoomlee.zoo.utils.GAUtil;
import com.zoomlee.zoo.utils.IntentUtils;
import com.zoomlee.zoo.utils.RequestCodes;
import com.zoomlee.zoo.utils.SharedPreferenceUtils;
import com.zoomlee.zoo.utils.TimeUtil;
import com.zoomlee.zoo.utils.UiUtil;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class DocumentDetailsActivity extends SecuredActionBarActivity {

    public static final String DOCUMENT_ID_KEY = "document_id";
    public static final String EXTRA_PERSON_ID_KEY = "person_id";
    private static final String CURRENT_FILE_ID = "current_file_id";
    public static final String FROM_NOTIFICATION = "FROM_NOTIFICATION";
    private static final int UNDEFINED_PERSON_ID = -20;

    private Document document;
    private int documentId;

    private View alertIv;
    private FieldsTableView fieldsTableView;
    private DocumentIconView iconView;
    private TextView tvDocName;
    private FieldView tagsField;
    private FieldView notesField;
    private ImageFlipper imageFlipper;
    private LoadDocumentAsyncTask loadTask;
    private ImageView ivPersonIcon;
    private ScrollView mainScrollView;
    private int currentFileId = -1;
    private boolean isFromNotifcation = false;
    /**
     * File loader to load files that are opened with {@link IntentUtils}.
     */
    private FileLoader fileLoader;

    public static void startActivity(Activity context, int documentId) {
        startActivity(context, documentId, UNDEFINED_PERSON_ID);
    }

    public static void startActivity(Activity context, int documentId, int persondId) {
        Intent intent = getIntentForStart(context, documentId, false, persondId, false);
        context.startActivityForResult(intent, RequestCodes.DOCUMENT_DETAILS_ACTIVITY);
    }

    public static Intent getIntentForStart(Context context, int documentId, boolean withPin, int personId, boolean fromNotification) {
        Intent intent = new Intent(context, DocumentDetailsActivity.class);
        intent.putExtra(IntentUtils.EXTRA_OPEN_WITH_PIN, withPin);
        intent.putExtra(EXTRA_PERSON_ID_KEY, personId);
        intent.putExtra(DOCUMENT_ID_KEY, documentId);
        intent.putExtra(FROM_NOTIFICATION, fromNotification);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        documentId = getIntent().getIntExtra(DOCUMENT_ID_KEY, -1);
        isFromNotifcation = getIntent().getBooleanExtra(FROM_NOTIFICATION, false);
        int personId = getIntent().getIntExtra(EXTRA_PERSON_ID_KEY, UNDEFINED_PERSON_ID);
        if (personId == UNDEFINED_PERSON_ID)
            personId = ((ZoomleeApp) getApplication()).getSelectedPersonId();

        updateABAvatar(personId);

        if (savedInstanceState != null) {
            currentFileId = savedInstanceState.getInt(CURRENT_FILE_ID, -1);
        }

        initUI();
        GAUtil.getUtil().timeSpent(GAEvents.ACTION_DOCUMENTS_DETAILS_VIEW);
    }

    @Override
    protected void onResume() {
        super.onResume();

        EventBus.getDefault().register(this);
        loadData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
        cancelTask();

        if (fileLoader != null) {
            fileLoader.onStop();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (imageFlipper.initialized())
            outState.putInt(CURRENT_FILE_ID, imageFlipper.getCurrentFile().getId());
    }

    private void loadData() {
        cancelTask();
        loadTask = new LoadDocumentAsyncTask();
        loadTask.execute();
    }

    private void cancelTask() {
        if (loadTask != null) {
            loadTask.cancel(true);
            loadTask = null;
        }
    }

    private void try2Fill() {
        ZoomleeApp app = (ZoomleeApp) getApplication();
        if (app.getSelectedPersonId() != Person.ALL_ID)
            updateABAvatar(document.getLocalPersonId());
        if (document.getTagsList().isEmpty()) {
            tagsField.setVisibility(View.GONE);
        } else {
            tagsField.setField(getString(R.string.title_tags), getFormattedTagsString());
            tagsField.setVisibility(View.VISIBLE);
        }
        if (TextUtils.isEmpty(document.getNotes())) {
            notesField.setVisibility(View.GONE);
        } else {
            notesField.setVisibility(View.VISIBLE);
            notesField.setField(getString(R.string.title_notes), document.getNotes());
        }
        tvDocName.setText(document.getName().toUpperCase());
        iconView.setDocument(document);
        fieldsTableView.setFields(document.getVisibleFields(), 0, false, document.isWorkPermit());
        viewOwnerIcon();
        updateAlertIv();
        viewFiles();

        GAUtil.getUtil().screenAccess(GAEvents.ACTION_DOCUMENT_TYPE, document.getCategoryName() + ": " + document.getTypeName());
    }

    private String getFormattedTagsString() {
        StringBuilder sb = new StringBuilder();
        for (Tag tag : document.getTagsList())
            sb.append(tag.getName()).append(", ");
        if (sb.length() > 0) sb.delete(sb.length() - 2, sb.length());

        return sb.toString();
    }

    private void viewOwnerIcon() {
        ZoomleeApp app = (ZoomleeApp) getApplication();
        if (app.getSelectedPersonId() == Person.ALL_ID) {
            Person person;
            if (document.getLocalPersonId() == Person.ME_ID)
                person = SharedPreferenceUtils.getUtils().getUserSettings();
            else {
                DaoHelper<Person> daoHelper = DaoHelpersContainer.getInstance().getDaoHelper(Person.class);
                person = daoHelper.getItemByLocalId(this, document.getLocalPersonId());
            }

            if (person == null) {
                ivPersonIcon.setImageResource(R.drawable.stub_person_green);
            } else {
                UiUtil.loadPersonIcon(person, ivPersonIcon, false);
            }
            ivPersonIcon.setVisibility(View.VISIBLE);
        } else {
            ivPersonIcon.setVisibility(View.GONE);
        }
    }

    private void updateAlertIv() {
        boolean alert = false;
        long currentTime = TimeUtil.getServerEndDayTimestamp();
        for (Field field : document.getVisibleFields()) {
            long notifyOn = field.getLongNotifyOn();
            if (notifyOn != -1 && notifyOn < currentTime) {
                alert = true;
                break;
            }
        }
        alertIv.setVisibility(alert ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onBackPressed() {
        if (isFromNotifcation)
            closeApp();
        super.onBackPressed();
    }


    private void initUI() {
        fieldsTableView = (FieldsTableView) findViewById(R.id.fieldsTable);
        alertIv = findViewById(R.id.alertIv);
        iconView = (DocumentIconView) findViewById(R.id.document_icon);
        tvDocName = (TextView) findViewById(R.id.docNameTv);
        tagsField = (FieldView) findViewById(R.id.tagsField);
        tagsField.setSingleLine(false);
        notesField = (FieldView) findViewById(R.id.notesField);
        notesField.setSingleLine(false);
        imageFlipper = (ImageFlipper) findViewById(R.id.imageFlipper);
        ivPersonIcon = (ImageView) findViewById(R.id.personIconIv);
        mainScrollView = (ScrollView) findViewById(R.id.scrollView);

        imageFlipper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File currentFile = imageFlipper.getCurrentFile();
                fileLoader = IntentUtils.openFile(DocumentDetailsActivity.this, currentFile, document.getName());
            }
        });

        findViewById(R.id.editDocument).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateEditDocActivity.startActivity(DocumentDetailsActivity.this, document.getId());
            }
        });
    }

    private void viewFiles() {
        if (document.getFilesList().size() == 0) {
            imageFlipper.setVisibility(View.GONE);
        } else {
            int fileIndex;
            File currentFile;
            if (imageFlipper.initialized())
                currentFile = imageFlipper.getCurrentFile();
            else {
                currentFile = new File();
                currentFile.setId(currentFileId);
            }
            fileIndex = document.getFilesList().indexOf(currentFile);
            if (fileIndex == -1) fileIndex = 0;

            imageFlipper.init(document.getFilesList(), mainScrollView, fileIndex);
            imageFlipper.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_doc_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (isFromNotifcation) {
                    finish();
                    PendingIntent pi = TaskStackBuilder.create(this)
                            .addNextIntentWithParentStack(MainActivity.getIntentForStart(this, false))
                            .addNextIntentWithParentStack(DocumentsActivity.getIntentForStart(this, document.getCategoryId(), document.getLocalPersonId(), -1, false))
                            .getPendingIntent((int) System.currentTimeMillis(), PendingIntent.FLAG_UPDATE_CURRENT);

                    try {
                        pi.send();
                    } catch (PendingIntent.CanceledException ce) {
                        ce.printStackTrace();
                    }

                } else
                    onBackPressed();
                return true;
            case R.id.action_mail:
                sendDocByEMail();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RequestCodes.EDIT_DOCUMENT:
            case RequestCodes.OPEN_ATTACHMENT:
                unpin();
                break;
        }
    }

    private void sendDocByEMail() {
        Intent i = new Intent(Intent.ACTION_SEND_MULTIPLE);
        i.setType("message/rfc822");
        i.setClassName("com.google.android.gm", "com.google.android.gm.ComposeActivityGmail");
        i.putExtra(Intent.EXTRA_SUBJECT, document.getName());

        List<ResolveInfo> resolveInfos = getPackageManager().queryIntentActivities(i, 0);
        if (resolveInfos.size() == 0) {
            MaterialDialog mMaterialDialog = new MaterialDialog(this)
                    .setMessage(R.string.no_app_to_share)
                    .setPositiveButton(R.string.ok, null);

            mMaterialDialog.show();
            return;
        }

        StringBuilder mailBody = new StringBuilder();
        for (Field field : document.getFieldsList()) {
            if (TextUtils.isEmpty(field.getFormattedValue())) continue;
            mailBody.append(field.getName()).append(": ")
                    .append(field.getFormattedValue()).append("\n");
        }
        i.putExtra(Intent.EXTRA_TEXT, mailBody.toString());

        if (document.getFilesList().size() > 0) {
            ArrayList<Uri> uris = new ArrayList<>();
            Uri fileUri;
            for (File file : document.getFilesList()) {
                if (file.getLocalPath() != null) {
                    fileUri = Uri.parse("content://" + FilesProvider.FILE_DIR_AUTHORITY + "/"
                            + new java.io.File(file.getLocalPath()).getName());
                    uris.add(fileUri);
                }
            }
            i.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        }

        startActivityForResult(Intent.createChooser(i, null), RequestCodes.SEND_EMAIL);
    }

    public void onEventMainThread(Events.DocumentChanged event) {
        if (event.getDocument().getId() == documentId)
            loadData();
    }

    private class LoadDocumentAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            DaoHelper<Document> daoHelper = DaoHelpersContainer.getInstance().getDaoHelper(Document.class);
            document = daoHelper.getItemByLocalId(DocumentDetailsActivity.this, documentId);
            return null;
        }

        @Override
        protected void onPostExecute(Void mVoid) {
            if (document == null)
                finish();
            else
                try2Fill();
            mainScrollView.setVisibility(View.VISIBLE);
        }
    }
}
