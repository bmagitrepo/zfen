package com.zoomlee.zoo.syncservice.resttask_executors;

import android.content.Context;

import com.zoomlee.zoo.net.CommonResponse;
import com.zoomlee.zoo.net.RestTask;
import com.zoomlee.zoo.net.api.ApiUrl;
import com.zoomlee.zoo.net.api.UserDataApi;
import com.zoomlee.zoo.net.model.User;
import com.zoomlee.zoo.syncservice.RestTaskPoster;
import com.zoomlee.zoo.utils.SharedPreferenceUtils;

import java.io.File;

import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.mime.TypedFile;

/**
 * Created by
 *
 * @author Evgen Marinin <ievgen.marinin@alterplay.com>
 * @date 29.01.15.
 */
class PutUserTaskExecutor implements TaskExecutorFactory.RestTaskExecutor {

    @Override
    public boolean execute(Context context, RestTask restTask) {
        if (SharedPreferenceUtils.getUtils().getPrivateKey() == null) {
            return false;
        }

        boolean result = true;
        UserDataApi api = buildUserDataApi();
        try {
            User user = SharedPreferenceUtils.getUtils().getUserSettings();
            TypedFile fileToSend = null;
            if (user.getImageLocalPath() != null)
                fileToSend = new TypedFile("image/jpg", new File(user.getImageLocalPath()));

            Integer countryId = user.getCountryId() != -1 ? user.getCountryId() : null;
            CommonResponse<User> userCommonResponse;
            if (fileToSend != null)
                userCommonResponse = api.updateUser(user.getPrivateKey(), user.getRemoteId(), user.getName(), countryId, fileToSend);
            else
                userCommonResponse = api.updateUser(user.getPrivateKey(), user.getRemoteId(), user.getName(), countryId);

            if (userCommonResponse.getError().getCode() == 200 && userCommonResponse.getBody() != null) {
                User newUser = userCommonResponse.getBody();
                newUser.setImageLocalPath(user.getImageLocalPath());
                SharedPreferenceUtils.getUtils().saveUserSettings(newUser);
            } else {
                result = false;
            }
            RestTaskPoster.postTask(context, new RestTask(RestTask.Types.USER_GET), true);
        } catch (RetrofitError error) {
            error.printStackTrace();
            result = false;
        }

        return result;
    }

    private UserDataApi buildUserDataApi() {
        return new RestAdapter.Builder()
                .setEndpoint(ApiUrl.API_URL)
                .build()
                .create(UserDataApi.class);
    }
}
