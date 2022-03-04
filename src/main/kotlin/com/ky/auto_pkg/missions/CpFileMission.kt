package com.ky.auto_pkg.missions

import com.ky.auto_pkg.ConfigConstants
import com.ky.auto_pkg.model.AppChannel
import com.ky.auto_pkg.model.ResUploadResult
import com.ky.auto_pkg.utils.FileUtils
import com.ky.auto_pkg.utils.LogUtils
import java.io.File
import java.nio.file.Files
import java.util.concurrent.Callable

/**
 * Created by 王金瑞
 * 2022/3/3
 * 11:02
 * com.ky.auto_pkg.missions
 */
class CpFileMission(
    private val mAppChannel: AppChannel,
    private val mTargetPath: String
) : Callable<Int> {

    override fun call(): Int {
        try {
            cpFiles()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
        return 1
    }

    @Throws(Exception::class)
    private fun cpFiles() {
        LogUtils.d(
            LogUtils.LOG_CP, "任务：${mAppChannel.name} 开始拷贝：\n"
        )

        val targetFileName =
            FileUtils.getFileNameByChannel(mAppChannel.apkLocalAbsPath!!, mAppChannel.name!!)
        val sourceFile = File(mAppChannel.apkLocalAbsPath!!)
        val targetFile = File(mTargetPath, targetFileName)
        if (!sourceFile.exists()) {
            throw Exception("Source源文件不存在！${sourceFile.absolutePath}")
        }
        Files.copy(sourceFile.toPath(), targetFile.toPath())

        val uploadResult: ResUploadResult
        if (mAppChannel.uploadResult != null) {
            uploadResult = mAppChannel.uploadResult!!
        } else {
            uploadResult = ResUploadResult()
            mAppChannel.uploadResult = uploadResult
        }
        uploadResult.url = ConfigConstants.URL_NGINX_PREFIX + targetFileName
    }
}