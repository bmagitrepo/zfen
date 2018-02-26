package com.zoomlee.zoo.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.dao.DaoHelper;
import com.zoomlee.zoo.dao.DaoHelpersContainer;
import com.zoomlee.zoo.utils.Events;
import com.zoomlee.zoo.net.model.Person;
import com.zoomlee.zoo.ui.adapters.PersonAdapter;
import com.zoomlee.zoo.utils.DeveloperUtil;
import com.zoomlee.zoo.utils.IntentUtils;
import com.zoomlee.zoo.utils.RequestCodes;
import com.zoomlee.zoo.utils.SharedPreferenceUtils;

import java.util.List;

import de.greenrobot.event.EventBus;

public class PersonListActivity extends SecuredActionBarActivity implements AdapterView.OnItemClickListener, View.OnClickListener {

    public static final String EXTRA_PERSON_SELECTED = "zoomlee_extra_selected_person";
    private static final String EXTRA_MANAGE_PERSONS = "manage_persons";

    private boolean managePersonsState;
    private List<Person> persons;
    private Person selectedPerson;
    private ListView personList;
    private PersonAdapter adapter;
    private LoadDataAsyncTask loadTask;

    public static void startToSelectPerson(Activity activity, Person selectedPerson) {
        Intent intent = new Intent(activity, PersonListActivity.class);
        intent.putExtra(IntentUtils.EXTRA_OPEN_WITH_PIN, false);
        intent.putExtra(EXTRA_MANAGE_PERSONS, false);
        intent.putExtra(EXTRA_PERSON_SELECTED, selectedPerson);
        activity.startActivityForResult(intent, RequestCodes.SELECT_PERSON);
    }

    public static void startForMangerPersons(Activity activity) {
        Intent intent = new Intent(activity, PersonListActivity.class);
        intent.putExtra(IntentUtils.EXTRA_OPEN_WITH_PIN, false);
        intent.putExtra(EXTRA_MANAGE_PERSONS, true);
        activity.startActivityForResult(intent, RequestCodes.MANAGE_PERSONS);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_person_list);
        initUi();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(managePersonsState ? R.string.title_activity_manage_persons : R.string.title_activity_select_person);
        updateABAvatar(null);
    }

    private void initUi() {
        managePersonsState = getIntent().getBooleanExtra(EXTRA_MANAGE_PERSONS, false);
        selectedPerson = getIntent().getParcelableExtra(EXTRA_PERSON_SELECTED);
        persons = initPersons();
        personList = (ListView) findViewById(R.id.personList);
        personList.setChoiceMode(managePersonsState
                ? ListView.CHOICE_MODE_NONE
                : ListView.CHOICE_MODE_SINGLE);

        View addFamilyMemberLayout = getLayoutInflater().inflate(R.layout.add_member_btn, null);
        personList.addFooterView(addFamilyMemberLayout);
        Button addFamilyMemberBtn = (Button) addFamilyMemberLayout.findViewById(R.id.addFamilyMemberBtn);
        addFamilyMemberBtn.setOnClickListener(this);

        adapter = new PersonAdapter(this, persons);
        personList.setAdapter(adapter);
        personList.setOnItemClickListener(this);
        personList.setDivider(null);
        personList.setItemChecked(getSelectedPersonPosition(), true);
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
    }

    private void loadData() {
        cancelTask();
        loadTask = new LoadDataAsyncTask();
        loadTask.execute();
    }

    private void cancelTask() {
        if (loadTask != null) {
            loadTask.cancel(true);
            loadTask = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RequestCodes.CREATE_PERSONS:
            case RequestCodes.EDIT_PERSON:
                unpin();
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        DeveloperUtil.michaelLog();
        Person selectedPerson = persons.get(position);
        if (!managePersonsState) {
            personList.setItemChecked(position, true);
            Intent returnedIntent = new Intent();
            returnedIntent.putExtra(EXTRA_PERSON_SELECTED, selectedPerson);
            setResult(RESULT_OK, returnedIntent);
            finish();
        } else {
            CreateEditPersonActivity.startToEditPerson(this, selectedPerson);
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

    private List<Person> initPersons() {
        DaoHelper<Person> daoHelper = DaoHelpersContainer.getInstance().getDaoHelper(Person.class);
        List<Person> persons = daoHelper.getAllItems(this);
        if (!managePersonsState)
            persons.add(0, SharedPreferenceUtils.getUtils().getUserSettings());
        return persons;
    }

    private int getSelectedPersonPosition() {
        int selectedPersonId = -2;
        if (selectedPerson != null) selectedPersonId = selectedPerson.getId();
        for (int i = 0; i < persons.size(); i++) {
            if (persons.get(i).getId() == selectedPersonId) return i;
        }

        return -1;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addFamilyMemberBtn:
                CreateEditPersonActivity.startToCreatePerson(this);
                break;
            default:
                break;
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(Events.PersonChanged event) {
        loadData();
    }

    private class LoadDataAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            persons = initPersons();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            adapter.clear();
            int position = getSelectedPersonPosition();
            if (position > -1) {
                personList.setItemChecked(position, true);
            }
            adapter.addAll(persons);
            adapter.notifyDataSetChanged();
        }
    }
}
