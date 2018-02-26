package com.zoomlee.zoo.syncservice.resttask_executors;

import android.content.Context;

import com.zoomlee.zoo.dao.DaoHelper;
import com.zoomlee.zoo.dao.DaoHelpersContainer;
import com.zoomlee.zoo.net.CommonResponse;
import com.zoomlee.zoo.net.RestTask;
import com.zoomlee.zoo.net.api.ApiUrl;
import com.zoomlee.zoo.net.api.PersonDataApi;
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
class DeletePersonTaskExecutor implements TaskExecutorFactory.RestTaskExecutor {

    @Override
    public boolean execute(Context context, RestTask restTask) {
        if (SharedPreferenceUtils.getUtils().getPrivateKey() == null) {
            return false;
        }

        boolean result = true;
        DaoHelper<Person> personDaoHelper = DaoHelpersContainer.getInstance().getDaoHelper(Person.class);
        Person person = personDaoHelper.getItemByLocalId(context, restTask.getLocalItemId(), true);
        if (person == null)
            return true;
        if (person.getRemoteId() == -1) {
            personDaoHelper.deleteByLocalId(context, person.getId());
            return true;
        }

        PersonDataApi api = buildPersonDataApi();
        try {
            CommonResponse<Object> userCommonResponse = api.deletePerson(SharedPreferenceUtils.getUtils().getPrivateKey(),
                    person.getRemoteId());
            if (userCommonResponse.getError().getCode() != 200) {
                result = false;
            }
        } catch (RetrofitError error) {
            error.printStackTrace();
            result = false;
        }

        if (result) personDaoHelper.deleteByLocalId(context, person.getId());

        return result;
    }

    private PersonDataApi buildPersonDataApi() {
        return new RestAdapter.Builder()
                .setEndpoint(ApiUrl.API_URL)
                .build()
                .create(PersonDataApi.class);
    }
}
