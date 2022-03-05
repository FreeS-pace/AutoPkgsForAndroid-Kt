package com.ky.auto_pkg.model

import java.io.File

/**
 * Created by 王金瑞
 * 2021/8/12
 * 19:33
 * com.wjr.auto_pkgs
 */
data class BuildConfig(
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
    var customChannelFile: File? = null
)