package com.ky.auto_pkg.missions

import com.ky.auto_pkg.Core
import com.ky.auto_pkg.model.BuildConfig
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
        if (!(mBuildConfig.isJiaGu && mBuildConfig.isMultiChannel && mBuildConfig.isSend2Server)) {
            // 1.创建拷贝任务
            startCpMissions()
            // 2.wait result
            waitResult()
        }

        LogUtils.d("", "\n\n=====================复制APK至Nginx结束======================")
    }

    @Throws(Exception::class)
    private fun startCpMissions() {
        val missions = ArrayList<CpFileMission>(mBuildConfig.checkChannels!!.size)
        for (channel in mBuildConfig.checkChannels!!) {
            missions.add(CpFileMission(channel, mBuildConfig.nginxPath))
        }
        mTasks = Core.THREAD_POOL.invokeAll(missions, 20, TimeUnit.MINUTES)
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
            LogUtils.d(LogUtils.LOG_CP, "所有Apk复制至Nginx完成：${resultCode[0]}")
        } else {
            throw Exception("${LogUtils.LOG_CP_EXCEPTION}复制至Nginx出错：${resultCode[0]}")
        }
    }
}