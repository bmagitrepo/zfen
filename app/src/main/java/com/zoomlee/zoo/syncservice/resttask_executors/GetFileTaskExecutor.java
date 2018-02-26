package com.zoomlee.zoo.syncservice.resttask_executors;

import android.content.Context;
import android.text.TextUtils;

import com.zoomlee.zoo.dao.DaoHelper;
import com.zoomlee.zoo.dao.DaoHelpersContainer;
import com.zoomlee.zoo.net.RestTask;
import com.zoomlee.zoo.net.api.FileDataApi;
import com.zoomlee.zoo.net.model.File;
import com.zoomlee.zoo.net.model.FilesType;
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
class GetFileTaskExecutor implements TaskExecutorFactory.RestTaskExecutor {

    @Override
    public boolean execute(Context context, RestTask restTask) {
        if (SharedPreferenceUtils.getUtils().getPrivateKey() == null) {
            return false;
        }

        boolean result = true;
        DaoHelper<File> fileDaoHelper = DaoHelpersContainer.getInstance().getDaoHelper(File.class);
        File file = fileDaoHelper.getItemByLocalId(context, restTask.getLocalItemId(), true);

        if (file == null || TextUtils.isEmpty(file.getRemotePath())
                || file.getStatus() == File.STATUS_DELETED) return true;

        int indexOfName = file.getRemotePath().lastIndexOf("/");
        String relativePath = file.getRemotePath().substring(indexOfName + 1);
        String endpoint = file.getRemotePath().substring(0, indexOfName);

        FileDataApi api = buildFileDataApi(endpoint);
        try {
            Response response = api.getAttachedFile(relativePath, SharedPreferenceUtils.getUtils().getPrivateKey());
            if (response.getStatus() == 200 && response.getBody() != null) {
                File.Type fileType = file.getType();
                String fileName = "file_" + file.getId() + "_" + file.getRemoteId()
                        + (fileType == null
                            ? (file.getTypeId() == FilesType.IMAGE_TYPE ? ".png" : ".pdf")
                            : fileType.extension);
                String filePath = FileUtil.saveFileInternaly(context, fileName, response.getBody());
                if (filePath != null) {
                    file.setLocalPath(filePath);
                    fileDaoHelper.saveRemoteChanges(context, file);
                } else {
                    result = false;
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
