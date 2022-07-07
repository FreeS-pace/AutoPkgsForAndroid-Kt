package com.ky.auto_pkg.model

import java.io.File

/**
 * Created by 王金瑞
 * 2021/8/12
 * 19:33
 * com.wjr.auto_pkgs
 */
data class BuildConfig(
    var isNotifyGroup: Boolean = true,
    var isPkgTest: Boolean = false,
    var nginxPath: String = "",
    var baseConfigPath: String = "",
    var isJiaGu: Boolean = false,
    var isSend2Server: Boolean = false,
    var appVersionCode: Int = 0,
    var appVersionName: String = "",
    var unityVersionName: String = "",
    var assembleType: String = "",
    var serverEnvType: String = "",
    var sourcePath: String = "",
    var jiaGuJarPath: String = "",
    var isMultiChannel: Boolean = false,
    var keyStoreConfig: KeyStoreConfig? = null,
    var jiaGu360Config: JiaGu360Config? = null,
    var appChannels: List<AppChannel>? = null,
    var checkChannels: List<AppChannel>? = null,
    var isAllChannel: Boolean = true,
    var customChannelFile: File? = null,
    var feishuBot: String = "0bf5558e-b3e4-48c0-bd5e-15d1ad38f035"
)