package com.ky.auto_pkg

import com.google.gson.GsonBuilder
import com.ky.auto_pkg.http.AddHeaderInterceptor
import com.ky.auto_pkg.http.BaseResponse
import com.ky.auto_pkg.http.BeanTypeDeserializer
import com.ky.auto_pkg.http.IAppService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * Created by 王金瑞
 * 2021/11/26
 * 19:58
 * com.ky.auto_pkg
 */
class Core {
    companion object {
        private const val CORE_POOL_SIZE = 3
        private const val MAXI_MUM_POOL_SIZE = 20
        val GSON = GsonBuilder()
            .registerTypeAdapter(BaseResponse::class.java, BeanTypeDeserializer())
            .serializeNulls()
            .create()
        val APP_SERVICE: IAppService by lazy {
            val builder = OkHttpClient.Builder()
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            builder.addInterceptor(interceptor)
            // 加入拦截器
            // 加入拦截器
            builder.addInterceptor(AddHeaderInterceptor())
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
            Retrofit.Builder()
                .baseUrl(ConfigConstants.API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .client(builder.build())
                .build()
                .create(IAppService::class.java)
        }
        val THREAD_POOL = ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAXI_MUM_POOL_SIZE, 20L, TimeUnit.SECONDS,
            SynchronousQueue()
        )
        fun test() {

        }
    }
}