package com.zoomlee.zoo.scopes.data.document;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.zoomlee.zoo.dao.DaoHelper;
import com.zoomlee.zoo.dao.DaoHelpersContainer;
import com.zoomlee.zoo.net.model.Document;
import com.zoomlee.zoo.net.model.Person;
import com.zoomlee.zoo.provider.helpers.DocumentsHelper;
import com.zoomlee.zoo.utils.DBUtil;

import java.util.ArrayList;
import java.util.List;

public class DocumentsByCategoryTask extends AsyncTask<PersonAndCategory, Document, List<Document>> {

    private final DaoHelper<Document> daoDocuments = DaoHelpersContainer.getInstance().getDaoHelper(Document.class);
    private final Context context;

    public DocumentsByCategoryTask(Context context) {
        this.context = context;
    }

    @Override
    public List<Document> doInBackground(@NonNull PersonAndCategory... personAndCategory) {
        return listDocumentsByCategory(personAndCategory[0].personId, personAndCategory[0].categoryId);
    }

    public List<Document> listDocumentsByCategory(int personId, int categoryId) {
        ContentResolver resolver = context.getContentResolver();
        String selection;
        String[] args;
        if (personId == Person.ALL_ID) {
            selection = DocumentsHelper.FullDocumentsContract.CATEGORY_ID + " = ? AND " + DocumentsHelper.FullDocumentsContract.STATUS + "=1";
            args = DBUtil.getArgsArray(categoryId);
        } else {
            selection = DocumentsHelper.FullDocumentsContract.CATEGORY_ID + " = ? AND " + DocumentsHelper.FullDocumentsContract.STATUS + "=1 AND " +
                    DocumentsHelper.FullDocumentsContract.PERSON_ID + " = ?";
            args = DBUtil.getArgsArray(categoryId, personId);
        }
        Cursor cursor = resolver.query(DocumentsHelper.FullDocumentsContract.FULL_DATA_URI, null,
                selection, args, DocumentsHelper.FullDocumentsContract.UPDATE_TIME + " DESC");

        if (cursor == null) return new ArrayList<>();

        List<Document> documents = daoDocuments.readItems(context, cursor, new DaoHelper.OnItemLoadedListener<Document>() {
            @Override
            public void onItemLoaded(Document item) {
                publishProgress(item);
            }
        });
        cursor.close();
        return documents;
    }
}
