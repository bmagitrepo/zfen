package com.zoomlee.zoo.ui.activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.zoomlee.zoo.R;
import com.zoomlee.zoo.ZoomleeApp;
import com.zoomlee.zoo.dao.DaoHelper;
import com.zoomlee.zoo.dao.DaoHelpersContainer;
import com.zoomlee.zoo.dao.TagsDocDaoHelper;
import com.zoomlee.zoo.net.model.Document;
import com.zoomlee.zoo.net.model.Person;
import com.zoomlee.zoo.net.model.Tag;
import com.zoomlee.zoo.provider.helpers.DocumentsHelper;
import com.zoomlee.zoo.ui.adapters.DocumentsAdapter;
import com.zoomlee.zoo.utils.IntentUtils;
import com.zoomlee.zoo.utils.RequestCodes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by
 *
 * @author Michael Oknov <misha.oknov@alterplay.com>
 * @since 4/28/15
 */
public class SelectDocumentsActivity extends SecuredActionBarActivity implements DocumentsAdapter.OnClickDocumentListener {
    private static final String EXTRA_TAG_ID = "zoomlee_extra_tag_id";
    private DocumentsAdapter adapter = new DocumentsAdapter();
    private MenuItem menuItem;
    private int personId;
    private Tag currentTag;

    public static void startActivity(Activity activity, int localTagId) {
        Intent i = new Intent(activity, SelectDocumentsActivity.class);
        i.putExtra(IntentUtils.EXTRA_OPEN_WITH_PIN, false);
        i.putExtra(EXTRA_TAG_ID, localTagId);
        activity.startActivityForResult(i, RequestCodes.SELECT_DOCUMENTS);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ZoomleeApp app = (ZoomleeApp) getApplication();
        personId = app.getSelectedPersonId();
        int localTagId = getIntent().getIntExtra(EXTRA_TAG_ID, -1);
        DaoHelper<Tag> daoHelper = DaoHelpersContainer.getInstance().getDaoHelper(Tag.class);
        currentTag = daoHelper.getItemByLocalId(this, localTagId);
        setContentView(R.layout.activity_select_documents);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.documentsRv);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(adapter);
        adapter.setListener(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        updateABAvatar();
        new GetDocumentsTask().execute();
    }

    @Override
    public void onClick(int position, boolean newSelected) {
        int selectedDocsCound = adapter.getSelectedDocIds().size();
        menuItem.setVisible(selectedDocsCound > 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_select_docs, menu);
        menuItem = menu.findItem(R.id.action_save);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                updateDocuments();
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateDocuments() {
        HashSet<Integer> selectedDocIds = adapter.getSelectedDocIds();
        if (selectedDocIds.isEmpty())
            return;
        List<Document> updateDocs = new ArrayList<>(selectedDocIds.size());
        List<Document> allDocs = adapter.getData();
        for (Document doc : allDocs)
            if (selectedDocIds.contains(doc.getId()))
                updateDocs.add(doc);

        DaoHelper<Document> documentDaoHelper = DaoHelpersContainer.getInstance().getDaoHelper(Document.class);
        for (Document doc : updateDocs) {
            doc.getTagsList().add(currentTag);
            documentDaoHelper.saveLocalChanges(this, doc);
        }

    }

    private class GetDocumentsTask extends AsyncTask<Void, Document, List<Document>> {
        private final ContentResolver resolver;
        private final Context ctx;
        private final DaoHelper<Document> daoDocuments;

        GetDocumentsTask() {
            resolver = getContentResolver();
            ctx = SelectDocumentsActivity.this;
            daoDocuments = DaoHelpersContainer.getInstance().getDaoHelper(Document.class);
        }

        @Override
        protected List<Document> doInBackground(Void... params) {
            if (personId == Person.ALL_ID) {
                DaoHelper<Person> personDaoHelper = DaoHelpersContainer.getInstance().getDaoHelper(Person.class);
                List<Person> persons = personDaoHelper.getAllItems(ctx);
                adapter.setPersons(persons);
            }
            TagsDocDaoHelper tagsDocDaoHelper = new TagsDocDaoHelper(ctx);
            List<Document> taggedDocs = tagsDocDaoHelper.getDocumentsByTag(currentTag.getId(), personId, null);
            StringBuilder notIn = new StringBuilder("( ");
            for (Document doc : taggedDocs)
                notIn.append(doc.getId()).append(", ");
            notIn.delete(notIn.length() - 2, notIn.length()).append(" )");

            String selection = DocumentsHelper.FullDocumentsContract.STATUS + "=1 AND " + DocumentsHelper.FullDocumentsContract._ID + " NOT IN " + notIn.toString();
            String[] args = null;
            if (personId != Person.ALL_ID) {
                selection += " AND " + DocumentsHelper.FullDocumentsContract.PERSON_ID + " = ?";
                args = new String[]{String.valueOf(personId)};
            }
            Cursor cursor = resolver.query(DocumentsHelper.FullDocumentsContract.FULL_DATA_URI, null,
                    selection, args, DocumentsHelper.FullDocumentsContract.UPDATE_TIME + " DESC");

            if (cursor == null) return new ArrayList<>();

            List<Document> documents = daoDocuments.readItems(ctx, cursor, new DaoHelper.OnItemLoadedListener<Document>() {
                @Override
                public void onItemLoaded(Document item) {
                    publishProgress(item);
                }
            });
            cursor.close();
            return documents;
        }

        @Override
        protected void onProgressUpdate(Document... values) {
            for (Document doc : values)
                adapter.add(doc);
        }
    }

}
