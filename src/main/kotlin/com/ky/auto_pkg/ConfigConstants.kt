package com.ky.auto_pkg

/**
 * Created by 王金瑞
 * 2021/8/14
 * 11:50
 * com.wjr.auto_pkgs
 */
object ConfigConstants {
    lateinit var ENV_ASSEMBLE_NAME: String
    lateinit var ENV_SERVER_SERVER_NAME: String
    lateinit var URL_NGINX_PREFIX: String
    lateinit var API_BASE_URL: String

    lateinit var TOKEN: String
    lateinit var FEISHU_BOT_WEB_HOOK_URL: String

    // 默认文件名称及关键字名称，不可更改！
    const val CONFIG_URL_CONFIG_FILE_NAME = "url_config.properties"
    const val CONFIG_ANDROID_GRADLE_FILE_NAME = "dependentConfig.gradle"
    const val CONFIG_KEYSTORE_FILE_NAME = "ace_android_key_config.json"
    const val CONFIG_DEFAULT_CHANNEL_FILE_NAME = "ace_app_channel_config.json"
    const val CONFIG_CUSTOM_CHANNEL_FILE_NAME = "ace_custom_channel.txt"
    const val CONFIG_DEFAULT_CHANNEL_PLATFORM = "UMENG_CHANNEL"
    const val CONFIG_DEFAULT_CHANNEL_SPLIT_CHAR = "&"

    // Properties文件中的Key名
    const val CONFIG_URL_NGINX_KEY = "URL_NGINX"
    const val CONFIG_API_KEY_PREFIX = "URL_"
    const val CONFIG_TOKEN_KEY_PREFIX = "TOKEN_"
    const val CONFIG_FEISHU_URL_KEY = "FEISHU_BOT_HOOK_URL_TEST"
    const val CONFIG_ASS_ENV_VALUE1 = "JTZW"
    const val CONFIG_ASS_ENV_VALUE2 = "JTZW_TEST"
    const val CONFIG_ASS_ENV_VALUE3 = "TW"
    const val CONFIG_SERVER_ENV_VALUE1 = "CSF"
    const val CONFIG_SERVER_ENV_VALUE2 = "KFF"
    const val CONFIG_SERVER_ENV_VALUE3 = "XSF"

    // Oss Remote 远程地址
    const val RES_UPLOAD_PATH_APK = "app/release/android/"
    const val RES_UPLOAD_MODE_APK = "android_apk"
}