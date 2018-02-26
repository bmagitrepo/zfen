package com.zoomlee.zoo.syncservice.resttask_executors;

import android.content.Context;

import com.zoomlee.zoo.dao.DaoHelper;
import com.zoomlee.zoo.dao.DaoHelpersContainer;
import com.zoomlee.zoo.net.CommonResponse;
import com.zoomlee.zoo.net.RestTask;
import com.zoomlee.zoo.net.api.ApiUrl;
import com.zoomlee.zoo.net.api.TaxDataApi;
import com.zoomlee.zoo.net.model.Tax;
import com.zoomlee.zoo.utils.SharedPreferenceUtils;

import retrofit.RestAdapter;
import retrofit.RetrofitError;

/**
 * Created by
 *
 * @author Evgen Marinin <ievgen.marinin@alterplay.com>
 * @date 05.05.15.
 */
class PutTaxTaskExecutor implements TaskExecutorFactory.RestTaskExecutor {

    @Override
    public boolean execute(Context context, RestTask restTask) {
        if (SharedPreferenceUtils.getUtils().getPrivateKey() == null) {
            return false;
        }

        boolean result = true;
        DaoHelper<Tax> taxDaoHelper = DaoHelpersContainer.getInstance().getDaoHelper(Tax.class);
        Tax tax = taxDaoHelper.getItemByLocalId(context, restTask.getLocalItemId(), true);
        if (tax == null || tax.getStatus() == Tax.STATUS_DELETED) return true;

        TaxDataApi api = buildTaxDataApi();
        try {
            Long departure = tax.getDeparture() <= 0 ? null : tax.getDeparture();
            CommonResponse<Tax> userCommonResponse = api.putTax(SharedPreferenceUtils.getUtils().getPrivateKey(),
                        tax.getRemoteId(), tax.getCountryId(), tax.getArrival(), departure);

            if (userCommonResponse.getError().getCode() == 200 && userCommonResponse.getBody() != null) {
                Tax newTax = userCommonResponse.getBody();
                newTax.setId(tax.getId());
                taxDaoHelper.saveRemoteChanges(context, newTax);
            } else {
                result = false;
            }
        } catch (RetrofitError error) {
            error.printStackTrace();
            result = false;
        }

        return result;
    }

    private TaxDataApi buildTaxDataApi() {
        return new RestAdapter.Builder()
                .setEndpoint(ApiUrl.API_URL)
                .build()
                .create(TaxDataApi.class);
    }
}
