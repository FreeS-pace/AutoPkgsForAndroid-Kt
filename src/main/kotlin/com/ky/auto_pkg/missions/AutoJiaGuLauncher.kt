package com.ky.auto_pkg.missions

import com.ky.auto_pkg.Core
import com.ky.auto_pkg.model.AppChannel
import com.ky.auto_pkg.model.BuildConfig
import com.ky.auto_pkg.model.KeyStoreConfig
import com.ky.auto_pkg.utils.FileUtils
import com.ky.auto_pkg.utils.LogUtils
import com.ky.auto_pkg.utils.StringUtils
import java.io.File
import java.io.FileFilter
import java.io.IOException
import java.nio.file.Files
import java.util.*
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import java.util.stream.Collectors

/**
 * Created by 王金瑞
 * 2022/2/26
 * 10:37
 * com.ky.auto_pkg.missions
 */
class AutoJiaGuLauncher private constructor() {
    companion object {
        val instance = SingletonHolder.holder
    }

    private object SingletonHolder {
        val holder = AutoJiaGuLauncher()
    }

    private lateinit var mBuildConfig: BuildConfig
    private lateinit var APK_SOURCE_FILE: File
    private lateinit var mTasks: List<Future<Int>>

    @Throws(Exception::class)
    fun autoSign(buildConfig: BuildConfig) {
        mBuildConfig = buildConfig
        if (mBuildConfig.isJiaGu) {
            // 1.找出所有apk文件
            findAllApkSourceFile()
            // 2.开启线程池打包
            startJiaGuMission()
            // 3.wait result
            waitResult()
        }
        // 4.处理Apk
        handleApks()

        LogUtils.d("", "=========================加固流程结束==========================\n\n")
    }

    @Throws(IOException::class)
    private fun findAllApkSourceFile() {
        val file = File(mBuildConfig.sourcePath)
        if (!file.exists() || !file.isDirectory) {
            throw IOException(LogUtils.LOG_JIAGU_EXCEPTION + "传入的Apk目录有误！")
        }
        val files = file.listFiles(FileFilter {
            it.name.endsWith(".apk")
        }) ?: throw IOException("${LogUtils.LOG_JIAGU_EXCEPTION}源文件目录不存在！")
        if (files.isEmpty()) throw IOException("${LogUtils.LOG_JIAGU_EXCEPTION}该目录未生成APK！")
        if (files.size != 1) throw IOException("${LogUtils.LOG_JIAGU_EXCEPTION}当前脚本无法处理多APK（assembleAllRelease）情况！请等待拓展..........")
        APK_SOURCE_FILE = files[0]
    }

    @Throws(InterruptedException::class)
    private fun startJiaGuMission() {
        val missions = ArrayList<JiaGuMission>(1)
        missions.add(JiaGuMission(mBuildConfig, APK_SOURCE_FILE))
        mTasks = Core.THREAD_POOL.invokeAll(missions, 20, TimeUnit.MINUTES)
    }

    @Throws(Exception::class)
    private fun waitResult() {
        val resultCode = arrayOfNulls<Int>(1)
        mTasks.forEach() {
            try {
                val integer: Int = it.get()
                resultCode[0] = integer
            } catch (e: Exception) {
                e.printStackTrace()
                resultCode[0] = -1
            }
        }
        if (resultCode[0] != -1) LogUtils.d(LogUtils.LOG_JIAGU, "所有加固执行完成：${resultCode[0]}")
        else throw Exception("${LogUtils.LOG_JIAGU_EXCEPTION}加固执行出错：${resultCode[0]}")
    }

    @Throws(Exception::class)
    private fun handleApks() {
        LogUtils.d(LogUtils.LOG_JIAGU, "开始检索生成的APK文件...")

        // check data
        val apkDirFile = File(mBuildConfig.sourcePath)
        if (!apkDirFile.exists()
            || apkDirFile.listFiles() == null
        ) {
            throw IOException(LogUtils.LOG_JIAGU_EXCEPTION + "Apk目录有误或为空！")
        }

        val files = apkDirFile.listFiles()!!
        val checkChannels: List<AppChannel> = mBuildConfig.checkChannels!!

        if (mBuildConfig.isJiaGu && mBuildConfig.isMultiChannel) {
            if (files.size != checkChannels.size + 2) {
                throw Exception(
                    LogUtils.LOG_JIAGU_EXCEPTION
                            + "加固多渠道后的Apk目录为空或文件数量不对："
                            + checkChannels
                )
            }
        } else {
            if (mBuildConfig.isJiaGu) {
                if (files.size != 2) {
                    throw Exception(
                        (LogUtils.LOG_JIAGU_EXCEPTION
                                + "加固后的Apk数量 != 2：" + files.size)
                    )
                }
            } else {
                if (files.size != 1) {
                    throw Exception(
                        (LogUtils.LOG_JIAGU_EXCEPTION
                                + "未加固的Apk数量 != 1：" + files.size)
                    )
                }
            }
        }

        val apkFiles = apkDirFile.listFiles()!!
        if (mBuildConfig.isJiaGu) {
            // 1.加固模式下重新检索未签名的APK
            reWriteUnSignApks(apkFiles)
            if (mBuildConfig.isMultiChannel) {
                // 2.找到渠道对应的localAbsPath
                FileUtils.setMultiChannelApkAbsPathByDir(apkFiles, mBuildConfig.checkChannels!!)
            } else {
                // 2.找到加固对应的localAbsPath
                FileUtils.setJiaGuApkAbsPathByDir(
                    apkFiles, mBuildConfig.checkChannels!!,
                    true
                )
            }
        } else {
            // 2.找到未加固对应的localAbsPath
            FileUtils.setJiaGuApkAbsPathByDir(apkFiles, mBuildConfig.checkChannels!!, false)
        }

        // checkApkPath
        for (channel: AppChannel in mBuildConfig.checkChannels!!) {
            if (StringUtils.isEmpties(arrayOf(channel.apkLocalAbsPath))) {
                throw Exception(
                    (LogUtils.LOG_UPLOAD_EXCEPTION + "未找到渠道："
                            + channel.name + "，对应的Apk文件")
                )
            }
        }
    }

    @Throws(Exception::class)
    private fun reWriteUnSignApks(apkFiles: Array<File>) {
        if (apkFiles.size <= 1) {
            throw IOException(LogUtils.LOG_JIAGU_EXCEPTION + "APK加固后生成数量有误！" + apkFiles.size)
        }

        // 1.检索未签名文件
        val unSignFiles = Arrays.stream(apkFiles)
            .filter { file ->
                (file.name.endsWith(".apk")
                        && file.name.contains("_jiagu") && !file.name.contains("_sign"))
            }
            .collect(Collectors.toList())
        if (unSignFiles.size > 0) {
            val commands: MutableList<String> = ArrayList()
            val keyStoreConfig: KeyStoreConfig = mBuildConfig.keyStoreConfig!!
            unSignFiles.forEach(Consumer { file ->
                LogUtils.d(LogUtils.LOG_JIAGU, "文件：" + file.name + "需要重新签名~，请稍后")
                val newApkName = file.name.substring(0, file.name.length - 4) + "_sign.apk"
                val targetPath = file.parent + File.separator + newApkName
                commands.add(
                    "jarsigner -verbose -keystore "
                            + keyStoreConfig.key_path
                            + " -storepass "
                            + keyStoreConfig.key_password
                            + " -signedjar "
                            + targetPath
                            + " "
                            + file.absolutePath
                            + " "
                            + keyStoreConfig.key_alias
                )
            })
            for (command in commands) {
                val process: Process = Core.buildProcess(command)

                // 阻塞直至完成
                val result = process.waitFor()
                if (result != 0) {
                    // 说明没有执行成功
                    process.destroy()
                    throw Exception(LogUtils.LOG_JIAGU + "执行签名异常，程序退出：" + result)
                }
                process.destroy()
            }
            // 删除文件
            // 删除文件
            LogUtils.d(LogUtils.LOG_JIAGU, "所有Apk已签名，删除文件规整中，处理完成...")
            for (file in unSignFiles) {
                Files.delete(file.toPath())
            }
        }
    }
}