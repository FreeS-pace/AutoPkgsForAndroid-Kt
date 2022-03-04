package com.ky.auto_pkg.missions

import com.ky.auto_pkg.Core
import com.ky.auto_pkg.model.BuildConfig
import com.ky.auto_pkg.utils.LogUtils
import java.io.File
import java.util.concurrent.Callable

/**
 * Created by 王金瑞
 * 2022/2/26
 * 15:15
 * com.ky.auto_pkg.missions
 */
class JiaGuMission(
    private val mBuildConfig: BuildConfig,
    private val mApkFile: File
) : Callable<Int> {

    override fun call(): Int {
        try {
            cmdJiaGu()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
        return 1
    }

    /**
     * 进行终端命令加固
     */
    @Throws(Exception::class)
    private fun cmdJiaGu() {
        LogUtils.d(
            LogUtils.LOG_JIAGU, "任务：${mApkFile.name} 开始执行加固：\n"
        )

        val sb = StringBuilder()
        sb.append("java")
            .append(" -jar ")
            .append(mBuildConfig.jiaGuJarPath)
            .append(" -jiagu ")
            .append(mApkFile.absolutePath)
            .append(" ")
            .append(mApkFile.parent)
            .append(" -autosign")
        if (mBuildConfig.isMultiChannel) {
            sb.append(" -automulpkg")
            val customChannelFile: File? = mBuildConfig.customChannelFile
            if (!mBuildConfig.isAllChannel && customChannelFile!!.exists()) {
                sb.append(" -pkgparam")
                    .append(" ")
                    .append(customChannelFile.absolutePath)
            }
        }
        val command = sb.toString()
        LogUtils.d(LogUtils.LOG_JIAGU, "加固命令：$command")
        val process: Process = Core.buildProcess(command)

        // 阻塞直至完成
        val result = process.waitFor()
        if (result != 0) {
            // 说明没有执行成功
            process.destroy()
            throw Exception(LogUtils.LOG_JIAGU_EXCEPTION + "执行加固异常：" + result)
        }
        LogUtils.d(LogUtils.LOG_JIAGU, "任务：" + mApkFile.name + " 执行结果：成功")

        process.destroy()
    }
}