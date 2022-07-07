package com.ky.auto_pkg.missions

import com.ky.auto_pkg.Core
import com.ky.auto_pkg.model.BuildConfig
import com.ky.auto_pkg.utils.FileUtils
import com.ky.auto_pkg.utils.LogUtils
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

/**
 * Created by 王金瑞
 * 2022/3/3
 * 10:54
 * com.ky.auto_pkg.missions
 */
class AutoOrmNginxLauncher private constructor() {
    companion object {
        val instance = SingletonHolder.holder
    }

    private object SingletonHolder {
        val holder = AutoOrmNginxLauncher()
    }

    private lateinit var mBuildConfig: BuildConfig
    private lateinit var mTasks: List<Future<Int>>

    fun ormNginxProxy(buildConfig: BuildConfig) {
        LogUtils.d("", "\n\n=====================开始复制APK至Nginx路径流程======================")

        mBuildConfig = buildConfig
        // 生成多渠道包的，需要
        startCpMissions()
        // 2.wait result
        waitResult()

        LogUtils.d("", "\n\n=====================复制APK至Nginx结束======================")
    }

    @Throws(Exception::class)
    private fun startCpMissions() {
        val missions = ArrayList<CpFileMission>(2)

        val targetName: String
        val apkSourceFile = if (mBuildConfig.isJiaGu) {
            targetName = "ace_release_jiagu.apk"
            FileUtils.findSourceApkPath(mBuildConfig.sourcePath, "_jiagu_sign.apk")
        } else {
            targetName = "ace_release.apk"
            FileUtils.findSourceApkPath(mBuildConfig.sourcePath, "-release.apk")
        }
        missions.add(CpFileMission(apkSourceFile, mBuildConfig.nginxPath, targetName))
        val mappingSourceFile = FileUtils.findSourceApkPath(mBuildConfig.sourcePath, "mapping.txt")
        missions.add(CpFileMission(mappingSourceFile, mBuildConfig.nginxPath, "mapping.txt"))
        mTasks = Core.THREAD_POOL.invokeAll(missions, 1, TimeUnit.MINUTES)
    }

    @Throws(Exception::class)
    private fun waitResult() {
        val resultCode = arrayOfNulls<Int>(1)
        mTasks.forEach {
            try {
                val result = it.get()
                if (result == null) {
                    throw ExecutionException(Throwable("${LogUtils.LOG_CP_EXCEPTION}执行失败，请查看控制台查找具体信息"))
                } else {
                    resultCode[0] = result
                }
            } catch (e: Exception) {
                e.printStackTrace()
                resultCode[0] = -1
            }
        }
        if (resultCode[0] != -1) {
            LogUtils.d(LogUtils.LOG_CP, "复制至Nginx完成：${resultCode[0]}")
        } else {
            throw Exception("${LogUtils.LOG_CP_EXCEPTION}复制至Nginx出错：${resultCode[0]}")
        }
    }
}