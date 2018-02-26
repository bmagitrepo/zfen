package com.zoomlee.zoo.syncservice.resttask_executors;

import android.content.Context;
import android.text.TextUtils;

import com.zoomlee.zoo.net.RestTask;
import com.zoomlee.zoo.net.api.FileDataApi;
import com.zoomlee.zoo.net.model.User;
import com.zoomlee.zoo.utils.FileUtil;
import com.zoomlee.zoo.utils.SharedPreferenceUtils;

import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by
 *
 * @author Evgen Marinin <ievgen.marinin@alterplay.com>
 * @date 29.01.15.
 */
class GetUserIconTaskExecutor implements TaskExecutorFactory.RestTaskExecutor {

    @Override
    public boolean execute(Context context, RestTask restTask) {
        if (SharedPreferenceUtils.getUtils().getPrivateKey() == null) {
            return false;
        }

        boolean result;
        User user = SharedPreferenceUtils.getUtils().getUserSettings();
        if (TextUtils.isEmpty(user.getImageRemotePath())) return true;

        int indexOfName = user.getImageRemotePath().lastIndexOf("/");
        String relativePath = user.getImageRemotePath().substring(indexOfName + 1);
        String relative144Path = user.getImageRemote144Path().substring(indexOfName + 1);
        String endpoint = user.getImageRemotePath().substring(0, indexOfName);
        FileDataApi api = buildFileDataApi(endpoint);

        result = loadIcon(context, relativePath, true, user, api);
        result = result && loadIcon(context, relative144Path, false, user, api);
        if (result) SharedPreferenceUtils.getUtils().saveUserSettings(user);

        return result;
    }

    private boolean loadIcon(Context context, String relativePath, boolean full, User user, FileDataApi api) {
        boolean result = true;
        try {
            Response response = api.getAttachedFile(relativePath, SharedPreferenceUtils.getUtils().getPrivateKey());
            if (response.getStatus() == 200 && response.getBody() != null) {
                String fileToSafe = full ? User.USER_PIC_NAME : User.USER_144PIC_NAME;
                String filePath = FileUtil.saveFileInternaly(context, fileToSafe, response.getBody());
                if (filePath == null) {
                    result = false;
                } else if (full) {
                    user.setImageLocalPath(filePath);
                }
            } else {
                result = false;
            }
        } catch (RetrofitError error) {
            error.printStackTrace();
            result = false;
        }

        return result;
    }


    private FileDataApi buildFileDataApi(String endpoint) {
        return new RestAdapter.Builder()
                .setEndpoint(endpoint)
                .build()
                .create(FileDataApi.class);
    }
}
