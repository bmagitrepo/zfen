package com.zoomlee.zoo.net;

import retrofit.Callback;
import retrofit.RetrofitError;

/**
 * Created by
 *
 * @author Michael Oknov <misha.oknov@alterplay.com>
 * @date 1/13/15
 */
public abstract class ZoomleeCallback<T extends CommonResponse> implements Callback<T> {

    @Override
    public void success(T response, retrofit.client.Response response2) {
        if (response.getError().getCode() == 200)
            success(response.getBody());
        else
            error(response.getError());
    }

    protected abstract <U extends Object> void success(U response);

    protected abstract void error(Error error);

    @Override
    public void failure(RetrofitError error) {
        Error mError = new Error();
        if (error.getBody() != null)
            mError.setReason(error.getBody().toString());
        else {
            mError.setCode(Error.NO_CONNECTION_CODE);
            mError.setReason("Connection error");
        }
        error(mError);

    }
}
