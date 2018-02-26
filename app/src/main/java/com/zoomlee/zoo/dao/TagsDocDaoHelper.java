package com.zoomlee.zoo.dao;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.zoomlee.zoo.net.model.Document;
import com.zoomlee.zoo.net.model.Person;
import com.zoomlee.zoo.net.model.Tag;
import com.zoomlee.zoo.provider.helpers.TagsProviderHelper.TagsDocAlertsContract;
import com.zoomlee.zoo.utils.DBUtil;
import com.zoomlee.zoo.utils.TimeUtil;
import com.zoomlee.zoo.utils.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.zoomlee.zoo.provider.helpers.DocumentsHelper.DocumentsContract;
import static com.zoomlee.zoo.provider.helpers.DocumentsHelper.FullDocumentsContract;
import static com.zoomlee.zoo.provider.helpers.DocumentsHelper.TagDocumentsContract;
import static com.zoomlee.zoo.provider.helpers.Tags2DocumentsProviderHelper.Tags2DocumentsContract;

/**
 * Created by
 *
 * @author Evgen Marinin <ievgen.marinin@alterplay.com>
 * @date 03.02.15.
 */
public class TagsDocDaoHelper {

    private Context context;

    public TagsDocDaoHelper(Context context) {
        this.context = context;
    }

    /**
     * To get documents without tag use {@linkplain TagsDocAlerts#NO_TAG_ID} as parameter
     * @param localTagId
     * @return
     */
    public List<Document> getDocumentsByTag(int localTagId) {
        return getDocumentsByTag(localTagId, Person.ALL_ID, null);
    }

    /**
     * To get documents without tag use {@linkplain TagsDocAlerts#NO_TAG_ID} as parameter
     * @param localTagId
     * @param localPersondId
     * @return
     */
    public List<Document> getDocumentsByTag(int localTagId, int localPersondId, DaoHelper.OnItemLoadedListener listener) {
        List<Document> result;
        ContentResolver contentResolver = context.getContentResolver();
        String selection = TagDocumentsContract.STATUS + "=1";
        String[] args;
        if (localPersondId == Person.ALL_ID) {
            args = DBUtil.getArgsArray(localTagId);
        } else {
            selection += " AND " + TagDocumentsContract.PERSON_ID + "=?";
            args = DBUtil.getArgsArray(localTagId, localPersondId);
        }
        Uri uri = TagDocumentsContract.CONTENT_URI;

        if (localTagId == TagsDocAlerts.NO_TAG_ID) {
            uri = FullDocumentsContract.FULL_DATA_URI;
            args = localPersondId == Person.ALL_ID ? null : DBUtil.getArgsArray(localPersondId);
            selection = getNoTaggedDocSelection(selection);
        }

        DaoHelper<Document> documentDaoHelper = DaoHelpersContainer.getInstance().getDaoHelper(Document.class);
        Cursor cursor = contentResolver.query(uri, null, selection, args, null);
        result = documentDaoHelper.readItems(context, cursor, listener);
        cursor.close();
        return result;
    }

    private String getNoTaggedDocSelection(String selection) {
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(Tags2DocumentsContract.CONTENT_URI, DBUtil.getArgsArray(Tags2DocumentsContract.DOCUMENT_ID),
                " 1=1 ) GROUP BY (" + Tags2DocumentsContract.DOCUMENT_ID, null, null); //SQL injection

        if (cursor.getCount() == 0) {
            cursor.close();
            return selection;
        }

        String[] args = new String[cursor.getCount()];
        for (int i = 0; i < args.length; i++) {
            cursor.moveToNext();
            args[i] = cursor.getString(0);
        }
        cursor.close();

        return selection + " AND " + FullDocumentsContract._ID + " NOT IN " + DBUtil.formatArgsAsSet(args);
    }

    /**
     * get TagsDocAlerts person by localId
     * (for user set personId = {@linkplain com.zoomlee.zoo.net.model.Person#ME_ID},
     * for all persons use {@linkplain com.zoomlee.zoo.net.model.Person#ALL_ID})
     *
     * @param personId
     * @return
     */
    public List<TagsDocAlerts> getTagsDocAlerts(int personId) {
        int timestamp = (int) TimeUtil.getServerEndDayTimestamp();

        ContentResolver contentResolver = context.getContentResolver();
        String selection = DocumentsContract.TABLE_NAME + "." + DocumentsContract.STATUS + "=" + Document.STATUS_NORMAL
                + " AND (" + TagsDocAlertsContract.TAGS_STATUS + "=" + Tag.STATUS_NORMAL
                + " OR " + TagsDocAlertsContract.TAGS_STATUS + " IS NULL)";
        String[] args;
        if (personId == Person.ALL_ID) {
            args = DBUtil.getArgsArray(timestamp);
        } else {
            selection += " AND " + DocumentsContract.PERSON_ID + " = ?";
            args = DBUtil.getArgsArray(timestamp, personId);
        }

        Cursor cursor = contentResolver.query(TagsDocAlertsContract.URI, null, selection,
                args, TagsDocAlertsContract.TAGS_ID + " DESC, " +
                        DocumentsContract.TABLE_NAME + "." + DocumentsContract.UPDATE_TIME + " DESC");
        if (cursor == null) return new ArrayList<>();

        List<TagsDocAlerts> result = new ArrayList<>();
        try {
            String[] projection = TagsDocAlertsContract.ALL_COLUMNS_PROJECTION;
            int idIndex = Util.findIndex(projection, TagsDocAlertsContract.TAGS_ID);
            int statusIndex = Util.findIndex(projection, TagsDocAlertsContract.TAGS_STATUS);
            int remoteIdIndex = Util.findIndex(projection, TagsDocAlertsContract.TAGS_REMOTE_ID);
            int updateTimeIndex = Util.findIndex(projection, TagsDocAlertsContract.TAGS_UPDATE_TIME);
            int nameIndex = Util.findIndex(projection, TagsDocAlertsContract.TAGS_NAME);
            int userIdIndex = Util.findIndex(projection, TagsDocAlertsContract.TAGS_USER_ID);
            int alertsCountIndex = Util.findIndex(projection, TagsDocAlertsContract.ALERTS_COUNT);
            int docTypeNameIndex = Util.findIndex(projection, TagsDocAlertsContract.DOCUMENT_TYPE_NAME);

            TagsDocAlerts item = null;
            while (cursor.moveToNext()) {
                int id = TagsDocAlerts.NO_TAG_ID;
                if (!cursor.isNull(idIndex)) id = cursor.getInt(idIndex);
                if (item == null || item.getId() != id) {
                    item = new TagsDocAlerts();
                    item.setId(id);
                    if (id != TagsDocAlerts.NO_TAG_ID) {
                        item.setRemoteId(cursor.getInt(remoteIdIndex));
                        item.setUpdateTime(cursor.getInt(updateTimeIndex));
                        item.setUserId(cursor.getInt(userIdIndex));
                        item.setStatus(cursor.getInt(statusIndex));
                        item.setName(cursor.getString(nameIndex));
                    }
                    result.add(item);
                }
                item.addAlertsCount(cursor.getInt(alertsCountIndex));
                item.addDocTypeName(cursor.getString(docTypeNameIndex));
            }
        } finally {
            cursor.close();
        }

        Collections.sort(result, new Comparator<TagsDocAlerts>() {
            @Override
            public int compare(TagsDocAlerts lhs, TagsDocAlerts rhs) {
                return lhs.getUpdateTime() - rhs.getUpdateTime();
            }
        });

        return result;
    }

    public static class TagsDocAlerts extends Tag {
        public static final int NO_TAG_ID = -2;

        private Set<String> docsTypesNames = new HashSet<>();
        private int alertsCount;

        public Set<String> getDocsTypesNames() {
            return docsTypesNames;
        }

        public void addDocTypeName(String docType) {
            docsTypesNames.add(docType);
        }

        public int getAlertsCount() {
            return alertsCount;
        }

        public void addAlertsCount(int alertsCount) {
            this.alertsCount += alertsCount;
        }

        @Override
        public String toString() {
            return "TagsDocAlerts{" +
                    "id=" + id +
                    ", remoteId=" + remoteId +
                    ", name='" + name + '\'' +
                    ", userId=" + userId +
                    ", docsTypesNames=" + docsTypesNames +
                    ", alertsCount=" + alertsCount +
                    '}';
        }
    }
}
