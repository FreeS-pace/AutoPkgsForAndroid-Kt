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
        sb.append("cd")
            .append(" ")
            .append(mBuildConfig.jiaGuJarPath.let {
                it.substring(0, it.lastIndexOf(File.separator))
            })
            .append(" && ")
            .append("java")
            .append(" -jar ")
            .append(mBuildConfig.jiaGuJarPath)
            .append(" -login ")
            .append(mBuildConfig.jiaGu360Config!!.userAccount)
            .append(" ")
            .append(mBuildConfig.jiaGu360Config!!.password)
            .append(" && ")
            .append("java")
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
        Core.syncExecProcess(command, "加固成功", "执行加固异常")
    }
}