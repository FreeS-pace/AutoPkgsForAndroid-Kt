package com.ky.auto_pkg.missions

import com.google.gson.JsonObject
import com.ky.auto_pkg.ConfigConstants
import com.ky.auto_pkg.Core
import com.ky.auto_pkg.model.BuildConfig
import com.ky.auto_pkg.model.FeiShuContent
import com.ky.auto_pkg.model.FeiShuObj
import com.ky.auto_pkg.model.ResUploadResult
import com.ky.auto_pkg.utils.LogUtils
import com.ky.auto_pkg.utils.StringUtils
import io.reactivex.rxjava3.subscribers.ResourceSubscriber

/**
 * Created by 王金瑞
 * 2022/3/3
 * 11:58
 * com.ky.auto_pkg.missions
 */
class AutoSendPkgMsgLauncher private constructor() {
    companion object {
        val instance = SingletonHolder.holder
    }

    private object SingletonHolder {
        val holder = AutoSendPkgMsgLauncher()
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
    fun autoSendPkgMsg(buildConfig: BuildConfig) {
        LogUtils.d("", "=====================开始发送消息至飞书Bot流程======================")

        Core.APP_SERVICE.net_postUploadResult2FeiShu(
            ConfigConstants.FEISHU_BOT_WEB_HOOK_URL, buildFeiShuMsgObj(buildConfig)
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

        LogUtils.d("", "=====================发送消息至飞书Bot结束======================\n\n")
    }

    private fun buildFeiShuMsgObj(buildConfig: BuildConfig): FeiShuObj {
        val checkChannels = buildConfig.checkChannels

        // 构建消息obj
        val obj = FeiShuObj("text", FeiShuContent())
        val env = System.getenv()

        // 构建msg
        val sb = StringBuilder(256)

        // 1.title
        sb.append("1 项目：").append(env["JOB_NAME"]).append(" 第").append(env["BUILD_NUMBER"])
            .append("次 构建完成").append("\n\n")

        // 2.构建配置
        sb.append("2 此次构建配置：").append("\n\t").append("2.1 构建分支：").append(env["GIT_BRANCH"])
            .append("\n\t").append("2.2 Android版本号：").append(buildConfig.appVersionCode)
            .append("\n\t").append("2.3 Android版本名：").append(buildConfig.appVersionName)
            .append("\n\t").append("2.4 Unity版本名：").append(buildConfig.unityVersionName)
            .append("\n\t").append("2.5 打包类型：").append(ConfigConstants.ENV_ASSEMBLE_NAME)
            .append("\n\t").append("2.6 服务器环境：").append(ConfigConstants.ENV_SERVER_SERVER_NAME)
            .append("\n\t").append("2.7 是否加固：").append(buildConfig.isJiaGu).append("\n\t")
            .append("2.8 是否使用多渠道(友盟)：").append(buildConfig.isMultiChannel).append("\n\t")
            .append("2.9 是否上传APK至服务器：").append(buildConfig.isSend2Server)

        if (buildConfig.isMultiChannel) {
            sb.append("\n\t").append("2.10 选择的渠道：")
            if (buildConfig.isAllChannel) {
                sb.append("全部")
            } else {
                for (channel in checkChannels!!) {
                    sb.append("\n\t\t").append("渠道名称：").append(channel.name).append(" 上传结果：")
                    if (buildConfig.isSend2Server) {
                        sb.append(if (channel.uploadResult != null) "成功" else "失败")
                    } else {
                        sb.append("未选择上传")
                    }
                }
            }
        }
        sb.append("\n\n")

        // 3.包体下载路径
        sb.append("3：APK下载URL：")
        if (buildConfig.isJiaGu && buildConfig.isMultiChannel && buildConfig.isSend2Server) {
            for (channel in checkChannels!!) {
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
        } else {
            sb.append("\n").append("本地下载目录：").append(ConfigConstants.URL_NGINX_PREFIX)
                .append(System.getenv("JOB_NAME"))
        }

        obj.content.text = sb.toString()
        return obj
    }
}