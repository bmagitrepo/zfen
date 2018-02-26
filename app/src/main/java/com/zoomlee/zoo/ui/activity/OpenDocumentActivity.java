package com.zoomlee.zoo.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.scopes.opendocument.DaggerOpenDocumentComponent;
import com.zoomlee.zoo.scopes.opendocument.OpenDocumentComponent;
import com.zoomlee.zoo.scopes.opendocument.OpenDocumentModule;
import com.zoomlee.zoo.ui.view.OpenDocView;
import com.zoomlee.zoo.utils.ActivityUtils;
import com.zoomlee.zoo.utils.FileUtil;
import com.zoomlee.zoo.utils.RequestCodes;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.BindView;

public class OpenDocumentActivity extends SecuredActionBarActivity implements OpenDocView.Presenter {

    @BindView(R.id.open_doc_view)
    OpenDocView openDocView;

    private File file;
    private com.zoomlee.zoo.net.model.File.Type fileType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setCustomizedView(R.layout.activity_open_document, false);

        ButterKnife.bind(this);

        createComponent().injectView(openDocView);

        if (getFile() == null) {
            Toast.makeText(this, R.string.message_cant_open_file, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        openDocView.setFileName(getFile().getName());
    }

    @Override
    public void onBackPressed() {
        cancel();

        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RequestCodes.CREATE_DOCUMENT) {
            ActivityUtils.finishAndRemoveTask(this);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private OpenDocumentComponent createComponent() {
        return DaggerOpenDocumentComponent.builder()
                .openDocumentModule(new OpenDocumentModule(this))
                .build();
    }

    private File getFile() {
        if (file == null) {
            // TODO: access content provider in worker thread
            file = FileUtil.fileFromUri(this, getIntent().getData());
        }
        return file;
    }

    private com.zoomlee.zoo.net.model.File.Type getFileType() {
        File file = getFile();
        if (fileType == null) {
            // first try mime type from the intent
            String mimeType = getIntent().getType();
            if (mimeType != null) {
                fileType = com.zoomlee.zoo.net.model.File.Type.byMimeType(mimeType);
            } else {
                fileType = com.zoomlee.zoo.net.model.File.Type.byPath(file.getPath());
            }
        }
        return fileType;
    }

    @Override
    public void addToExistingDocument() {
        SelectPersonActivity.startActivity(this, getFile().getPath(), getFileType());
        finish();
    }

    @Override
    public void createNewDocument() {
        CreateEditDocActivity.withAttachment(this, getFile().getPath(), getFileType(), CreateEditDocActivity.CREATE_DOCUMENT_ID);
    }

    @Override
    public void cancel() {
        ActivityUtils.finishAndRemoveTask(this);
    }
}
