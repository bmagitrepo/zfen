package com.zoomlee.zoo.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.zoomlee.zoo.net.CommonResponse;
import com.zoomlee.zoo.net.RestTask;
import com.zoomlee.zoo.net.api.ApiUrl;
import com.zoomlee.zoo.net.api.PersonDataApi;
import com.zoomlee.zoo.net.model.Document;
import com.zoomlee.zoo.net.model.Form;
import com.zoomlee.zoo.net.model.Person;
import com.zoomlee.zoo.provider.helpers.BaseProviderHelper;
import com.zoomlee.zoo.provider.helpers.PersonsProviderHelper.PersonsContract;
import com.zoomlee.zoo.syncservice.RestTaskPoster;
import com.zoomlee.zoo.utils.PicassoUtil;
import com.zoomlee.zoo.utils.SharedPreferenceUtils;
import com.zoomlee.zoo.utils.SharedPreferenceUtils.LastSyncTimeKeys;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import retrofit.RestAdapter;

import static com.zoomlee.zoo.utils.Events.PersonChanged;

/**
 * Created by  Evgen Marinin <ievgen.marinin@alterplay.com> on 22.01.15.
 */
class PersonsDaoHelper extends UserDataDaoHelper<Person> {

    protected PersonsDaoHelper() {
        super(LastSyncTimeKeys.PERSONS, PersonsContract.CONTENT_URI, RestTask.Types.PERSON_UPLOAD, RestTask.Types.PERSON_DELETE);
    }

    @Override
    public List<Person> readItems(Context context, Cursor cursor) {
        return readItems(context, cursor, null);
    }

    @Override
    public List<Person> readItems(Context context, Cursor cursor, OnItemLoadedListener<Person> onItemLoadedListener) {
        if (cursor == null || cursor.getCount() == 0) return new ArrayList<Person>();
        List<Person> items = new ArrayList<Person>(cursor.getCount());

        int idIndex = cursor.getColumnIndex(PersonsContract._ID);
        int remoteIdIndex = cursor.getColumnIndex(PersonsContract.REMOTE_ID);
        int statusIndex = cursor.getColumnIndex(PersonsContract.STATUS);
        int updateTimeIndex = cursor.getColumnIndex(PersonsContract.UPDATE_TIME);
        int nameIndex = cursor.getColumnIndex(PersonsContract.NAME);
        int userIdIndex = cursor.getColumnIndex(PersonsContract.USER_ID);
        int imageRemotePathIndex = cursor.getColumnIndex(PersonsContract.IMAGE_REMOTE_PATH);
        int imageLocalPathIndex = cursor.getColumnIndex(PersonsContract.IMAGE_LOCAL_PATH);


        while (cursor.moveToNext()) {
            Person item = new Person();
            item.setId(cursor.getInt(idIndex));
            item.setRemoteId(cursor.getInt(remoteIdIndex));
            item.setStatus(cursor.getInt(statusIndex));
            item.setUpdateTime(cursor.getInt(updateTimeIndex));
            item.setName(cursor.getString(nameIndex));
            item.setUserId(cursor.getInt(userIdIndex));
            item.setImageRemotePath(cursor.getString(imageRemotePathIndex));
            item.setImageLocalPath(cursor.getString(imageLocalPathIndex));
            items.add(item);
            if (onItemLoadedListener != null) onItemLoadedListener.onItemLoaded(item);
        }

        return items;
    }

    @Override
    public List<Person> getAllItems(Context context) {
        String selection = BaseProviderHelper.DataColumns.STATUS + "=1";
        String sortOrder = PersonsContract.NAME + " ASC";
        return getAllItems(context, selection, null, sortOrder);
    }

    @Override
    protected CommonResponse<List<Person>> callApi(Context context, Object api) {
        CommonResponse<List<Person>> commonResponse = buildPersonDataApi().getPersons(SharedPreferenceUtils.getUtils().getPrivateKey(), getLastSyncTime());
        return commonResponse;
    }

    private PersonDataApi buildPersonDataApi() {
        return new RestAdapter.Builder()
                .setEndpoint(ApiUrl.API_URL)
                .build()
                .create(PersonDataApi.class);
    }

    @Override
    protected ContentValues convertEntity(Person item) {
        ContentValues contentValues = new ContentValues();
        if (item.getId() != -1)
            contentValues.put(PersonsContract._ID, item.getId());
        if (item.getId() == -1 || item.getRemoteId() != -1)
            contentValues.put(PersonsContract.REMOTE_ID, item.getRemoteId());
        contentValues.put(PersonsContract.STATUS, item.getStatus());
        contentValues.put(PersonsContract.UPDATE_TIME, item.getUpdateTime());
        contentValues.put(PersonsContract.NAME, item.getName());
        contentValues.put(PersonsContract.USER_ID, item.getUserId());
        contentValues.put(PersonsContract.IMAGE_REMOTE_PATH, item.getImageRemotePath());
        contentValues.put(PersonsContract.IMAGE_LOCAL_PATH, item.getImageLocalPath());
        return contentValues;
    }

    @Override
    protected void saveRelatedLocalData(Context context, Person person) {
        if (person.getImageLocal144Path() != null)
            PicassoUtil.getInstance().invalidate(new File(person.getImageLocal144Path()));
    }

    @Override
    protected void saveRelatedRemoteData(Context context, Person person) {
        RestTask restTask = new RestTask(person.getId(), RestTask.Types.PERSON_GET_ICON);
        RestTaskPoster.postTask(context, restTask, true);
    }

    @Override
    protected void deleteRelatedData(Context context, Person person) {
        if (person.getImageLocalPath() != null) {
            new File(person.getImageLocalPath()).delete();
            new File(person.getImageLocal144Path()).delete();
        }

        DocumentsDaoHelper documentsDaoHelper = (DocumentsDaoHelper) DaoHelpersContainer.getInstance().getDaoHelper(Document.class);
        documentsDaoHelper.deleteAllItems(context, person.getId());
        FormsDaoHelper formsDaoHelper = (FormsDaoHelper) DaoHelpersContainer.getInstance().getDaoHelper(Form.class);
        formsDaoHelper.deleteAllItems(context, person.getId());
    }

    @Override
    protected void postEntityChanged(Person person) {
        EventBus.getDefault().post(new PersonChanged(PersonChanged.UPDATED, person));
    }

    @Override
    protected void postEntityDeleted(Person person) {
        EventBus.getDefault().post(new PersonChanged(PersonChanged.DELETED, person));
    }
}
