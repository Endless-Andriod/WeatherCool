package com.example.administrator.weathercool.util;


import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by Administrator on 2017/3/21.
 */

public class HttpUtil {
    public static void senOkHttpRequest(String adress, Callback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(adress).build();
        client.newCall(request).enqueue(callback);
    }
}
