package com.ky.auto_pkg.http

import com.google.gson.JsonObject
import com.ky.auto_pkg.model.*
import io.reactivex.rxjava3.core.Flowable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

/**
 * Created by 王金瑞
 * 2021/11/29
 * 11:02
 * com.ky.auto_pkg.http
 */
interface IAppService {
    /**
     * 获取OssToken
     */
    @GET("api/ace/pack/oss")
    fun net_getOssToken(): Flowable<BaseResponse<StsTokenContainer>>

    /**
     * 通知服务器上传完成
     */
    @POST("api/ace/pack/android")
    fun net_postUploadResult2Server(
        @Body body: UploadServerBody
    ): Flowable<BaseResponse<HashMap<String, String>>>

    /**
     * 获取飞书自建应用Token
     */
    @POST("https://open.feishu.cn/open-apis/auth/v3/tenant_access_token/internal")
    fun net_getFeishuTenantToken(
        @Body body: TenantTokenBody
    ): Flowable<FeishuResponse>

    /**
     * 上传图片至飞书
     */
    @POST("https://open.feishu.cn/open-apis/image/v4/put/")
    @Multipart
    fun net_uploadImg2FeishuServer(
        @Header("Authorization") token: String,
        @Part type: MultipartBody.Part,
        @Part imgFile: MultipartBody.Part
    ): Flowable<UploadImgResponse>

    @POST("https://open.feishu.cn/open-apis/image/v4/put/")
    @Multipart
    fun net_test(
        @Header("Authorization") token: String,
        @Part type: MultipartBody.Part,
        @Part imgFile: MultipartBody.Part
    ): Flowable<UploadImgResponse>

    /**
     * 发送简易消息至飞书
     */
    @POST
    fun net_postSimpleMsg2FeiShu(
        @Url url: String,
        @Body body: FeiShuObj
    ): Flowable<JsonObject>
}