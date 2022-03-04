package com.ky.auto_pkg.utils

import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Created by 王金瑞
 * 2022/2/23
 * 17:13
 * com.ky.auto_pkg.utils
 */
object ZipUtils {
    var sourceFileLen: Int = 0
    var zippingLen: Int = 0
    val splitRegex = "-AutoBuild-"
    var mListener: IZippingListener? = null
    val SIMPLE_DATE_FORMAT = SimpleDateFormat("yyyy_MM_dd_hh_mm_ss")

    fun setListener(listener: IZippingListener) {
        mListener = listener
    }

    // 递归计算文件数量
    @Throws(IOException::class)
    fun calculateFileLen(file: File) {
        if (file.isDirectory) {
            file.listFiles()?.forEach {
                if (it.isDirectory) {
                    calculateFileLen(it)
                } else {
                    sourceFileLen++
                }
            }
        } else {
            sourceFileLen++
        }
    }

    // 开始压缩
    @Throws(IOException::class)
    fun startZipping(sourceFile: File, targetPath: String) {
        sourceFileLen = 0
        zippingLen = 0
        val timeName = SIMPLE_DATE_FORMAT.format(Date(System.currentTimeMillis()))
        val zipFileName = "${sourceFile.name}${splitRegex}${timeName}.zip"
        val zipFilePath = "${targetPath}${File.separator}${zipFileName}"

        val file = File(targetPath)
        if (!file.exists()) {
            file.mkdirs()
        }

        // 先计算所有所有文件数量
        calculateFileLen(sourceFile)

        val zos = ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFilePath)))
        compressZip(zos, sourceFile, sourceFile.name)
        zos.closeEntry()
        zos.close()
    }

    // 递归
    @Throws(IOException::class)
    fun compressZip(zos: ZipOutputStream, file: File, zipFileName: String) {
        if (file.isDirectory) {
            file.listFiles()?.forEach {
                if (it.isDirectory) {
                    compressZip(zos, it, zipFileName + File.separator + it.name)
                } else {
                    zipping(zos, it, zipFileName)
                }
            }
        } else {
            zipping(zos, file, zipFileName)
        }
    }

    @Throws(IOException::class)
    fun zipping(zos: ZipOutputStream, file: File, zipFileName: String) {
        val zipEntry = ZipEntry("${zipFileName}${File.separator}${file.name}")
        zos.putNextEntry(zipEntry)
        val bis = BufferedInputStream(FileInputStream(file))

        val buffer = ByteArray(1024)
        var read: Int
        while (true) {
            read = bis.read(buffer)
            if (read == -1) {
                break
            }
            with(zos) {
                write(buffer, 0, read)
                flush()
            }
        }
        bis.close()
        zippingLen++
        val progress = (zippingLen * 1f / sourceFileLen * 100).toInt()
        LogUtils.d("", "${progress}---${zippingLen}---${sourceFileLen}")
        mListener?.updateProgress(progress)
    }
}