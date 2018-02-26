package com.zoomlee.zoo.syncservice.resttask_executors;

import android.content.Context;

import com.zoomlee.zoo.dao.DaoHelper;
import com.zoomlee.zoo.dao.DaoHelpersContainer;
import com.zoomlee.zoo.net.CommonResponse;
import com.zoomlee.zoo.net.RestTask;
import com.zoomlee.zoo.net.api.ApiUrl;
import com.zoomlee.zoo.net.api.StaticDataApi;
import com.zoomlee.zoo.net.model.CategoriesDocumentsType;
import com.zoomlee.zoo.net.model.Category;
import com.zoomlee.zoo.net.model.Changes;
import com.zoomlee.zoo.net.model.Color;
import com.zoomlee.zoo.net.model.Country;
import com.zoomlee.zoo.net.model.DocumentsType;
import com.zoomlee.zoo.net.model.DocumentsType2Field;
import com.zoomlee.zoo.net.model.FieldsType;
import com.zoomlee.zoo.net.model.FilesType;
import com.zoomlee.zoo.net.model.Group;
import com.zoomlee.zoo.utils.SharedPreferenceUtils;
import com.zoomlee.zoo.utils.SharedPreferenceUtils.LastSyncTimeKeys;

import java.util.List;

import retrofit.RestAdapter;
import retrofit.RetrofitError;

/**
 * Created by
 *
 * @author Evgen Marinin <ievgen.marinin@alterplay.com>
 * @date 29.01.15.
 */
class GetStaticDataTaskExecutor implements TaskExecutorFactory.RestTaskExecutor {

    private static Class[] STATIC_DATA_CLASSES = new Class[]{
            CategoriesDocumentsType.class, Category.class, Color.class, Country.class,
            DocumentsType.class, DocumentsType2Field.class, FieldsType.class, Group.class,
            FilesType.class
    };

    @Override
    public boolean execute(Context context, RestTask restTask) {
        StaticDataApi api = buildStaticDataApi();
        boolean success = true;
        int lastDownloadTime = SharedPreferenceUtils.getUtils().getIntSetting(LastSyncTimeKeys.STATIC_DATA);
        try {
            CommonResponse<Changes> changes = api.getChanges(lastDownloadTime);
            if (changes.getError().getCode() == 200) {
                for (Class dataClass : STATIC_DATA_CLASSES) {
                    DaoHelper daoHelper = DaoHelpersContainer.getInstance().getDaoHelper(dataClass);
                    success = success && daoHelper.downloadItems(context, api, changes.getBody());
                }
                lastDownloadTime = changes.getBody().getLastUpdate() + 1;
            } else {
                success = false;
            }
        } catch (RetrofitError error) {
            try {
                CommonResponse<List<Object>> listCommonResponse = new CommonResponse<>();
                listCommonResponse = (CommonResponse<List<Object>>) error.getBodyAs(listCommonResponse.getClass());
                if (listCommonResponse != null && listCommonResponse.getError().getCode() != 200)
                    success = false;
                else
                    error.printStackTrace();
            } catch (RetrofitError error1) {
                error1.printStackTrace();
                success = false;
            }
        }

        if (success) {
            SharedPreferenceUtils.getUtils().setIntSetting(LastSyncTimeKeys.STATIC_DATA, lastDownloadTime);
        }
        return success;
    }

    private StaticDataApi buildStaticDataApi() {
        return new RestAdapter.Builder()
                .setEndpoint(ApiUrl.API_URL)
                .build()
                .create(StaticDataApi.class);
    }
}
