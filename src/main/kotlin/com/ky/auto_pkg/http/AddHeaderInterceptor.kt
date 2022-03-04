package com.ky.auto_pkg.http

import com.ky.auto_pkg.ConfigConstants
import com.ky.auto_pkg.utils.LogUtils
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Created by 王金瑞
 * 2022/2/23
 * 16:40
 * com.ky.auto_pkg.http
 */
class AddHeaderInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        request = request.newBuilder()
            .url(request.url)
            .addHeader("token", ConfigConstants.TOKEN)
            .build()
        val url = request.url.toUrl().toString()
        LogUtils.d(LogUtils.LOG_HTTP, "请求拦截器：url：${url} request method：${request.method}")
        return chain.proceed(request)
    }
}