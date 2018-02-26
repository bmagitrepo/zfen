package com.zoomlee.zoo.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.dao.DaoHelper;
import com.zoomlee.zoo.dao.DaoHelpersContainer;
import com.zoomlee.zoo.net.model.Form;
import com.zoomlee.zoo.net.model.Person;
import com.zoomlee.zoo.net.model.User;
import com.zoomlee.zoo.ui.view.FormItemView;
import com.zoomlee.zoo.utils.SharedPreferenceUtils;

import java.util.List;


/**
 * Created by
 *
 * @author Evgen Marinin <ievgen.marinin@alterplay.com>
 * @date 27.05.15.
 */
public class FormsListAdapter extends ArrayAdapter<Form> {

    private boolean allPersons;
    private List<Person> personsList;
    private User user;
    private Form documentToOpen;
    private FormItemView.FormItemListener documentListener;
    private FormItemView selectedView;
    private DaoHelper<Person> personsDaoHelper;

    public FormsListAdapter(Context context, List<Form> items, boolean allPersons) {
        super(context, R.layout.item_form, items);
        personsDaoHelper = DaoHelpersContainer.getInstance().getDaoHelper(Person.class);

        setAllPersons(allPersons);
    }

    public void setAllPersons(boolean allPersons) {
        this.allPersons = allPersons;
        if (allPersons) {
            this.personsList = personsDaoHelper.getAllItems(getContext());
            this.user = SharedPreferenceUtils.getUtils().getUserSettings();
        }
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            view = inflater.inflate(R.layout.item_form, parent, false);
        }

        Form item = getItem(position);
        FormItemView itemView = (FormItemView) view;
        itemView.bind(item, allPersons, user, personsList);

        if (item.equals(documentToOpen)) {
            itemView.openActions(true, documentListener);
            selectedView = itemView;
            // do this only once
            documentToOpen = null;
            documentListener = null;
        }

        return view;
    }

    /**
     * Sets document to be opened after binded.
     *
     * @param documentToOpen document
     * @param listener       to handle actions
     */
    public void setOpenForm(Form documentToOpen, FormItemView.FormItemListener listener) {
        this.documentToOpen = documentToOpen;
        this.documentListener = listener;
    }

    public void setSelectedView(FormItemView selectedView) {
        this.selectedView = selectedView;
    }

    public FormItemView getSelectedView() {
        return selectedView;
    }
}