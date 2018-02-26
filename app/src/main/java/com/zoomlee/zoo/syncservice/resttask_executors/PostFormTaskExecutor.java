package com.zoomlee.zoo.syncservice.resttask_executors;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zoomlee.zoo.dao.DaoHelper;
import com.zoomlee.zoo.dao.DaoHelpersContainer;
import com.zoomlee.zoo.net.CommonResponse;
import com.zoomlee.zoo.net.RestTask;
import com.zoomlee.zoo.net.api.ApiUrl;
import com.zoomlee.zoo.net.api.FormDataApi;
import com.zoomlee.zoo.net.model.Form;
import com.zoomlee.zoo.net.model.Person;
import com.zoomlee.zoo.utils.SharedPreferenceUtils;

import retrofit.RestAdapter;
import retrofit.RetrofitError;

/**
 * Created by
 *
 * @author Evgen Marinin <ievgen.marinin@alterplay.com>
 * @date 27.05.15.
 */
class PostFormTaskExecutor implements TaskExecutorFactory.RestTaskExecutor {

    @Override
    public boolean execute(Context context, RestTask restTask) {
        if (SharedPreferenceUtils.getUtils().getPrivateKey() == null) {
            return false;
        }

        boolean result = true;
        DaoHelper<Form> formDaoHelper = DaoHelpersContainer.getInstance().getDaoHelper(Form.class);
        Form form = formDaoHelper.getItemByLocalId(context, restTask.getLocalItemId(), true);
        if (form == null) return true;
        if (form.getLocalPersonId() != -1) {
            Person person = getFormsPerson(context, form.getLocalPersonId());
            if (person == null || person.getStatus() == Person.STATUS_DELETED)
                return true;
        }

        FormDataApi api = buildFormDataApi();
        try {
            Gson gson = new GsonBuilder().serializeNulls().create();
            String privateKey = SharedPreferenceUtils.getUtils().getPrivateKey();
            CommonResponse<Form> commonResponse = api.postForm(privateKey, form.getRemoteId(), gson.toJson(form.getData()));

            if (commonResponse.getError().getCode() == 200 && commonResponse.getBody() != null) {
                Form newForm = commonResponse.getBody();
                newForm.setId(form.getId());
                formDaoHelper.saveRemoteChanges(context, newForm);
            } else {
                result = false;
            }
        } catch (RetrofitError error) {
            error.printStackTrace();
            result = false;
        }

        return result;
    }

    private Person getFormsPerson(Context context, int personLocalId) {
        DaoHelper<Person> daoHelper = DaoHelpersContainer.getInstance().getDaoHelper(Person.class);
        return daoHelper.getItemByLocalId(context, personLocalId, true);
    }

    private FormDataApi buildFormDataApi() {
        return new RestAdapter.Builder()
                .setEndpoint(ApiUrl.API_URL)
                .build()
                .create(FormDataApi.class);
    }
}
