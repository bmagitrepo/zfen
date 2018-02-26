package com.zoomlee.zoo.utils;

import android.content.Context;

import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.zoomlee.zoo.BuildConfig;

import java.io.IOException;

/**
 * Created by
 *
 * @author Evgen Marinin <ievgen.marinin@alterplay.com>
 * @date 18.02.15.
 */
public class PicassoUtil {

    private static Picasso picasso;

    public static void init(Context context) {
        Picasso.Builder builder = new Picasso.Builder(context);

        if (BuildConfig.DEBUG) {
            builder.loggingEnabled(true);
        }

        OkHttpClient picassoClient = new OkHttpClient();
        picassoClient.interceptors().add(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request newRequest = chain.request().newBuilder()
                        .addHeader("zoomle_key", SharedPreferenceUtils.getUtils().getPrivateKey())
                        .build();
                return chain.proceed(newRequest);
            }
        });

        picasso = builder.downloader(new OkHttpDownloader(picassoClient)).build();
    }

    public static Picasso getInstance() {
        return picasso;
    }
}
