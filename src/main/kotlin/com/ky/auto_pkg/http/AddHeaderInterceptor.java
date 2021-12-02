package com.ky.auto_pkg.http;

import com.ky.auto_pkg.ConfigConstants;
import com.ky.auto_pkg.Core;
import com.ky.auto_pkg.utils.LogUtils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;


public class AddHeaderInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        request = request.newBuilder()
                .url(request.url())
                .addHeader("token", ConfigConstants.TOKEN)
                .build();

        String url = request.url().url().toString();
        LogUtils.d(LogUtils.LOG_HTTP, LogUtils.LOG_HTTP + "请求拦截器：url：" + url + " request method: " + request.method());

        return chain.proceed(request);
    }
}
