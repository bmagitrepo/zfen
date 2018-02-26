package com.zoomlee.zoo.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.net.model.Form;
import com.zoomlee.zoo.net.model.FormField;
import com.zoomlee.zoo.ui.view.field.FieldTextEditView;
import com.zoomlee.zoo.utils.IntentUtils;
import com.zoomlee.zoo.utils.RequestCodes;

import java.util.HashMap;
import java.util.Map;

public class FormsArticlesActivity extends SecuredActionBarActivity {

    private Map<FormField, FieldTextEditView> fieldsToView = new HashMap<>(18);
    private LinearLayout fieldsLayout;
    private Form form;
    private String descriptionOfArticle;
    private String value;
    private int fieldsGroupMargin;

    public static void startToEditForm(Activity activity, Form form) {
        Intent intent = new Intent(activity, FormsArticlesActivity.class);
        intent.putExtra(IntentUtils.EXTRA_OPEN_WITH_PIN, false);
        intent.putExtra(EditFormActivity.EXTRA_FORM, form);
        activity.startActivityForResult(intent, RequestCodes.FORM_ARTICLES_ACTIVITY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCustomizedView(R.layout.activity_forms_articles);

        form = getIntent().getParcelableExtra(EditFormActivity.EXTRA_FORM);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        updateABAvatar(form.getLocalPersonId());

        initUI();
    }

    private void initUI() {
        descriptionOfArticle = getResources().getString(R.string.description_of_Articles);
        value = getResources().getString(R.string.value);
        fieldsGroupMargin = getResources().getDimensionPixelSize(R.dimen.form_fields_group_margin);
        fieldsLayout = (LinearLayout) findViewById(R.id.fieldsLayout);

        for (int i = FormField.FIRST_ARTICLE_ID; i <= FormField.LAST_ARTICLE_ID; i += 2) {
            createTextEditView(true, i);
            createTextEditView(false, i + 1);
        }
    }

    private void createTextEditView(boolean article, int fieldId) {
        FieldTextEditView fieldView = new FieldTextEditView(this);
        fieldView.setTitle(article ? descriptionOfArticle : value);
        fieldView.setHideTitle(true);
        FormField formField = form.getData().get(fieldId - 1);
        fieldView.setValue(formField.getValue());
        fieldsToView.put(formField, fieldView);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (!article) layoutParams.bottomMargin = fieldsGroupMargin;
        fieldsLayout.addView(fieldView, layoutParams);
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
        readViewValues();
        Intent returnedIntent = new Intent();
        returnedIntent.putExtra(EditFormActivity.EXTRA_FORM, form);
        setResult(RESULT_OK, returnedIntent);
        super.onBackPressed();
    }

    private void readViewValues() {
        for (Map.Entry<FormField, FieldTextEditView> entry: fieldsToView.entrySet()) {
            FormField formField = entry.getKey();
            FieldTextEditView fieldTextEditView = entry.getValue();
            formField.setValue(fieldTextEditView.getValue());
        }
    }
}