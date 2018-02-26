package com.zoomlee.zoo.syncservice.resttask_executors;

import android.content.Context;
import android.text.TextUtils;

import com.zoomlee.zoo.dao.DaoHelper;
import com.zoomlee.zoo.dao.DaoHelpersContainer;
import com.zoomlee.zoo.net.RestTask;
import com.zoomlee.zoo.net.api.FileDataApi;
import com.zoomlee.zoo.net.model.Person;
import com.zoomlee.zoo.utils.FileUtil;
import com.zoomlee.zoo.utils.SharedPreferenceUtils;

import java.io.File;

import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by
 *
 * @author Evgen Marinin <ievgen.marinin@alterplay.com>
 * @date 29.01.15.
 */
class GetPersonIconTaskExecutor implements TaskExecutorFactory.RestTaskExecutor {

    @Override
    public boolean execute(Context context, RestTask restTask) {
        if (SharedPreferenceUtils.getUtils().getPrivateKey() == null) {
            return false;
        }

        boolean result;
        DaoHelper<Person> personDaoHelper = DaoHelpersContainer.getInstance().getDaoHelper(Person.class);
        Person person = personDaoHelper.getItemByLocalId(context, restTask.getLocalItemId(), true);

        if (person == null || TextUtils.isEmpty(person.getImageRemotePath())
                || person.getStatus() == Person.STATUS_DELETED) return true;

        int indexOfName = person.getImageRemotePath().lastIndexOf("/");
        String relativePath = person.getImageRemotePath().substring(indexOfName + 1);
        String relative144Path = person.getImageRemote144Path().substring(indexOfName + 1);
        String endpoint = person.getImageRemotePath().substring(0, indexOfName);

        FileDataApi api = buildFileDataApi(endpoint);

        result = loadIcon(context, relativePath, true, person, api);
        result = result && loadIcon(context, relative144Path, false, person, api);
        if (result) personDaoHelper.saveRemoteChanges(context, person);

        return result;
    }

    private boolean loadIcon(Context context, String relativePath, boolean full, Person person, FileDataApi api) {
        boolean result = true;
        try {
            Response response = api.getAttachedFile(relativePath, SharedPreferenceUtils.getUtils().getPrivateKey());
            if (response.getStatus() == 200 && response.getBody() != null) {
                String fileName;
                if (person.getImageLocalPath() == null) {
                    fileName = "person_" + person.getId() +
                            (full ? ".png" : "_144x144.png");
                } else {
                    File file = new File(full ? person.getImageLocalPath() : person.getImageLocal144Path());
                    fileName = file.getName();
                }
                String filePath = FileUtil.saveFileInternaly(context, fileName, response.getBody());
                if (filePath == null) {
                    result = false;
                } else if (full) {
                    person.setImageLocalPath(filePath);
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
