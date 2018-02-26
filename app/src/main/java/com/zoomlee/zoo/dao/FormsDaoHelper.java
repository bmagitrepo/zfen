package com.zoomlee.zoo.dao;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.zoomlee.zoo.net.CommonResponse;
import com.zoomlee.zoo.net.RestTask;
import com.zoomlee.zoo.net.api.ApiUrl;
import com.zoomlee.zoo.net.api.FormDataApi;
import com.zoomlee.zoo.net.model.Form;
import com.zoomlee.zoo.net.model.FormField;
import com.zoomlee.zoo.net.model.Person;
import com.zoomlee.zoo.provider.helpers.FormsProviderHelper.FormsContract;
import com.zoomlee.zoo.utils.DBUtil;
import com.zoomlee.zoo.utils.SharedPreferenceUtils;
import com.zoomlee.zoo.utils.Util;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import retrofit.RestAdapter;

import static com.zoomlee.zoo.provider.helpers.FormFieldsProviderHelper.FormFieldsContract;
import static com.zoomlee.zoo.utils.Events.FormChanged;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 26.05.15.
 */
class FormsDaoHelper extends UserDataDaoHelper<Form> {

    private static final String LAST_SYNC_TIME_KEY = "last_forms_sync";

    protected FormsDaoHelper() {
        super(LAST_SYNC_TIME_KEY, FormsContract.CONTENT_URI, RestTask.Types.FORM_POST, -1);
    }

    @Override
    public synchronized Uri saveRemoteChanges(Context context, Form form) {
        PersonsDaoHelper personsDaoHelper = (PersonsDaoHelper) DaoHelpersContainer.getInstance().getDaoHelper(Person.class);
        if (form.getRemotePersonId() != -1) {
            Person person = personsDaoHelper.getItemByRemoteId(context, form.getRemotePersonId());
            if (person == null) return null;
            form.setLocalPersonId(person.getId());
        }

        return super.saveRemoteChanges(context, form);
    }

    @Override
    public List<Form> readItems(Context context, Cursor cursor) {
        return readItems(context, cursor, null);
    }

    @Override
    public List<Form> readItems(Context context, Cursor cursor, OnItemLoadedListener<Form> onItemLoadedListener) {
        if (cursor == null || cursor.getCount() == 0) return new ArrayList<Form>();
        List<Form> items = new ArrayList<Form>(cursor.getCount());

        String[] projection = FormsContract.ALL_COLUMNS_PROJECTION;

        int idIndex = Util.findIndex(projection, FormsContract._ID);
        int remoteIdIndex = Util.findIndex(projection, FormsContract.REMOTE_ID);
        int updateTimeIndex = Util.findIndex(projection, FormsContract.UPDATE_TIME);
        int personIdIndex = Util.findIndex(projection, FormsContract.PERSON_ID);
        int userIdIndex = Util.findIndex(projection, FormsContract.USER_ID);

        DaoHelper<FormField> formFormFieldDaoHelper = DaoHelpersContainer.getInstance().getDaoHelper(FormField.class);

        while (cursor.moveToNext()) {
            Form item = new Form();
            item.setId(cursor.getInt(idIndex));
            item.setRemoteId(cursor.getInt(remoteIdIndex));
            item.setUpdateTime(cursor.getInt(updateTimeIndex));
            item.setLocalPersonId(cursor.getInt(personIdIndex));
            item.setUserId(cursor.getInt(userIdIndex));

            item.setData(formFormFieldDaoHelper.getAllItems(context,
                    FormFieldsContract.TABLE_NAME + "." + FormFieldsContract.FORM_ID + "=?",
                    DBUtil.getArgsArray(item.getId()), FormFieldsContract.FIELD_TYPE_ID + " ASC"));

            items.add(item);
            if (onItemLoadedListener != null) onItemLoadedListener.onItemLoaded(item);
        }

        return items;
    }

    @Override
    public List<Form> getAllItems(Context context) {
        return getAllItems(context, null, null, null);
    }

    @Override
    public Form getItemByLocalId(Context context, int localId, boolean showDeleted) {
        ContentResolver contentResolver = context.getContentResolver();
        String selection = FormsContract._ID + "=" + localId;
        Cursor cursor = contentResolver.query(FormsContract.CONTENT_URI,
                null, selection, null, null);

        List<Form> result = readItems(context, cursor);
        cursor.close();
        return result.size() > 0 ? result.get(0) : null;
    }

    @Override
    protected int saveItems(Context context, List<Form> items) {
        for (Form form : items) {
            saveItem(context, form);
        }

        return items.size();
    }

    @Override
    protected void saveRelatedLocalData(Context context, Form form) {
        FormFieldsDaoHelper formFieldDaoHelper = (FormFieldsDaoHelper) DaoHelpersContainer.getInstance().getDaoHelper(FormField.class);
        formFieldDaoHelper.saveItems(context, form.getData(), form.getId());
    }

    @Override
    protected void saveRelatedRemoteData(Context context, Form form) {
        saveRelatedLocalData(context, form);
    }

    @Override
    protected void deleteRelatedData(Context context, Form form) {
        FormFieldsDaoHelper formFieldDaoHelper = (FormFieldsDaoHelper) DaoHelpersContainer.getInstance().getDaoHelper(FormField.class);
        formFieldDaoHelper.deleteAllItems(context, form.getId());
    }

    @Override
    protected void postEntityChanged(Form form) {
        EventBus.getDefault().post(new FormChanged(FormChanged.UPDATED, form));
    }

    @Override
    protected void postEntityDeleted(Form form) {
    }

    /**
     * throw UnsupportedOperationException
     * @param context
     * @param form
     */
    @Override
    public synchronized void deleteItem(Context context, Form form) {
        throw new UnsupportedOperationException("delete not permitted!!!");
    }

    @Override
    protected CommonResponse<List<Form>> callApi(Context context, Object api) {
        return buildFormDataApi().getForms(SharedPreferenceUtils.getUtils().getPrivateKey(), getLastSyncTime());
    }

    @Override
    protected ContentValues convertEntity(Form item) {
        ContentValues contentValues = new ContentValues();
        if (item.getId() != -1)
            contentValues.put(FormsContract._ID, item.getId());
        if (item.getId() == -1 || item.getRemoteId() != -1)
            contentValues.put(FormsContract.REMOTE_ID, item.getRemoteId());
        contentValues.put(FormsContract.UPDATE_TIME, item.getUpdateTime());
        contentValues.put(FormsContract.PERSON_ID, item.getLocalPersonId());
        contentValues.put(FormsContract.USER_ID, item.getUserId());
        return contentValues;
    }

    protected void deleteAllItems(Context context, int personLocalId) {
        ContentResolver contentResolver = context.getContentResolver();
        String selection = FormsContract.PERSON_ID + "=?";
        String[] args = DBUtil.getArgsArray(personLocalId);
        Cursor cursor = contentResolver.query(FormsContract.CONTENT_URI, null, selection, args, null);
        List<Form> docs = readItems(context, cursor);
        for (Form doc : docs) {
            FormFieldsDaoHelper formFieldDaoHelper = (FormFieldsDaoHelper) DaoHelpersContainer.getInstance().getDaoHelper(FormField.class);
            formFieldDaoHelper.deleteAllItems(context, doc.getId());
        }
        contentResolver.delete(FormsContract.CONTENT_URI, selection, args);

        for (Form doc : docs) {
            EventBus.getDefault().post(new FormChanged(FormChanged.DELETED, doc));
        }
    }

    private FormDataApi buildFormDataApi() {
        return new RestAdapter.Builder()
                .setEndpoint(ApiUrl.API_URL)
                .build()
                .create(FormDataApi.class);
    }
}
