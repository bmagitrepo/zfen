package com.zoomlee.zoo.syncservice.resttask_executors;

import android.content.ContentResolver;
import android.content.Context;

import com.zoomlee.zoo.dao.DaoHelper;
import com.zoomlee.zoo.dao.DaoHelpersContainer;
import com.zoomlee.zoo.net.CommonResponse;
import com.zoomlee.zoo.net.RestTask;
import com.zoomlee.zoo.net.api.ApiUrl;
import com.zoomlee.zoo.net.api.TagDataApi;
import com.zoomlee.zoo.net.model.Tag;
import com.zoomlee.zoo.provider.helpers.Tags2DocumentsProviderHelper;
import com.zoomlee.zoo.utils.DBUtil;
import com.zoomlee.zoo.utils.SharedPreferenceUtils;

import retrofit.RestAdapter;
import retrofit.RetrofitError;

/**
 * Created by
 *
 * @author Evgen Marinin <ievgen.marinin@alterplay.com>
 * @date 09.04.15.
 */
class DeleteTagTaskExecutor implements TaskExecutorFactory.RestTaskExecutor {

    @Override
    public boolean execute(Context context, RestTask restTask) {
        if (SharedPreferenceUtils.getUtils().getPrivateKey() == null) {
            return false;
        }

        DaoHelper<Tag> tagDaoHelper = DaoHelpersContainer.getInstance().getDaoHelper(Tag.class);
        Tag tag = tagDaoHelper.getItemByLocalId(context, restTask.getLocalItemId(), true);
        if (tag == null) return true;
        if (tag.getRemoteId() == -1) {
            tagDaoHelper.deleteByLocalId(context, tag.getId());
            ContentResolver contentResolver = context.getContentResolver();
            contentResolver.delete(Tags2DocumentsProviderHelper.Tags2DocumentsContract.CONTENT_URI,
                    Tags2DocumentsProviderHelper.Tags2DocumentsContract.TAG_ID + "=?",
                    DBUtil.getArgsArray(tag.getId()));
            return true;
        }

        boolean result = true;
        TagDataApi api = buildTagDataApi();
        try {
            CommonResponse<Object> tagCommonResponse = api.deleteTag(SharedPreferenceUtils.getUtils().getPrivateKey(),
                    tag.getRemoteId());
            if (tagCommonResponse.getError().getCode() != 200)
                result = false;
        } catch (RetrofitError error) {
            error.printStackTrace();
            result = false;
        }

        if (result) {
            tagDaoHelper.deleteByLocalId(context, tag.getId());
            ContentResolver contentResolver = context.getContentResolver();
            contentResolver.delete(Tags2DocumentsProviderHelper.Tags2DocumentsContract.CONTENT_URI,
                    Tags2DocumentsProviderHelper.Tags2DocumentsContract.TAG_ID + "=?",
                    DBUtil.getArgsArray(tag.getId()));
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
