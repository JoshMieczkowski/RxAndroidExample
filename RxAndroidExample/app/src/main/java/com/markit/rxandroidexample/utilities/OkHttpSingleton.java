package com.markit.rxandroidexample.utilities;

import com.squareup.okhttp.OkHttpClient;

/**
 * Created by josh.mieczkowski on 8/26/2015.
 */
public class OkHttpSingleton {
    private static OkHttpSingleton ourInstance = new OkHttpSingleton();
    private OkHttpClient okHttpClient;

    public static OkHttpSingleton getInstance() {
        return ourInstance;
    }

    private OkHttpSingleton() {
        okHttpClient = new OkHttpClient();
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }
}
