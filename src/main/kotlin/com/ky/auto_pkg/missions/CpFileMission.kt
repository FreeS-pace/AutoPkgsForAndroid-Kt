package com.ky.auto_pkg.missions

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
    private val mSourceFile: File, private val mTargetPath: String, private val mTargetName: String
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
            LogUtils.LOG_CP, "任务：${mSourceFile} 开始拷贝：\n"
        )

        val targetFile = File(mTargetPath, mTargetName)
        if (!mSourceFile.exists()) {
            throw Exception("Source源文件不存在！${mSourceFile.absolutePath}")
        }
        Files.copy(mSourceFile.toPath(), targetFile.toPath())

        LogUtils.d(
            LogUtils.LOG_CP, "任务：${mSourceFile} 拷贝完成"
        )
    }
}