package com.zoomlee.zoo.syncservice.resttask_executors;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zoomlee.zoo.dao.DaoHelper;
import com.zoomlee.zoo.dao.DaoHelpersContainer;
import com.zoomlee.zoo.net.CommonResponse;
import com.zoomlee.zoo.net.RestTask;
import com.zoomlee.zoo.net.api.ApiUrl;
import com.zoomlee.zoo.net.api.DocumentDataApi;
import com.zoomlee.zoo.net.model.Document;
import com.zoomlee.zoo.net.model.Person;
import com.zoomlee.zoo.utils.SharedPreferenceUtils;

import retrofit.RestAdapter;
import retrofit.RetrofitError;

/**
 * Created by
 *
 * @author Evgen Marinin <ievgen.marinin@alterplay.com>
 * @date 29.01.15.
 */
class PostDocumentTaskExecutor implements TaskExecutorFactory.RestTaskExecutor {

    @Override
    public boolean execute(Context context, RestTask restTask) {
        if (SharedPreferenceUtils.getUtils().getPrivateKey() == null) {
            return false;
        }

        boolean result = true;
        DaoHelper<Document> documentDaoHelper = DaoHelpersContainer.getInstance().getDaoHelper(Document.class);
        Document document = documentDaoHelper.getItemByLocalId(context, restTask.getLocalItemId(), true);
        if (document == null) return true;

        DocumentDataApi api = buildDocumentDataApi();
        try {
            Gson gson = new GsonBuilder().serializeNulls().create();
            String privateKey = SharedPreferenceUtils.getUtils().getPrivateKey();
            CommonResponse<Document> commonResponse;
            if (document.getLocalPersonId() == Person.ME_ID)

                commonResponse= api.postDocument(privateKey, document.getName(), "null", document.getCategoryId(),
                        document.getColorId(), document.getTypeId(), document.getNotes(), gson.toJson(document.getFieldsList()), gson.toJson(document.getTagsList()));
            else {
                Person person = getDocsPerson(context, document.getLocalPersonId());
                if (person == null || person.getStatus() == Person.STATUS_DELETED) {
                    documentDaoHelper.deleteItem(context, document);
                    return true;
                } else if (person.getRemoteId() == -1) return false;


                commonResponse= api.postDocument(privateKey, document.getName(), String.valueOf(person.getRemoteId()),
                        document.getCategoryId(), document.getColorId(), document.getTypeId(), document.getNotes(),
                        gson.toJson(document.getFieldsList()), gson.toJson(document.getTagsList()));
            }

            if (commonResponse.getError().getCode() == 200 && commonResponse.getBody() != null) {
                Document newDocument = commonResponse.getBody();
                newDocument.setId(document.getId());
                documentDaoHelper.saveRemoteChanges(context, newDocument);
            } else {
                result = false;
            }
        } catch (RetrofitError error) {
            error.printStackTrace();
            result = false;
        }

        return result;
    }

    private Person getDocsPerson(Context context, int personLocalId) {
        DaoHelper<Person> daoHelper = DaoHelpersContainer.getInstance().getDaoHelper(Person.class);
        return daoHelper.getItemByLocalId(context, personLocalId, true);
    }

    private DocumentDataApi buildDocumentDataApi() {
        return new RestAdapter.Builder()
                .setEndpoint(ApiUrl.API_URL)
                .build()
                .create(DocumentDataApi.class);
    }
}
