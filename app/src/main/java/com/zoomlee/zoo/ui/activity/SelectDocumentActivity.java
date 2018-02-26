package com.zoomlee.zoo.ui.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.net.model.File;
import com.zoomlee.zoo.utils.Events;
import com.zoomlee.zoo.net.model.Document;
import com.zoomlee.zoo.scopes.data.document.DocumentsByCategoryTask;
import com.zoomlee.zoo.scopes.data.document.PersonAndCategory;
import com.zoomlee.zoo.scopes.opendocument.DaggerSelectDocumentComponent;
import com.zoomlee.zoo.scopes.opendocument.SelectDocumentComponent;
import com.zoomlee.zoo.scopes.opendocument.SelectDocumentModule;
import com.zoomlee.zoo.ui.view.selectdocument.SelectDocumentView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.BindView;
import de.greenrobot.event.EventBus;

public class SelectDocumentActivity extends UnsecuredActivity implements SelectDocumentView.Presenter {

    public static final String EXTRA_CATEGORY_ID = "extra_category_id";

    @BindView(R.id.select_document_view)
    SelectDocumentView selectDocumentView;

    private GetTask task;
    private int personId;
    private int categoryId;

    /**
     * Starts document selection step.
     * In case of opening document result is {@link #RESULT_OK}, otherwise {@link #RESULT_CANCELED}.
     *
     * @param activity         to start from
     * @param categoryId       from the previous step
     * @param attachmentIntent with attachment data
     */
    public static void startForResult(Activity activity, int categoryId, Intent attachmentIntent, int requestCode) {
        attachmentIntent.setComponent(new ComponentName(activity, SelectDocumentActivity.class));
        attachmentIntent.putExtra(EXTRA_CATEGORY_ID, categoryId);
        activity.startActivityForResult(attachmentIntent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.select_document_view);

        ButterKnife.bind(this);

        // create object pool and satisfy our dependencies from it
        createComponent().injectView(selectDocumentView);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        personId = getIntent().getIntExtra(SelectCategoryActivity.EXTRA_PERSON_ID, 0);
        categoryId = getIntent().getIntExtra(EXTRA_CATEGORY_ID, 0);

        // show profile picture
        updateABAvatar(personId);
    }

    private SelectDocumentComponent createComponent() {
        return DaggerSelectDocumentComponent.builder()
                .selectDocumentModule(new SelectDocumentModule(this))
                .build();
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadDocuments();
        EventBus.getDefault().register(this);
    }

    private void loadDocuments() {
        if (task == null) {
            // load documents only if there is no loading in progress
            task = new GetTask(this);
            task.execute(new PersonAndCategory(personId, categoryId));
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(Events.DocumentChanged event) {
        loadDocuments();
    }

    @Override
    protected void onPause() {
        super.onPause();

        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (task != null) {
            // stop and indicate it
            task.cancel(true);
            task = null;
        }
    }

    @Override
    public void selectDocument(Document document) {
        // move to create document activity
        String attachmentPath = getIntent().getStringExtra(CreateEditDocActivity.EXTRA_ATTACHMENT_PATH);
        File.Type attachmentType = (File.Type) getIntent().getSerializableExtra(CreateEditDocActivity.EXTRA_ATTACHMENT_TYPE);
        CreateEditDocActivity.withAttachment(this, attachmentPath, attachmentType, document.getId());
        setResult(RESULT_OK);
        finish();
    }

    private class GetTask extends DocumentsByCategoryTask {

        public GetTask(Context context) {
            super(context);
        }

        @Override
        protected void onPostExecute(@NonNull List<Document> documents) {
            if (!documents.isEmpty()) {
                selectDocumentView.setDocuments(documents);
            } else {
                finish();
            }

            task = null;
        }
    }
}
