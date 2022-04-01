package com.ky.auto_pkg.utils

/**
 * Created by 王金瑞
 * 2021/8/13
 * 15:52
 * com.wjr.auto_pkgs.utils
 */
object LogUtils {
    const val LOG_JIAGU = "JiaGuLog："
    const val LOG_JIAGU_EXCEPTION = "JiaGuException："
    const val LOG_HTTP = "HttpLog："
    const val LOG_UPLOAD = "UploadLog："
    const val LOG_UPLOAD_EXCEPTION = "UploadException："
    const val LOG_FEISHU = "FeiShuLog："
    const val LOG_FEISHU_EXCEPTION = "FeiShuException："
    const val LOG_CP = "CopyApk："
    const val LOG_CP_EXCEPTION = "CopyApkException："
    const val LOG_FIR = "FirLog："
    const val LOG_FIR_EXCEPTION = "FirException："

    @JvmStatic
    fun d(tag: String, msg: String) {
        println(tag + msg)
    }
}