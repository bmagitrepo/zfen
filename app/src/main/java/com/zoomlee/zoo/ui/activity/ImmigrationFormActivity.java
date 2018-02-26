package com.zoomlee.zoo.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import com.radaee.pdf.Document;
import com.radaee.reader.PDFLayoutView;
import com.zoomlee.zoo.R;
import com.zoomlee.zoo.net.model.Form;
import com.zoomlee.zoo.net.model.helpers.FormLoader;
import com.zoomlee.zoo.provider.FilesProvider;
import com.zoomlee.zoo.ui.view.ImageLoadingView;
import com.zoomlee.zoo.utils.IntentUtils;
import com.zoomlee.zoo.utils.PrintUtils;
import com.zoomlee.zoo.utils.RequestCodes;

import java.io.File;

/**
 * Created by
 *
 * @author Michael Oknov <misha.oknov@alterplay.com>
 * @since 5/28/15
 */
public class ImmigrationFormActivity extends SecuredActionBarActivity implements View.OnClickListener {

    private static final String EXTRA_FORM = "zoomlee_extra_form";

    private ImageLoadingView loadingView;
    private PDFLayoutView pdfView;

    private Form form;
    private View printLayout;
    private View editFormLayout;

    private FormLoader formLoader;
    private boolean reload;
    private Document pdfDocument;
    private LoadFormTask loadFormTask;

    public static void startImmigrationForm(Activity activity, Form form) {
        Intent intent = new Intent(activity, ImmigrationFormActivity.class);
        intent.putExtra(IntentUtils.EXTRA_OPEN_WITH_PIN, false);
        intent.putExtra(EXTRA_FORM, form);
        activity.startActivityForResult(intent, RequestCodes.IMMIGRATION_FORM_ACTIVITY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        form = getIntent().getParcelableExtra(EXTRA_FORM);

        pdfView = (PDFLayoutView) findViewById(R.id.pdfView);
        loadingView = (ImageLoadingView) findViewById(R.id.loading);
        printLayout = findViewById(R.id.printLayout);
        editFormLayout = findViewById(R.id.editFormLayout);
        printLayout.setOnClickListener(this);
        editFormLayout.setOnClickListener(this);

        formLoader = new FormLoader(ImmigrationFormActivity.this, form);

        loadForm();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(form.getName());
        updateABAvatar(form.getLocalPersonId());
    }

    private void loadPdf(File file) {
        pdfView.setVisibility(View.VISIBLE);

        pdfDocument = new Document();
        int ret = pdfDocument.Open(file.getAbsolutePath(), "");
        processOpenResult(ret);
    }

    private final void processOpenResult(int ret)
    {
        switch( ret )
        {
            case 0://succeeded, and continue
                loadingView.hide();
                pdfView.PDFOpen(pdfDocument, null);
                break;
            case -1://need input password
            case -2://unknown encryption
            case -3://damaged or invalid format
            case -10://access denied or invalid file path
            default://unknown error
                pdfDocument.Close();
                pdfDocument = null;
                pdfView.setVisibility(View.GONE);
                loadingView.showError();
                break;
        }
    }

    private void loadForm() {
        cancelLoadTask();
        loadFormTask = new LoadFormTask();
        loadFormTask.execute();
    }

    private void cancelLoadTask() {
        if (loadFormTask != null) {
            loadFormTask.cancel(true);
            loadFormTask = null;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.editFormLayout:
                EditFormActivity.startToEditForm(this, form);
                break;
            case R.id.printLayout:
                printForm();
                break;
        }
    }

    private void printForm() {
        loadingView.show();
        editFormLayout.setEnabled(false);
        printLayout.setEnabled(false);
        pdfView.setEnabled(false);

        formLoader.getPrintAblePdfFormAsync(new FormLoader.OnLoadCompleteListener() {
            @Override
            public void loadCompleted(File formFile) {
                Uri pdfFormUri = Uri.parse("content://" + FilesProvider.CACHE_DIR_AUTHORITY + "/"
                        + formFile.getName());
                PrintUtils.doPrint(ImmigrationFormActivity.this, formFile, pdfFormUri, form.getName());

                loadingView.hide();
                editFormLayout.setEnabled(true);
                printLayout.setEnabled(true);
                pdfView.setEnabled(true);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (reload) {
            loadForm();
            reload = false;
        }
    }

    @Override
    protected void onPause() {
        cancelLoadTask();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pdfView.PDFClose();
        if (pdfDocument != null) pdfDocument.Close();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestCodes.PRINT_REQUEST || requestCode == RequestCodes.EDIT_FORM_ACTIVITY) {
            unpin();

            if (requestCode == RequestCodes.EDIT_FORM_ACTIVITY && resultCode == RESULT_OK) {
                Form newForm = data.getParcelableExtra(EditFormActivity.EXTRA_FORM);
                if (newForm != null) {
                    form = newForm;
                    formLoader = new FormLoader(this, form, true);
                    reload = true;
                }
            }
        }
    }

    private class LoadFormTask extends AsyncTask<Void, Void, File> {
        @Override
        protected void onPreExecute() {
            pdfView.setVisibility(View.GONE);
            loadingView.show();
            editFormLayout.setEnabled(false);
            printLayout.setEnabled(false);
        }

        @Override
        protected File doInBackground(Void... params) {
            return formLoader.getViewAblePdfForm();
        }

        @Override
        protected void onPostExecute(File file) {
            loadPdf(file);
            editFormLayout.setEnabled(true);
            printLayout.setEnabled(true);
        }
    }
}
