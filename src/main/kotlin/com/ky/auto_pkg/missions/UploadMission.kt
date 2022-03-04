package com.ky.auto_pkg.missions

import com.aliyun.oss.OSS
import com.aliyun.oss.internal.OSSUtils
import com.aliyun.oss.model.Callback
import com.aliyun.oss.model.PutObjectRequest
import com.ky.auto_pkg.ConfigConstants
import com.ky.auto_pkg.Core
import com.ky.auto_pkg.http.BaseResponse
import com.ky.auto_pkg.listener.UploadObjectProgressListener
import com.ky.auto_pkg.model.AppChannel
import com.ky.auto_pkg.model.ResUploadResult
import com.ky.auto_pkg.model.StsToken
import com.ky.auto_pkg.utils.GsonUtils
import com.ky.auto_pkg.utils.LogUtils
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.concurrent.Callable

/**
 * Created by 王金瑞
 * 2022/2/26
 * 20:21
 * com.ky.auto_pkg.missions
 */
class UploadMission(
    private val mOssClient: OSS,
    private val mStsToken: StsToken,
    private val mAppChannel: AppChannel,
    private val mUtcTime: Long
) : Callable<AppChannel> {

    init {
        LogUtils.d(LogUtils.LOG_UPLOAD, "任务：" + mAppChannel.name + "渠道准备就绪")
    }

    override fun call(): AppChannel {
        uploadApk()
        return mAppChannel
    }

    @Throws(Exception::class)
    private fun uploadApk() {
        var contentStream: InputStream? = null
        try {
            val remoteRelativePath = ConfigConstants.RES_UPLOAD_PATH_APK + mUtcTime + ".apk"
            val apkFile = File(mAppChannel.apkLocalAbsPath!!)
            val request = PutObjectRequest(mStsToken.bucket, remoteRelativePath, apkFile)
                .withProgressListener<PutObjectRequest>(UploadObjectProgressListener(mAppChannel))

            // buildCallback
            request.callback = Callback().apply {
                callbackUrl =
                    ConfigConstants.API_BASE_URL + "api/ace/oss/callback?mode=" + ConfigConstants.RES_UPLOAD_MODE_APK + "&filename=" + remoteRelativePath
                callbackBody = ("{\\\"mimeType\\\":application/vnd.android.package-archive,"
                        + "\\\"size\\\":" + apkFile.length() + "}")
                calbackBodyType = Callback.CalbackBodyType.JSON
            }

            LogUtils.d(
                LogUtils.LOG_UPLOAD,
                "任务：" + mAppChannel.name + " 回调JsonBody2：" + OSSUtils.jsonizeCallback(request.callback)
            )

            val responseMsg = mOssClient.putObject(request).response
            if (responseMsg.statusCode != 200) {
                throw Exception("${LogUtils.LOG_UPLOAD_EXCEPTION} Oss文件上传失败：${mAppChannel.apkLocalAbsPath}")
            }
            contentStream = responseMsg.content
            val reader = InputStreamReader(contentStream!!, StandardCharsets.UTF_8)
            val serverResponse = Core.GSON.fromJson<BaseResponse<ResUploadResult>>(
                reader,
                GsonUtils.genericType<BaseResponse<ResUploadResult>>()
            )

            contentStream.close()
            contentStream = null

            if (serverResponse.code != 200 || serverResponse.data == null) {
                throw Exception(
                    "${LogUtils.LOG_UPLOAD_EXCEPTION} " +
                            "Oss返回Response错误：${serverResponse.error}" +
                            "，文件：${mAppChannel.apkLocalAbsPath}"
                )
            }

            serverResponse.data!!.path = "${mStsToken.endpoint}/${remoteRelativePath}"
            mAppChannel.uploadResult = serverResponse.data

            LogUtils.d(
                LogUtils.LOG_UPLOAD, "任务：" + mAppChannel.name + " 上传完成=================="
            )
        } catch (e: Exception) {
            contentStream?.close()
            throw e
        }
    }
}