package com.ky.auto_pkg.http

import com.google.gson.JsonObject
import com.ky.auto_pkg.model.FeiShuObj
import com.ky.auto_pkg.model.StsTokenContainer
import com.ky.auto_pkg.model.UploadServerBody
import io.reactivex.rxjava3.core.Flowable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url

/**
 * Created by 王金瑞
 * 2021/11/29
 * 11:02
 * com.ky.auto_pkg.http
 */
interface IAppService {
    @GET("api/ace/pack/oss")
    fun net_getOssToken(): Flowable<BaseResponse<StsTokenContainer>>

    @POST("api/ace/pack/android")
    fun net_postUploadResult(
        @Body body: UploadServerBody
    ): Flowable<BaseResponse<StsTokenContainer>>

    @POST
    fun net_postUploadResult(
        @Url url: String,
        @Body body: FeiShuObj
    ): Flowable<JsonObject>
}