package com.ky.auto_pkg.missions

import com.aliyun.oss.OSS
import com.ky.auto_pkg.Core
import com.ky.auto_pkg.http.RxResults
import com.ky.auto_pkg.model.*
import com.ky.auto_pkg.utils.LogUtils
import com.ky.auto_pkg.utils.TimeUtils
import io.reactivex.rxjava3.subscribers.ResourceSubscriber
import java.util.*
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors

/**
 * Created by 王金瑞
 * 2022/2/26
 * 18:30
 * com.ky.auto_pkg.missions
 */
class AutoUploadLauncher private constructor() {
    companion object {
        val instance = SingletonHolder.holder
    }

    private object SingletonHolder {
        val holder = AutoUploadLauncher()
    }

    private lateinit var mBuildConfig: BuildConfig
    private lateinit var mStsToken: StsToken
    private lateinit var mOSSClient: OSS
    private lateinit var mTasks: List<Future<AppChannel>>
    private lateinit var mUploadFailCollect: List<AppChannel>

    @Throws(Exception::class)
    fun autoUpload(config: BuildConfig) {
        LogUtils.d("", "=====================开始上传APK至Server流程======================")

        try {
            mBuildConfig = config
            // 1.请求Oss临时Token
            requestOssTokenByServer()
            // 2.创建阿里云Oss对象
            mOSSClient = Core.createOssClient(mStsToken)
            // 3.开始并发上传任务
            startConcurrentUploadApks()
            // 4.阻塞等待结果
            waitResult()
            // 5.上报服务器
            sendResult2Server()

            mOSSClient.shutdown()

            LogUtils.d("", "=====================上传APK至Server结束======================\n\n")
        } catch (e: Exception) {
            if (this::mOSSClient.isInitialized) {
                mOSSClient.shutdown()
            }
            throw e
        }
    }

    @Throws(Exception::class)
    private fun requestOssTokenByServer() {
        val resultCode = intArrayOf(1)
        Core.APP_SERVICE.net_getOssToken()
            .compose(RxResults.handleNewResult())
            .subscribe(object : ResourceSubscriber<StsTokenContainer>() {
                override fun onNext(t: StsTokenContainer) {
                    mStsToken = t.sts_token!!
                }

                override fun onError(t: Throwable?) {
                    t?.printStackTrace()
                    resultCode[0] = -1
                }

                override fun onComplete() {
                }
            })
        if (resultCode[0] != 1) {
            throw Exception(LogUtils.LOG_UPLOAD_EXCEPTION + "请求Oss Token失败，请查看ConsoleLog")
        }
        LogUtils.d(LogUtils.LOG_UPLOAD, "获取OSS Token成功：$mStsToken")
    }

    @Throws(Exception::class)
    private fun startConcurrentUploadApks() {
        var currUtcTime = TimeUtils.local2UTCLong(Date())
        val missions = ArrayList<UploadMission>(mBuildConfig.checkChannels!!.size)
        for (channel in mBuildConfig.checkChannels!!) {
            missions.add(UploadMission(mOSSClient, mStsToken, channel, currUtcTime))
            currUtcTime += 2
        }

        LogUtils.d(LogUtils.LOG_UPLOAD, "要上传的Apk渠道：${mBuildConfig.checkChannels}")

        mTasks = Core.THREAD_POOL.invokeAll(missions, 30, TimeUnit.MINUTES)
    }

    /**
     * 阻塞等待
     */
    @Throws(Exception::class)
    private fun waitResult() {
        mTasks.forEach {
            try {
                it.get()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        // 检索结果
        mUploadFailCollect = mBuildConfig.checkChannels!!.stream()
            .filter {
                it.uploadResult == null
            }
            .collect(Collectors.toList())
        if (mBuildConfig.checkChannels!!.size == mUploadFailCollect.size) {
            throw Exception("${LogUtils.LOG_UPLOAD_EXCEPTION}所有已选的渠道包全部上传失败：${mBuildConfig.checkChannels}")
        }
    }

    /**
     * 上报服务器
     */
    @Throws(Exception::class)
    private fun sendResult2Server() {
        LogUtils.d(LogUtils.LOG_UPLOAD, "==============上传结果==============")

        val uploadServerBody =
            UploadServerBody(mBuildConfig.appVersionCode, mBuildConfig.appVersionName).apply {
                file_url = HashMap(mBuildConfig.checkChannels!!.size - mUploadFailCollect.size)
                for (channel in mBuildConfig.checkChannels!!) {
                    if (channel.uploadResult != null) {
                        file_url!![channel.value!!] = channel.uploadResult!!.path!!
                        LogUtils.d(
                            LogUtils.LOG_UPLOAD,
                            "========渠道：" + channel.name + " 上传成功，OssUrl："
                                    + channel.uploadResult!!.path
                        )
                    } else {
                        LogUtils.d(
                            LogUtils.LOG_UPLOAD_EXCEPTION,
                            "========渠道：" + channel.name + " 上传失败"
                        )
                    }
                }
            }

        LogUtils.d(LogUtils.LOG_HTTP, "================准备上报Server：$uploadServerBody")

        val resultCode = intArrayOf(1)
        Core.APP_SERVICE.net_postUploadResult2Server(uploadServerBody)
            .compose(RxResults.handleNewResult())
            .subscribe(object : ResourceSubscriber<HashMap<String, String>>() {
                override fun onNext(t: HashMap<String, String>) {
                    // 根据server返回结果重新赋值UploadResult
                    for (channel in mBuildConfig.checkChannels!!) {
                        if (channel.uploadResult != null && t.containsKey(channel.value)) {
                            channel.uploadResult!!.url = t[channel.value]
                        }
                    }
                }

                override fun onError(t: Throwable?) {
                    t?.printStackTrace()
                    resultCode[0] = -1
                }

                override fun onComplete() {
                }
            })
        if (resultCode[0] != 1) {
            throw Exception("${LogUtils.LOG_UPLOAD_EXCEPTION}资源上报完成失败：请查看ConsoleLog：${uploadServerBody}")
        }
    }
}