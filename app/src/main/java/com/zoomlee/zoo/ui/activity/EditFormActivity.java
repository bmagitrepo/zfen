package com.zoomlee.zoo.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.TextView;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.dao.DaoHelper;
import com.zoomlee.zoo.dao.DaoHelpersContainer;
import com.zoomlee.zoo.net.model.Form;
import com.zoomlee.zoo.net.model.FormField;
import com.zoomlee.zoo.ui.view.field.FieldDateEditView;
import com.zoomlee.zoo.ui.view.field.FieldEditView;
import com.zoomlee.zoo.ui.view.field.FieldToggleView;
import com.zoomlee.zoo.utils.IntentUtils;
import com.zoomlee.zoo.utils.RequestCodes;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;

public class EditFormActivity extends SecuredActionBarActivity implements View.OnClickListener {

    public static final String EXTRA_FORM = "form_to_edit";

    public static final SparseIntArray VIEW_ID_TO_FIELD_ID = new SparseIntArray();
    static {
        VIEW_ID_TO_FIELD_ID.append(R.id.familyName, 1);
        VIEW_ID_TO_FIELD_ID.append(R.id.firstName, 2);
        VIEW_ID_TO_FIELD_ID.append(R.id.middleName, 3);
        VIEW_ID_TO_FIELD_ID.append(R.id.familyNumbers, 10);
        VIEW_ID_TO_FIELD_ID.append(R.id.usStreetAddress, 11);
        VIEW_ID_TO_FIELD_ID.append(R.id.city, 12);
        VIEW_ID_TO_FIELD_ID.append(R.id.state, 13);
        VIEW_ID_TO_FIELD_ID.append(R.id.passportIssuedBy, 14);
        VIEW_ID_TO_FIELD_ID.append(R.id.passportNumber, 15);
        VIEW_ID_TO_FIELD_ID.append(R.id.countryOfResidence, 16);
        VIEW_ID_TO_FIELD_ID.append(R.id.countriesVisited, 17);
        VIEW_ID_TO_FIELD_ID.append(R.id.flightNo, 18);
        VIEW_ID_TO_FIELD_ID.append(R.id.primaryPurpose, 19);
        VIEW_ID_TO_FIELD_ID.append(R.id.fruitsTv, 21);
        VIEW_ID_TO_FIELD_ID.append(R.id.meetsTv, 23);
        VIEW_ID_TO_FIELD_ID.append(R.id.snailsTv, 25);
        VIEW_ID_TO_FIELD_ID.append(R.id.soilTv, 27);
        VIEW_ID_TO_FIELD_ID.append(R.id.livestockTv, 29);
        VIEW_ID_TO_FIELD_ID.append(R.id.currencyTv, 31);
        VIEW_ID_TO_FIELD_ID.append(R.id.merchandiseTv, 33);
        VIEW_ID_TO_FIELD_ID.append(R.id.residents, 35);
        VIEW_ID_TO_FIELD_ID.append(R.id.visitors, 36);
    }

    @BindViews({R.id.familyName, R.id.firstName, R.id.middleName,
            R.id.birthDate, R.id.familyNumbers, R.id.usStreetAddress, R.id.city, R.id.state,
            R.id.passportIssuedBy, R.id.passportNumber, R.id.countryOfResidence, R.id.countriesVisited,
            R.id.flightNo, R.id.primaryPurpose, R.id.fruitsTv, R.id.meetsTv, R.id.snailsTv, R.id.soilTv,
            R.id.livestockTv, R.id.currencyTv, R.id.merchandiseTv, R.id.residents, R.id.visitors})
    FieldEditView[] fields;
    @BindView(R.id.descriptionOfArticles)
    TextView descriptionOfArticles;
    @BindViews({R.id.cancelFrame, R.id.submitFrame})
    View[] toolbarButtons;

    private Form form;

    public static void startToEditForm(Activity activity, Form form) {
        Intent intent = new Intent(activity, EditFormActivity.class);
        intent.putExtra(IntentUtils.EXTRA_OPEN_WITH_PIN, false);
        intent.putExtra(EXTRA_FORM, form);
        activity.startActivityForResult(intent, RequestCodes.EDIT_FORM_ACTIVITY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCustomizedView(R.layout.activity_edit_form, false);

        form = getIntent().getParcelableExtra(EXTRA_FORM);
        ButterKnife.bind(this);

        initUI();
    }

    private void initUI() {
        descriptionOfArticles.setOnClickListener(this);
        toolbarButtons[0].setOnClickListener(this);
        toolbarButtons[1].setOnClickListener(this);

        initValues();
    }

    private void initValues() {
        for (FieldEditView fieldEditView: fields) {
            if (fieldEditView instanceof FieldDateEditView) {
                try {
                    int month1 = Integer.valueOf(form.getData().get(FormField.BIRTH_DATE_MOUNTH1_TYPE_ID - 1).getValue());
                    int month2 = Integer.valueOf(form.getData().get(FormField.BIRTH_DATE_MOUNTH2_TYPE_ID - 1).getValue());
                    int day1 = Integer.valueOf(form.getData().get(FormField.BIRTH_DATE_DAY1_TYPE_ID - 1).getValue());
                    int day2 = Integer.valueOf(form.getData().get(FormField.BIRTH_DATE_DAY2_TYPE_ID - 1).getValue());
                    int year1 = Integer.valueOf(form.getData().get(FormField.BIRTH_DATE_YEAR1_TYPE_ID - 1).getValue());
                    int year2 = Integer.valueOf(form.getData().get(FormField.BIRTH_DATE_YEAR2_TYPE_ID - 1).getValue());

                    int month = month1 * 10 + month2;
                    int day = day1 * 10 + day2;
                    int year = year1 * 10 + year2;

                    Calendar calendar = Calendar.getInstance();
                    int currentYear = calendar.get(Calendar.YEAR);
                    if (currentYear % 100 < year)
                        currentYear -= 100;
                    year = (currentYear / 100) * 100 + year;
                    calendar.set(year, month - 1, day);
                    ((FieldDateEditView) fieldEditView).setValue(calendar);
                } catch (NumberFormatException e) {}
            } else {
                FormField formField = form.getData().get(VIEW_ID_TO_FIELD_ID.get(fieldEditView.getId()) - 1);
                fieldEditView.setValue(formField.getValue());
            }
        }
    }

    private void saveForm() {
        for (FieldEditView fieldEditView: fields) {
            if (fieldEditView instanceof FieldDateEditView) {
                Calendar calendar = ((FieldDateEditView) fieldEditView).getCalendarValue();
                int month = calendar.get(Calendar.MONTH) + 1;
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int year = calendar.get(Calendar.YEAR) % 100;

                FormField month1Field = form.getData().get(FormField.BIRTH_DATE_MOUNTH1_TYPE_ID - 1);
                FormField month2Field = form.getData().get(FormField.BIRTH_DATE_MOUNTH2_TYPE_ID - 1);
                FormField day1Field = form.getData().get(FormField.BIRTH_DATE_DAY1_TYPE_ID - 1);
                FormField day2Field = form.getData().get(FormField.BIRTH_DATE_DAY2_TYPE_ID - 1);
                FormField year1Field = form.getData().get(FormField.BIRTH_DATE_YEAR1_TYPE_ID - 1);
                FormField year2Field = form.getData().get(FormField.BIRTH_DATE_YEAR2_TYPE_ID - 1);

                month1Field.setValue(String.valueOf(month / 10));
                month2Field.setValue(String.valueOf(month % 10));
                day1Field.setValue(String.valueOf(day / 10));
                day2Field.setValue(String.valueOf(day % 10));
                year1Field.setValue(String.valueOf(year / 10));
                year2Field.setValue(String.valueOf(year % 10));
            } else if (fieldEditView instanceof FieldToggleView) {
                FormField yesField = form.getData().get(VIEW_ID_TO_FIELD_ID.get(fieldEditView.getId()) - 1);
                FormField noField = form.getData().get(VIEW_ID_TO_FIELD_ID.get(fieldEditView.getId()));
                boolean checked = ((FieldToggleView) fieldEditView).isChecked();
                yesField.setValue(checked ? "X" : "");
                noField.setValue(checked ? "" : "X");
            } else {
                FormField formField = form.getData().get(VIEW_ID_TO_FIELD_ID.get(fieldEditView.getId()) - 1);
                formField.setValue(fieldEditView.getValue());
            }
        }

        DaoHelper<Form> formDaoHelper = DaoHelpersContainer.getInstance().getDaoHelper(Form.class);
        formDaoHelper.saveLocalChanges(this, form);

        Intent intent = new Intent();
        intent.putExtra(EditFormActivity.EXTRA_FORM, form);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.descriptionOfArticles:
                FormsArticlesActivity.startToEditForm(this, form);
                break;
            case R.id.cancelFrame:
                finish();
                break;
            case R.id.submitFrame:
                saveForm();
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestCodes.FORM_ARTICLES_ACTIVITY) {
            unpin();

            if (resultCode == Activity.RESULT_OK) {
                Form newForm = data.getParcelableExtra(EXTRA_FORM);
                if (newForm != null) form = newForm;
            }
        }
    }
}
