package com.ky.auto_pkg.utils

import com.ky.auto_pkg.Core
import com.ky.auto_pkg.model.AppChannel
import com.ky.auto_pkg.model.BuildConfig
import java.io.*
import java.util.stream.Collectors

/**
 * Created by 王金瑞
 * 2021/12/3
 * 18:00
 * com.ky.auto_pkg.utils
 */
object FileUtils {
    @Throws(IOException::class)
    fun writeContent(br: BufferedReader) {
        var line = br.readLine()
        while (line != null) {
            LogUtils.d("", line)
            line = br.readLine()
        }
        br.close()
    }

    fun writeAsyncContent(br: BufferedReader) {
        Core.THREAD_POOL.submit {
            try {
                writeContent(br)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * saveStr 2 File
     */
    @Throws(IOException::class)
    fun saveStr2File(content: String, targetFile: File) {
        val writer = BufferedWriter(FileWriter(targetFile, false))
        writer.write(content)
        writer.flush()
        writer.close()
    }

    /**
     * 根据Channel获取FileName
     */
    fun getFileNameByChannel(path: String, channelName: String): String {
        return channelName + path.substring(path.lastIndexOf("."))
    }

    /**
     * 找出多渠道对应的apk路径
     */
    fun setMultiChannelApkAbsPathByDir(files: Array<File>, channels: List<AppChannel>) {
        for (channel in channels) {
            findApkByFiles(files, channel, channel.name!!, true)
        }
    }

    /**
     * 找出非多渠道对应的apk路径
     */
    fun setJiaGuApkAbsPathByDir(
        files: Array<File>,
        channels: List<AppChannel>,
        isJiaGu: Boolean
    ) {
        val sourceChannel = channels[0]
        findApkByFiles(files, sourceChannel, "-release", false)

        if (isJiaGu) {
            val jiaGuChannel = channels[1]
            findApkByFiles(files, jiaGuChannel, "_jiagu_sign", true)
        }
    }

    private fun findApkByFiles(
        files: Array<File>,
        channel: AppChannel,
        keyStr: String,
        isJiaGu: Boolean
    ) {
        for (file in files) {
            val name = file.name
            if (name.contains(keyStr) && (isJiaGu || !name.contains("_jiagu_sign"))) {
                channel.apkLocalAbsPath = file.absolutePath
            }
        }
    }

    fun findSourceApkPath(path: String, suffix: String): File {
        return File(path)
            .listFiles()!!
            .filter {
                it.name.endsWith(suffix)
            }[0]
    }

    /**
     * 获取选中的渠道
     */
    fun getCheckChannels(buildConfig: BuildConfig): List<AppChannel> {
        return if (buildConfig.isJiaGu && buildConfig.isAllChannel) {
            buildConfig.appChannels!!
        } else {
            buildConfig.appChannels!!
                .stream()
                .filter {
                    it.isCheck
                }
                .collect(Collectors.toList())
        }
    }

    /**
     * 递归删除文件夹里内容
     */
    fun delDir(file: File) {
        if (file.isDirectory) {
            val listFiles = file.listFiles()
            for (childFile in listFiles) {
                delDir(childFile)
            }
            file.delete()
        } else {
            file.delete()
        }
    }
}