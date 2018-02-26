package com.zoomlee.zoo.scopes.data.person;

import android.content.Context;
import android.os.AsyncTask;

import com.zoomlee.zoo.dao.DaoHelper;
import com.zoomlee.zoo.dao.DaoHelpersContainer;
import com.zoomlee.zoo.net.model.Person;
import com.zoomlee.zoo.utils.SharedPreferenceUtils;

import java.util.ArrayList;
import java.util.List;

public class AllPersonsTask extends AsyncTask<Void, Void, List<Person>> {

    private final DaoHelper<Person> daoPersons = DaoHelpersContainer.getInstance().getDaoHelper(Person.class);
    private final Context context;

    public AllPersonsTask(Context context) {
        this.context = context;
    }

    @Override
    protected List<Person> doInBackground(Void... params) {
        return listPersons();
    }

    public List<Person> listPersons() {
        List<Person> allPersons = new ArrayList<>();
        allPersons.add(SharedPreferenceUtils.getUtils().getUserSettings());
        allPersons.addAll(daoPersons.getAllItems(context));
        return allPersons;
    }
}
