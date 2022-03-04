package com.ky.auto_pkg.listener

import com.aliyun.oss.event.ProgressEvent
import com.aliyun.oss.event.ProgressEventType
import com.aliyun.oss.event.ProgressListener
import com.ky.auto_pkg.model.AppChannel
import com.ky.auto_pkg.utils.LogUtils

/**
 * Created by 王金瑞
 * 2022/3/2
 * 13:50
 * com.ky.auto_pkg.listener
 */
class UploadObjectProgressListener(private val mAppChannel: AppChannel) : ProgressListener {
    private var mBytesWritten: Long = 0L
    private var mTotalBytes: Long = -1L
    private var mProgress: Float = 0f
    private val mMinNotifyGap = 5f
    private var mSucceed = false

    override fun progressChanged(progressEvent: ProgressEvent) {
        val bytes = progressEvent.bytes
        when (progressEvent.eventType) {
            ProgressEventType.TRANSFER_STARTED_EVENT ->
                LogUtils.d(
                    LogUtils.LOG_UPLOAD,
                    "任务：${mAppChannel.name} 开始上传：${mAppChannel.apkLocalAbsPath}"
                )
            ProgressEventType.REQUEST_CONTENT_LENGTH_EVENT -> {
                mTotalBytes = bytes
                LogUtils.d(
                    LogUtils.LOG_UPLOAD,
                    "任务：：${mAppChannel.name} 上传文件大小为：${(mTotalBytes * 1F / 1024 / 1024)}mb"
                )
            }
            ProgressEventType.REQUEST_BYTE_TRANSFER_EVENT -> {
                mBytesWritten += bytes
                if (mTotalBytes != -1L) {
                    val percent = mBytesWritten * 100F / mTotalBytes
                    if (percent == 0f || percent == 100f || mProgress - percent > mMinNotifyGap) {
                        mProgress = percent
                        LogUtils.d(
                            LogUtils.LOG_UPLOAD,
                            "任务：${mAppChannel.name} 已上传 -> ${mProgress}%"
                        )
                    }
                } else {
                    LogUtils.d(
                        LogUtils.LOG_UPLOAD,
                        "任务：${mAppChannel.name} 未获取到该文件的总大小（分母），无进度可计算..."
                    )
                }
            }
            ProgressEventType.TRANSFER_COMPLETED_EVENT -> {
                mSucceed = true
                LogUtils.d(
                    LogUtils.LOG_UPLOAD,
                    "任务：${mAppChannel.name} 上传完成 -> 已上传大小：${mBytesWritten * 1F / 1024 / 1024}mb，" +
                            "共需上传大小：${mTotalBytes * 1F / 1024 / 1024}mb"
                )
            }
            ProgressEventType.TRANSFER_FAILED_EVENT ->
                LogUtils.d(
                    LogUtils.LOG_UPLOAD,
                    "任务：${mAppChannel.name} 上传失败，请查看失败原因 -> 已上传大小：${mBytesWritten}字节"
                )
            else -> {}
        }
    }
}