package com.ky.auto_pkg.missions

import com.google.gson.JsonObject
import com.ky.auto_pkg.ConfigConstants
import com.ky.auto_pkg.Core
import com.ky.auto_pkg.http.RxResults
import com.ky.auto_pkg.model.*
import com.ky.auto_pkg.utils.LogUtils
import com.ky.auto_pkg.utils.StringUtils
import io.reactivex.rxjava3.subscribers.ResourceSubscriber
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File


/**
 * Created by 王金瑞
 * 2022/3/3
 * 11:58
 * com.ky.auto_pkg.missions
 */
class AutoPostFirAndMsgLauncher private constructor() {
    private lateinit var mBuildConfig: BuildConfig

    companion object {
        val instance = SingletonHolder.holder
    }

    private object SingletonHolder {
        val holder = AutoPostFirAndMsgLauncher()
    }

    @Throws(Exception::class)
    fun autoSendPkgMsg(buildConfig: BuildConfig) {
        LogUtils.d("", "=====================开始发送消息至飞书Bot流程======================")

        mBuildConfig = buildConfig
        if (buildConfig.isNotifyGroup) {
            buildGroupMsg()
        } else {
            sendSimpleMsg()
        }

        LogUtils.d("", "=====================发送消息至飞书Bot结束======================\n\n")
    }

    /**
     * 构建群组消息
     */
    @Throws(Exception::class)
    private fun buildGroupMsg() {
        uploadApk2Fir()
        sendGroupMsg()
    }

    /**
     * 上传图片/发送消息
     * 1.获取企业Token
     * 2.上传图片
     * 3.发送Group消息
     */
    @Throws(Exception::class)
    private fun sendGroupMsg() {
        Core.APP_SERVICE.net_getFeishuTenantToken(
            TenantTokenBody(
                ConfigConstants.FEISHU_TENANT_APP_ID,
                ConfigConstants.FEISHU_TENANT_APP_SECRET
            )
        ).compose(RxResults.handleFeishuResult())
            .flatMap { token ->
                // 上传图片
                val imgFile = File(mBuildConfig.nginxPath)
                    .listFiles()!!
                    .filter {
                        it.name.endsWith(".png")
                    }[0]
                val imgBody: RequestBody =
                    imgFile.asRequestBody("image/*".toMediaTypeOrNull())
                val requestBody = MultipartBody.Builder()
                    .addFormDataPart("image_type", "message")
                    .addFormDataPart("image", imgFile.name, imgBody)
                    .build()
                Core.APP_SERVICE.net_uploadImg2FeishuServer("Bearer $token", requestBody)
            }
            .compose(RxResults.handleNewResult())
            .flatMap {
                // 构建Group消息
                val postObj = FeiShuPostObj()
                val obj = FeiShuObj("post", postObj)
                postObj.setTitle("Android构建完成通知")
                // part1
                val part1Data = ArrayList<IPartItemData>()
                part1Data.add(PartTextObj(buildPrefixMsg().toString()))
                postObj.addPartData(part1Data)
                // part2
                postObj.addPartData(buildQRCodeMsg(it.image_key!!))
                // part3
                val part3Data = ArrayList<IPartItemData>()
                part3Data.add(PartTextObj(buildFeiShuChannelObj(StringBuilder()).toString()))
                postObj.addPartData(part3Data)
                println("最终发送：=============：" + postObj)
                // request
                Core.APP_SERVICE.net_postSimpleMsg2FeiShu(
                    ConfigConstants.FEISHU_BOT_WEB_HOOK_URL,
                    obj
                )
            }
            .subscribe(object : ResourceSubscriber<JsonObject>() {
                override fun onNext(t: JsonObject) {
                    LogUtils.d(LogUtils.LOG_FEISHU, "发送飞书消息结果：$t")
                }

                override fun onError(t: Throwable?) {
                    LogUtils.d(LogUtils.LOG_FEISHU_EXCEPTION, "发送飞书报错：")
                    t?.printStackTrace()
                }

                override fun onComplete() {
                }
            })
    }

    /**
     * 上传APk至Fir
     */
    @Throws(Exception::class)
    private fun uploadApk2Fir() {
        val apkPath = File(mBuildConfig.nginxPath).listFiles()!!
            .filter {
                it.name.endsWith(".apk")
            }[0].absolutePath
        val command = StringBuilder().run {
            append("cd")
                .append(" ")
                .append(mBuildConfig.nginxPath)
                .append(" && ")
                .append("/usr/local/bin/fir login -T ${ConfigConstants.FIR_TOKEN}")
                .append(" && ")
                .append("/usr/local/bin/fir publish -S -R $apkPath")
            toString()
        }
        LogUtils.d(LogUtils.LOG_FIR, "上传Fir命令：$command")
        Core.syncExecProcess(command, "上传Fir成功", "上传Fir异常")
    }

    /**
     * 消息发送内容：
     * 1.项目名称
     * 2.构建配置：
     * 构建分支
     * 安卓版本号
     * 安卓版本名称
     * Unity版本名称
     * 打包类型
     * 服务器环境
     * 是否加固
     * 是否使用友盟多渠道
     * 是否需要上传APK至服务器
     * 选择的渠道：全部 / 某些
     * 3.APK下载URL：
     */
    @Throws(Exception::class)
    private fun sendSimpleMsg() {
        // 构建消息体
        val postObj = FeiShuPostObj()
        val obj = FeiShuObj("post", postObj)
        postObj.setTitle("Android构建完成通知")
        val partData = ArrayList<IPartItemData>()
        val itemData = PartTextObj(buildFeiShuChannelObj(buildPrefixMsg()).toString())
        partData.add(itemData)
        postObj.addPartData(partData)

        Core.APP_SERVICE.net_postSimpleMsg2FeiShu(
            ConfigConstants.FEISHU_BOT_WEB_HOOK_URL, obj
        ).subscribe(object : ResourceSubscriber<JsonObject>() {
            override fun onNext(t: JsonObject) {
                LogUtils.d(LogUtils.LOG_FEISHU, "发送飞书消息结果：$t")
            }

            override fun onError(t: Throwable?) {
                LogUtils.d(LogUtils.LOG_FEISHU_EXCEPTION, "发送飞书报错：")
                t?.printStackTrace()
            }

            override fun onComplete() {
            }
        })
    }

    /**
     * 渠道信息
     */
    @Throws(Exception::class)
    private fun buildFeiShuChannelObj(sb: StringBuilder): StringBuilder {
        // 3.包体下载路径
        if (mBuildConfig.isJiaGu && mBuildConfig.isMultiChannel && mBuildConfig.isSend2Server) {
            sb.append("\n\n")
                .append("4：APK渠道信息：")
            for (channel in mBuildConfig.checkChannels!!) {
                sb.append("\n").append(channel.name).append("：")
                val uploadResult: ResUploadResult? = channel.uploadResult
                if (uploadResult == null) {
                    sb.append("上传失败")
                } else {
                    if (!StringUtils.isEmpties(arrayOf(uploadResult.url))) {
                        sb.append("上传成功\n").append("下载地址：").append(uploadResult.url)
                    } else {
                        sb.append("*****上传成功，但上报服务器失败：\n").append("下载地址：").append(uploadResult.url)
                    }
                }
            }
        }
        return sb
    }

    /**
     * 二维码信息
     */
    @Throws(Exception::class)
    private fun buildQRCodeMsg(imgKey: String): ArrayList<IPartItemData> {
        return ArrayList<IPartItemData>().also {
            it.add(PartImgObj(imgKey))
        }
    }

    /**
     * 公共信息
     */
    @Throws(Exception::class)
    private fun buildPrefixMsg(): StringBuilder {
        // 构建消息obj
        val env = System.getenv()

        // 构建msg
        val sb = StringBuilder(256)

        // 1.title
        sb.append("1 项目：").append(env["JOB_NAME"]).append(" 第").append(env["BUILD_NUMBER"])
            .append("次 构建完成").append("\n\n")

        // 2.构建配置
        sb.append("2 此次构建配置：").append("\n\t").append("2.1 构建分支：").append(env["GIT_BRANCH"])
            .append("\n\t").append("2.2 Android版本号：").append(mBuildConfig.appVersionCode)
            .append("\n\t").append("2.3 Android版本名：").append(mBuildConfig.appVersionName)
            .append("\n\t").append("2.4 Unity版本名：").append(mBuildConfig.unityVersionName)
            .append("\n\t").append("2.5 打包类型：").append(ConfigConstants.ENV_ASSEMBLE_NAME)
            .append("\n\t").append("2.6 服务器环境：").append(ConfigConstants.ENV_SERVER_SERVER_NAME)
            .append("\n\t").append("2.7 是否加固：").append(mBuildConfig.isJiaGu).append("\n\t")
            .append("2.8 是否使用多渠道(友盟)：").append(mBuildConfig.isMultiChannel).append("\n\t")
            .append("2.9 是否上传APK至服务器：").append(mBuildConfig.isSend2Server)

        if (mBuildConfig.isMultiChannel) {
            sb.append("\n\t").append("2.10 选择的渠道：")
            if (mBuildConfig.isAllChannel) {
                sb.append("全部")
            } else {
                for (channel in mBuildConfig.checkChannels!!) {
                    sb.append("\n\t\t").append("渠道名称：").append(channel.name).append(" 上传结果：")
                    if (mBuildConfig.isSend2Server) {
                        sb.append(if (channel.uploadResult != null) "成功" else "失败")
                    } else {
                        sb.append("未选择上传")
                    }
                }
            }
        }
        sb.append("\n\n")
        sb.append("3：APK二维码及下载URL：")
        sb.append("\n")
            .append("本地下载目录：")
            .append(ConfigConstants.URL_NGINX_PREFIX)
            .append(System.getenv("JOB_NAME"))
        return sb
    }
}