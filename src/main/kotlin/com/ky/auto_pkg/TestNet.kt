package com.ky.auto_pkg

import com.ky.auto_pkg.model.FeishuResponse
import com.ky.auto_pkg.model.TenantTokenBody
import com.ky.auto_pkg.model.UploadImgResponse
import io.reactivex.rxjava3.subscribers.ResourceSubscriber
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File


/**
 * Created by 王金瑞
 * 2022/4/2
 * 16:01
 * com.ky.auto_pkg
 */
fun main() {
    ConfigConstants.API_BASE_URL = "https://ace-api-dev.timedomain.tech/"
    // 上传图片
    val imgFile = File("/Users/keepyoung/Downloads/image.jpg")
    val imgBody: RequestBody =
        imgFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
    val part = MultipartBody.Part.createFormData("image", imgFile.name, imgBody)
    var textPart = MultipartBody.Part.createFormData("image_type", "message")

    var tentToken: String? = null
    Core.APP_SERVICE.net_getFeishuTenantToken(
        TenantTokenBody(
            "cli_a2cfc169113c500e",
            "AX0THkAvmEfMiwVT3rODVcXKaHbNJg2n"
        )
    ).subscribeWith(object : ResourceSubscriber<FeishuResponse>() {
        override fun onNext(t: FeishuResponse) {
            tentToken = t.tenant_access_token
        }

        override fun onError(t: Throwable?) {
            TODO("Not yet implemented")
        }

        override fun onComplete() {
            TODO("Not yet implemented")
        }

    })

    println("=========================开始上传：" + tentToken)
    Core.APP_SERVICE.net_test(
        "Bearer ${tentToken}",
        textPart,
        part
    )
        .subscribeWith(object : ResourceSubscriber<UploadImgResponse>() {
            override fun onNext(t: UploadImgResponse) {
                println("NetRes：" + t)
            }

            override fun onError(t: Throwable?) {
                t?.printStackTrace()
            }

            override fun onComplete() {
            }

        })
}