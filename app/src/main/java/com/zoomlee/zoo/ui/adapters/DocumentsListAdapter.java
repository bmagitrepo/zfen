package com.zoomlee.zoo.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.incitations.Incitation;
import com.zoomlee.zoo.incitations.IncitationsController;
import com.zoomlee.zoo.net.model.Document;
import com.zoomlee.zoo.net.model.Person;
import com.zoomlee.zoo.net.model.User;
import com.zoomlee.zoo.ui.view.DocumentItemView;
import com.zoomlee.zoo.utils.SharedPreferenceUtils;

import java.util.List;


/**
 * Created by
 *
 * @author Evgen Marinin <ievgen.marinin@alterplay.com>
 * @date 03.02.15.
 */
public class DocumentsListAdapter extends ArrayAdapter<Document> implements IncitationsAdapter.Incitated {

    private static final int MIN_POSITION = 1;
    private static final int MAX_POSITION = 3;

    private boolean allPersons;
    private List<Person> personsList;
    private User user;
    private Document documentToOpen;
    private DocumentItemView.DocumentItemListener documentListener;
    private DocumentItemView selectedView;
    private IncitationsAdapter.Incitated incitated;
    private final IncitationsController controller = new IncitationsController();
    private boolean isShowIncitations;

    public DocumentsListAdapter(Context context, List<Document> items, boolean allPersons, List<Person> persons) {
        super(context, R.layout.item_document, items);
        this.allPersons = allPersons;
        if (allPersons) {
            this.personsList = persons;
            this.user = SharedPreferenceUtils.getUtils().getUserSettings();
        }
        updateIncitations();
    }

    private void updateIncitations() {
        incitated = controller.createIncitated(getCount(), IncitationsController.Screen.DOCUMENTS, MIN_POSITION, MAX_POSITION);
    }

    public void showIncitations(boolean isShow) {
        isShowIncitations = isShow;
        if(isShow) {
            notifyDataSetChanged();
        }
    }

    @Override
    public void notifyDataSetChanged() {
        // incitation change along with data changes, for example can disappear when too few items
        updateIncitations();

        super.notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            view = inflater.inflate(R.layout.item_document, parent, false);
        }

        Document item = getItem(position);
        DocumentItemView itemView = (DocumentItemView) view;
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
    public void setOpenDocument(Document documentToOpen, DocumentItemView.DocumentItemListener listener) {
        this.documentToOpen = documentToOpen;
        this.documentListener = listener;
    }

    public void setSelectedView(DocumentItemView selectedView) {
        this.selectedView = selectedView;
    }

    public DocumentItemView getSelectedView() {
        return selectedView;
    }

    @Override
    public int getIncitationPosition() {
        return isShowIncitations ? incitated.getIncitationPosition() : AdapterView.INVALID_POSITION;
    }

    @Override
    public Incitation getIncitation() {
        return incitated.getIncitation();
    }
}