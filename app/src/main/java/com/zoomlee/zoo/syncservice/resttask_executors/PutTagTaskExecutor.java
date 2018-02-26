package com.zoomlee.zoo.syncservice.resttask_executors;

import android.content.Context;

import com.zoomlee.zoo.dao.DaoHelper;
import com.zoomlee.zoo.dao.DaoHelpersContainer;
import com.zoomlee.zoo.net.CommonResponse;
import com.zoomlee.zoo.net.RestTask;
import com.zoomlee.zoo.net.api.ApiUrl;
import com.zoomlee.zoo.net.api.TagDataApi;
import com.zoomlee.zoo.net.model.Tag;
import com.zoomlee.zoo.utils.SharedPreferenceUtils;

import retrofit.RestAdapter;
import retrofit.RetrofitError;

/**
 * Created by
 *
 * @author Evgen Marinin <ievgen.marinin@alterplay.com>
 * @date 29.01.15.
 */
class PutTagTaskExecutor implements TaskExecutorFactory.RestTaskExecutor {

    @Override
    public boolean execute(Context context, RestTask restTask) {
        if (SharedPreferenceUtils.getUtils().getPrivateKey() == null) {
            return false;
        }

        boolean result = true;
        DaoHelper<Tag> tagDaoHelper = DaoHelpersContainer.getInstance().getDaoHelper(Tag.class);
        Tag tag = tagDaoHelper.getItemByLocalId(context, restTask.getLocalItemId(), true);
        if (tag == null || tag.getStatus() == Tag.STATUS_DELETED) return true;

        TagDataApi api = buildTagDataApi();
        try {
            CommonResponse<Tag> fileCommonResponse = api.putTag(SharedPreferenceUtils.getUtils().getPrivateKey(),
                    tag.getRemoteId(), tag.getName());
            if (fileCommonResponse.getError().getCode() != 200) {
                result = false;
            }
        } catch (RetrofitError error) {
            error.printStackTrace();
            result = false;
        }

        return result;
    }

    private TagDataApi buildTagDataApi() {
        return new RestAdapter.Builder()
                .setEndpoint(ApiUrl.API_URL)
                .build()
                .create(TagDataApi.class);
    }
}
