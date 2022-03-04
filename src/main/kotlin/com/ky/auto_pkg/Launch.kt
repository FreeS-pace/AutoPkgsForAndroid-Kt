package com.ky.auto_pkg

import com.ky.auto_pkg.missions.AutoJiaGuLauncher
import com.ky.auto_pkg.missions.AutoOrmNginxLauncher
import com.ky.auto_pkg.missions.AutoSendPkgMsgLauncher
import com.ky.auto_pkg.missions.AutoUploadLauncher
import com.ky.auto_pkg.model.AppChannel
import com.ky.auto_pkg.model.BuildConfig
import com.ky.auto_pkg.model.KeyStoreConfig
import com.ky.auto_pkg.utils.FileUtils
import com.ky.auto_pkg.utils.GsonUtils
import com.ky.auto_pkg.utils.LogUtils
import com.ky.auto_pkg.utils.StringUtils
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.*
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
        val buildConfig = buildConfigData(args)

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

fun buildConfigData(args: Array<String?>?): BuildConfig {
    if (StringUtils.isEmpties(args) || args!!.size < 11) {
        throw IllegalArgumentException("传入的参数数量 < 11：${Arrays.toString(args)}")
    }
    return BuildConfig().apply {
        /**
         * 路径初始化
         */
        nginxPath = "${args[0]}${File.separator}${System.getenv("JOB_NAME")}"
        baseConfigPath = args[1]!!
        isJiaGu = args[2].toBoolean()
        isSend2Server = args[3].toBoolean()
        val outputPath = args[4]
        val branchTag = args[5]
        val assembleType = args[6]!!
        val serverEnvType = args[7]!!
        this.assembleType = assembleType
        this.serverEnvType = serverEnvType

        /**
         * 读取配置文件：版本号、版本名称
         */
        val reader = BufferedReader(
            FileReader(
                File(
                    File(outputPath!!).parentFile, ConfigConstants.CONFIG_ANDROID_GRADLE_FILE_NAME
                )
            )
        )
        while (true) {
            val readLine = reader.readLine() ?: break
            readLine.apply {
                if (contains("unitySourceVersion")) {
                    unityVersionName = handleDependentGradleParameter(this, true, "\"", "").trim()
                } else if (contains("appVersionCode")) {
                    appVersionCode =
                        handleDependentGradleParameter(this, false, null, null).trim().toInt()
                } else if (contains("appVersionName")) {
                    appVersionName = handleDependentGradleParameter(this, true, "\"", "").trim()
                }
            }
        }
        reader.close()
        if (appVersionCode == 0 || StringUtils.isEmpties(arrayOf(appVersionName))) throw IllegalArgumentException(
            "读取Gradle配置版本号和版本名称错误：${appVersionCode}，${appVersionName}"
        )
        sourcePath = StringBuilder().run {
            append(outputPath)
            if (!outputPath.endsWith(File.separator)) append(File.separator)
            if (branchTag!!.contains("/")) append(branchTag.substring(branchTag.lastIndexOf("/") + 1)) else append(
                branchTag
            )
            if (!branchTag.endsWith("/")) append("/")
            append(assembleType).append("/").append(serverEnvType)
            toString()
        }
        jiaGuJarPath = args[8]!!
        jiagu360JavaPath = args[9]!!
        isMultiChannel = args[10].toBoolean()

        val keyStoreConfigPath =
            baseConfigPath + File.separator + ConfigConstants.CONFIG_KEYSTORE_FILE_NAME
        keyStoreConfig =
            Core.GSON.fromJson(FileReader(keyStoreConfigPath), KeyStoreConfig::class.java)

        val channels: MutableList<AppChannel>
        if (isJiaGu && isMultiChannel) {
            val type = GsonUtils.genericType<List<AppChannel>>()
            channels = Core.GSON.fromJson(
                FileReader(baseConfigPath + File.separator + ConfigConstants.CONFIG_DEFAULT_CHANNEL_FILE_NAME),
                type
            )
            if (args.size > 11 && args[11] != null) {
                isAllChannel = false
                val checkChannel =
                    args[10]!!.split(ConfigConstants.CONFIG_DEFAULT_CHANNEL_SPLIT_CHAR)
                var index = 0
                for (selChannelStr in checkChannel) {
                    while (index < channels.size) {
                        val channel = channels[index]
                        if (channel.name.equals(selChannelStr)) {
                            index++
                            break
                        }
                        index++
                    }
                }
                // 写入自定义模版
                customChannelFile =
                    File(baseConfigPath, ConfigConstants.CONFIG_CUSTOM_CHANNEL_FILE_NAME)
                if (customChannelFile!!.exists()) customChannelFile!!.delete()
                val channelStr = StringBuilder().run {
                    channels.forEach(Consumer {
                        if (it.isCheck) {
                            append(ConfigConstants.CONFIG_DEFAULT_CHANNEL_PLATFORM).append(" ")
                                .append(it.name).append(" ").append(it.value).append("\n")
                        }
                    })
                    toString()
                }
                FileUtils.saveStr2File(channelStr, customChannelFile!!)
            }
        } else {
            channels = ArrayList<AppChannel>(2)
            // build1
            channels.add(AppChannel("未加固Release包", null).apply {
                isCheck = true
            })
            // build2
            if (isJiaGu) channels.add(AppChannel("已加固Release包", null).apply {
                isCheck = true
            })
        }
        if (channels.isEmpty()) {
            throw IllegalArgumentException("打包渠道数量不能为0！")
        }
        appChannels = channels
        checkChannels = FileUtils.getCheckChannels(this)

        // 删除Nginx目录
        val nginxDirFile = File(nginxPath).also {
            if (it.exists()) FileUtils.delDir(it)
        }
        nginxDirFile.mkdirs()

    }
}

fun printConfig2Console(buildConfig: BuildConfig) {
    val str = StringBuilder().run {
        append("==========================================已选加固配置：").append("\n1.是否开启加固：")
            .append(buildConfig.isJiaGu).append("\n2.是否使用多渠道：").append(buildConfig.isMultiChannel)
            .append("\n3.已选渠道：")
        if (buildConfig.isAllChannel) {
            append("全部（默认选项）")
        } else {
            buildConfig.appChannels!!.forEach(Consumer {
                if (it.isCheck) append(it.name).append("，")
            })
            delete(length - 1, length)
        }
        append("\n是否上传至服务器：").append(buildConfig.isSend2Server)
        toString()
    }
    LogUtils.d("", str)
}

fun handleDependentGradleParameter(
    lineStr: String, isRegex: Boolean, regex: String?, replace: String?
): String {
    return lineStr.let {
        val res = it.split(":")[1]
        val subRes = if (res.contains(",")) res.substring(0, res.length - 1) else res
        if (isRegex && subRes.contains(regex!!)) subRes.replace(regex, replace!!) else subRes
    }
}