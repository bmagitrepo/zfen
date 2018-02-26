package com.zoomlee.zoo.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.cocosw.undobar.UndoBarController;
import com.zoomlee.zoo.R;
import com.zoomlee.zoo.ZoomleeApp;
import com.zoomlee.zoo.dao.DaoHelper;
import com.zoomlee.zoo.dao.DaoHelpersContainer;
import com.zoomlee.zoo.dao.TagsDocDaoHelper;
import com.zoomlee.zoo.net.model.Category;
import com.zoomlee.zoo.net.model.Document;
import com.zoomlee.zoo.net.model.Person;
import com.zoomlee.zoo.net.model.Tag;
import com.zoomlee.zoo.scopes.data.document.DocumentsByCategoryTask;
import com.zoomlee.zoo.scopes.data.document.PersonAndCategory;
import com.zoomlee.zoo.ui.adapters.DocumentsListAdapter;
import com.zoomlee.zoo.ui.adapters.IncitationsAdapter;
import com.zoomlee.zoo.ui.fragments.dialog.AddTagFragment;
import com.zoomlee.zoo.ui.view.DocumentItemView;
import com.zoomlee.zoo.utils.DeveloperUtil;
import com.zoomlee.zoo.utils.Events;
import com.zoomlee.zoo.utils.GAEvents;
import com.zoomlee.zoo.utils.GAUtil;
import com.zoomlee.zoo.utils.IntentUtils;
import com.zoomlee.zoo.utils.RequestCodes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import de.greenrobot.event.EventBus;

public class DocumentsActivity extends SecuredActionBarActivity implements AdapterView.OnItemClickListener,
        View.OnTouchListener, AdapterView.OnItemLongClickListener, DocumentItemView.DocumentItemListener,
        UndoBarController.AdvancedUndoListener {

    public static final String DIALOG_TAG = "DialogFragment";
    protected DialogFragment fragment;

    private static final String POSITION = "position";
    private static final String DOCUMENT = "document";
    private static final String CATEGORY_ID_KEY = "category_id";
    private static final String PERSON_ID_KEY = "person_id";
    private static final String TAG_ID_KEY = "tag_id";

    private DocumentsListAdapter adapter;
    private int remoteCategoryId = -1;
    private int localTagId = -1;
    private int localPersonId;
    private DaoHelper<Document> daoHelper;
    private LoadDataAsyncTask loadTask;
    private boolean allPerson;
    private Comparator<Document> comparator = new UpdateTimeComparator();
    private ListView listView;
    private int scrollY = -1;
    private int scrolledPosition;

    private AddTagFragment.OnSelectListener listener = new AddTagFragment.OnSelectListener() {
        @Override
        public void onAddTagToNew() {
            CreateEditDocActivity.startWithTag(DocumentsActivity.this, localTagId);
            closeDialog();
        }

        @Override
        public void onAddTagToExist() {
            SelectDocumentsActivity.startActivity(DocumentsActivity.this, localTagId);
            closeDialog();
        }
    };

    public static void startActivityWithCategory(Activity activity, int remoteCategoryId, int localPersonId) {
        Intent intent = getIntentForStart(activity, remoteCategoryId, localPersonId, -1, false);
        activity.startActivityForResult(intent, RequestCodes.DOCUMENTS_ACTIVITY);
    }

    public static void startActivityWithTag(Activity activity, int localTagId, int localPersonId) {
        Intent intent = getIntentForStart(activity, -1, localPersonId, localTagId, false);
        activity.startActivityForResult(intent, RequestCodes.DOCUMENTS_ACTIVITY);
    }

    public static Intent getIntentForStart(Context context, int remoteCategoryId, int localPersonId, int localTagId, boolean openWithPin) {
        Intent intent = new Intent(context, DocumentsActivity.class);
        intent.putExtra(IntentUtils.EXTRA_OPEN_WITH_PIN, openWithPin);
        intent.putExtra(CATEGORY_ID_KEY, remoteCategoryId);
        intent.putExtra(PERSON_ID_KEY, localPersonId);
        intent.putExtra(TAG_ID_KEY, localTagId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_documents);

        remoteCategoryId = getIntent().getIntExtra(CATEGORY_ID_KEY, -1);
        localTagId = getIntent().getIntExtra(TAG_ID_KEY, -1);
        localPersonId = getIntent().getIntExtra(PERSON_ID_KEY, Person.ALL_ID);
        daoHelper = DaoHelpersContainer.getInstance().getDaoHelper(Document.class);
        DaoHelper<Person> personDaoHelper = DaoHelpersContainer.getInstance().getDaoHelper(Person.class);

        listView = (ListView) findViewById(R.id.listView);

        ZoomleeApp app = (ZoomleeApp) getApplication();
        allPerson = app.getSelectedPersonId() == Person.ALL_ID;
        adapter = new DocumentsListAdapter(this, new ArrayList<Document>(),
                allPerson,
                personDaoHelper.getAllItems(this));

        listView.setAdapter(IncitationsAdapter.wrap(adapter, adapter));
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);
        listView.setOnTouchListener(this);

        findViewById(R.id.addNewBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDocument();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        updateABAvatar();

        if (isDocsByCategory()) {
            DaoHelper<Category> categoryDaoHelper = DaoHelpersContainer.getInstance().getDaoHelper(Category.class);
            Category category = categoryDaoHelper.getItemByRemoteId(this, remoteCategoryId);
            setActivityTitle(category.getName());
            GAUtil.getUtil().screenAccess(GAEvents.ACTION_CATEGORY_TYPE, category.getName());
            GAUtil.getUtil().timeSpent(GAEvents.ACTION_DOCUMENT_LIST);
        } else {
            DaoHelper<Tag> categoryDaoHelper = DaoHelpersContainer.getInstance().getDaoHelper(Tag.class);
            Tag tag = categoryDaoHelper.getItemByLocalId(this, localTagId);
            if (tag != null) setActivityTitle(tag.getName());
            else setActivityTitle(getString(R.string.title_no_tag));
        }
        if (isDocsByTag())
            fragment = AddTagFragment.newInstance(listener);
    }

    private void setActivityTitle(String titile) {
        if (titile.length() > 0)
            titile = titile.substring(0,1).toUpperCase() + titile.substring(1);
        getSupportActionBar().setTitle(titile);
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        loadDocuments();
    }

    /**
     * Starts loading documents from the data source.
     */
    private void loadDocuments() {
        cancelTask();
        loadTask = new LoadDataAsyncTask(DocumentsActivity.this);
        loadTask.execute(new PersonAndCategory(localPersonId, remoteCategoryId));
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
        cancelTask();

        // document should be deleted
        UndoBarController.clear(this);
    }

    private void cancelTask() {
        if (loadTask != null) {
            loadTask.cancel(true);
            loadTask = null;
        }
    }

    private void createDocument() {
        if (isDocsByTag() && localTagId != TagsDocDaoHelper.TagsDocAlerts.NO_TAG_ID)
            addTagToDocument();
        else
            CreateEditDocActivity.startActivity(this);
    }

    private void addTagToDocument() {
        FragmentManager fragmentManager = getSupportFragmentManager();

        fragment.show(fragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_in_top, R.anim.slide_out_bottom), DIALOG_TAG);
    }

    public void closeDialog() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        DialogFragment fragment = (DialogFragment) fragmentManager.findFragmentByTag(DIALOG_TAG);
        if (fragment != null) {
            fragment.dismiss();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RequestCodes.DOCUMENT_DETAILS_ACTIVITY
                || requestCode == RequestCodes.CREATE_DOCUMENT
                || requestCode == RequestCodes.EDIT_DOCUMENT
                || requestCode == RequestCodes.INCITATION_ACTIVITY) {
            unpin();
        }
        if (requestCode == RequestCodes.EDIT_DOCUMENT && resultCode == RESULT_OK) {
            DocumentItemView selectedView = adapter.getSelectedView();
            Document document = selectedView.getDocument();
            DocumentDetailsActivity.startActivity(this, document.getId());
            selectedView.closeActions(true);
            adapter.setSelectedView(null);
        }
    }

    public void onEventMainThread(final Events.DocumentChanged event) {
        if (adapter.getSelectedView() != null) {
            // when adapter will be rebuilt, we have to reopen previously opened document
            Document documentToOpen = adapter.getSelectedView().getDocument();
            if (!event.getDocument().equals(documentToOpen) || event.getStatus() != Events.DocumentChanged.DELETED) {
                adapter.setOpenDocument(documentToOpen, this);
            }
        }

        switch (event.getStatus()) {
            case Events.DocumentChanged.UPDATED:
                onDocumentUpdated(event);
                break;
            case Events.DocumentChanged.DELETED:
                onDocumentDeleted(event);
                break;
            default:
                break;
        }

        if (adapter.isEmpty()) {
            DocumentsActivity.this.finish();
        }
    }

    private void onDocumentUpdated(Events.DocumentChanged event) {
        Document eventDoc = event.getDocument();
        if ((allPerson || eventDoc.getLocalPersonId() == localPersonId)
                && (!isDocsByCategory() || eventDoc.getCategoryId() == remoteCategoryId)
                && (!isDocsByTag() || eventDoc.haveTag(localTagId)))
            addDoc(eventDoc);
        else
            adapter.remove(eventDoc);
    }

    private void addDoc(Document doc) {
        adapter.remove(doc);

        Document newDoc = daoHelper.getItemByLocalId(this, doc.getId());
        if (newDoc != null) {
            adapter.add(newDoc);
            adapter.sort(comparator);
        }
    }

    private void onDocumentDeleted(Events.DocumentChanged event) {
        adapter.remove(event.getDocument());
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        DeveloperUtil.michaelLog("item click");
        if (adapter.getSelectedView() != null) {
            adapter.setSelectedView(null); // deselect actions
            return;
        }
        Document document = (Document) parent.getAdapter().getItem(position);
        DocumentDetailsActivity.startActivity(this, document.getId());
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && adapter.getSelectedView() != null) {
            // we close actions on any touch
            DeveloperUtil.michaelLog("close " + adapter.getSelectedView());

            adapter.getSelectedView().closeActions(false);
        }
        return false;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (position != adapter.getIncitationPosition()) {
            openActionsForView(view);
        }
        return true;
    }

    private void openActionsForView(View view) {
        DeveloperUtil.michaelLog("open for " + view);

        adapter.setSelectedView((DocumentItemView) view);
        adapter.getSelectedView().openActions(this);
    }

    @Override
    public void onItemDelete(Document document) {
        // previous document lost
        UndoBarController.clear(this);

        // remember what we deleted to be able to restore it
        Bundle bundle = new Bundle();
        bundle.putParcelable(DOCUMENT, document);
        bundle.putInt(POSITION, adapter.getPosition(document));

        adapter.remove(document);

        new UndoBarController.UndoBar(this)
                .listener(this)
                .token(bundle)
                .message(R.string.message_document_deleted)
                .show();
    }

    @Override
    public void onManageTags(Document document) {
        CreateEditDocActivity.startManageTags(this, document.getId());
    }

    @Override
    public void onUndo(Parcelable parcelable) {
        // restore item
        Bundle bundle = (Bundle) parcelable;
        int position = bundle.getInt(POSITION);
        Document document = bundle.getParcelable(DOCUMENT);

        adapter.insert(document, position);
    }

    @Override
    public void onHide(Parcelable parcelable) {
        // delete item
        deleteDocumentIn(parcelable);
    }

    @Override
    public void onClear(@NonNull Parcelable[] parcelables) {
        for (Parcelable parcelable : parcelables) {
            deleteDocumentIn(parcelable);
        }
    }

    /**
     * Deletes document that is contained in the given parcel.
     *
     * @param parcelable containing document to be deleted
     */
    private void deleteDocumentIn(Parcelable parcelable) {
        Bundle bundle = (Bundle) parcelable;
        Document document = bundle.getParcelable(DOCUMENT);

        DaoHelper<Document> daoHelper = DaoHelpersContainer.getInstance().getDaoHelper(Document.class);
        daoHelper.deleteItem(this, document);
    }

    private class LoadDataAsyncTask extends DocumentsByCategoryTask {

        public LoadDataAsyncTask(Context context) {
            super(context);
        }

        @Override
        protected void onPreExecute() {
            if (!adapter.isEmpty() && scrollY == -1) {
                scrolledPosition = listView.getFirstVisiblePosition();
                View childView = listView.getChildAt(0);
                scrollY = childView.getTop();
            }
            adapter.clear();
            adapter.showIncitations(false);
        }

        @Override
        public List<Document> doInBackground(@NonNull PersonAndCategory... personAndCategory) {
            if (isDocsByCategory())
                return super.doInBackground(personAndCategory);
            else if (isDocsByTag())
                return new TagsDocDaoHelper(getApplicationContext()).getDocumentsByTag(localTagId, localPersonId, new DaoHelper.OnItemLoadedListener<Document>() {
                    @Override
                    public void onItemLoaded(Document item) {
                        publishProgress(item);
                    }
                });
            else
                return new ArrayList<>();
        }

        @Override
        protected void onProgressUpdate(Document... document) {
            adapter.addAll(document);
            adapter.sort(comparator);
            adapter.notifyDataSetChanged();
        }

        @Override
        protected void onPostExecute(List<Document> documents) {
            if (adapter.isEmpty())
                finish();
            else if (scrollY != -1) {
                listView.setSelectionFromTop(scrolledPosition, scrollY);
                scrollY = -1;
            }
            adapter.showIncitations(true);
        }
    }

    private static class UpdateTimeComparator implements Comparator<Document> {
        @Override
        public int compare(Document lhs, Document rhs) {
            return rhs.getUpdateTime() - lhs.getUpdateTime();
        }
    }

    private boolean isDocsByTag() {
        return localTagId != -1 && remoteCategoryId == -1;
    }

    private boolean isDocsByCategory() {
        return localTagId == -1 && remoteCategoryId != -1;
    }
}
