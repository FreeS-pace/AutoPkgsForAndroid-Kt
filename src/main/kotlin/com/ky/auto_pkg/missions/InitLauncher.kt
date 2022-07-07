package com.ky.auto_pkg.missions

import com.ky.auto_pkg.ConfigConstants
import com.ky.auto_pkg.Core
import com.ky.auto_pkg.model.AppChannel
import com.ky.auto_pkg.model.BuildConfig
import com.ky.auto_pkg.model.JiaGu360Config
import com.ky.auto_pkg.model.KeyStoreConfig
import com.ky.auto_pkg.utils.FileUtils
import com.ky.auto_pkg.utils.GsonUtils
import com.ky.auto_pkg.utils.StringUtils
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.*
import java.util.function.Consumer

/**
 * Created by 王金瑞
 * 2022/3/4
 * 14:05
 * com.ky.auto_pkg.missions
 */
class InitLauncher private constructor() {
    companion object {
        val instance = SingletonHolder.holder
    }

    private object SingletonHolder {
        val holder = InitLauncher()
    }

    @Throws(Exception::class)
    fun generateBuildConfig(args: Array<String?>?): BuildConfig {
        if (StringUtils.isEmpties(args) || args!!.size < 11) {
            throw IllegalArgumentException("传入的参数数量不应该 < 11：${Arrays.toString(args)}")
        }
        return BuildConfig().apply {
            /**
             * 路径初始化
             */
            isNotifyGroup = "${args[0]}".toBoolean()
            isPkgTest = "${args[1]}".toBoolean()
            nginxPath = "${args[2]}${File.separator}${System.getenv("JOB_NAME")}"
            baseConfigPath = args[3]!!
            isJiaGu = args[4].toBoolean()
            isSend2Server = args[5].toBoolean()
            val outputPath = args[6]
            val branchTag = args[7]
            val assembleType = args[8]!!
            val serverEnvType = if (isPkgTest) "test" else args[9]!!
            this.assembleType = assembleType
            this.serverEnvType = serverEnvType
            feishuBot = args[10].toString()

            /**
             * 读取配置文件：版本号、版本名称
             */
            val reader = BufferedReader(
                FileReader(
                    File(
                        File(outputPath!!).parentFile,
                        ConfigConstants.CONFIG_ANDROID_GRADLE_FILE_NAME
                    )
                )
            )
            while (true) {
                val readLine = reader.readLine() ?: break
                readLine.apply {
                    if (contains("unitySourceVersion")) {
                        unityVersionName =
                            handleDependentGradleParameter(this, true, "\"", "").trim()
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
            jiaGuJarPath = args[10]!!
            isMultiChannel = args[11].toBoolean()

            val keyStoreConfigPath =
                baseConfigPath + File.separator + ConfigConstants.CONFIG_KEYSTORE_FILE_NAME
            keyStoreConfig =
                Core.GSON.fromJson(FileReader(keyStoreConfigPath), KeyStoreConfig::class.java)
            val jiaGu360ConfigPath =
                baseConfigPath + File.separator + ConfigConstants.CONFIG_JIAGU_360_FILE_NAME
            jiaGu360Config =
                Core.GSON.fromJson(FileReader(jiaGu360ConfigPath), JiaGu360Config::class.java)

            val channels: MutableList<AppChannel>
            if (isJiaGu && isMultiChannel) {
                val type = GsonUtils.genericType<List<AppChannel>>()
                channels = Core.GSON.fromJson(
                    FileReader(baseConfigPath + File.separator + ConfigConstants.CONFIG_DEFAULT_CHANNEL_FILE_NAME),
                    type
                )
                if (args.size > 12 && args[12] != null) {
                    isAllChannel = false
                    val checkChannel =
                        args[12]!!.split(ConfigConstants.CONFIG_DEFAULT_CHANNEL_SPLIT_CHAR)
                    for (selChannelStr in checkChannel) {
                        for (channel in channels) {
                            if (channel.name.equals(selChannelStr)) {
                                channel.isCheck = true
                                break
                            }
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