package com.ky.auto_pkg

import com.ky.auto_pkg.missions.*
import com.ky.auto_pkg.model.BuildConfig
import com.ky.auto_pkg.utils.LogUtils
import java.util.function.Consumer
import kotlin.system.exitProcess

/**
 * Created by 王金瑞
 * 2021/11/26
 * 19:47
 * com.ky.auto_pkg
 */
fun main(args: Array<String?>?) {
    var result = 0
    try {
        LogUtils.d("", "=================================开始处理APK==================================")
        // 1.generate config
        val buildConfig = InitLauncher.instance.generateBuildConfig(args)

        // 2.初始化配置
        Core.initConfigProperties(buildConfig)

        // 3.打印配置信息
        printConfig2Console(buildConfig)

        // 4.预开启线程池
        if (buildConfig.isMultiChannel) Core.THREAD_POOL.corePoolSize =
            buildConfig.checkChannels!!.size
        else Core.THREAD_POOL.corePoolSize = 2
        Core.THREAD_POOL.prestartCoreThread()

        // 5.打包+签名+多渠道
        AutoJiaGuLauncher.instance.autoSign(buildConfig)

        // 6.上传Oss，通知后台：加固 && 多渠道 && 通知服务器
        if (buildConfig.isJiaGu && buildConfig.isMultiChannel && buildConfig.isSend2Server) {
            AutoUploadLauncher.instance.autoUpload(buildConfig)
        }

        // 7.将文件拷贝至Nginx目录下
        AutoOrmNginxLauncher.instance.ormNginxProxy(buildConfig)

        // 8.通知飞书
        AutoSendPkgMsgLauncher.instance.autoSendPkgMsg(buildConfig)
    } catch (e: Exception) {
        e.printStackTrace()
        result = -1
    } finally {
        Core.THREAD_POOL.shutdownNow()
        exitProcess(result)
    }
}

fun printConfig2Console(buildConfig: BuildConfig) {
    val str = StringBuilder().run {
        append("==========================================已选打包配置：")
            .append("\n1.是否为打包测试模式：")
            .append(buildConfig.isPkgTest)
            .append("\n2.是否开启加固：")
            .append(buildConfig.isJiaGu)
            .append("\n3.是否使用多渠道：")
            .append(buildConfig.isMultiChannel)
            .append("\n4.已选渠道：")
        if (buildConfig.isAllChannel) {
            append("全部（默认选项）")
        } else {
            buildConfig.appChannels!!.forEach(Consumer {
                if (it.isCheck) append(it.name).append("，")
            })
            delete(length - 1, length)
        }
        append("\n5.是否上传至服务器：")
            .append(buildConfig.isSend2Server)
        toString()
    }
    LogUtils.d("", str)
}