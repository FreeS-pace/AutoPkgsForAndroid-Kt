package com.ky.auto_pkg;

/**
 * Created by 王金瑞
 * 2021/8/14
 * 11:50
 * com.wjr.auto_pkgs
 */
public class ConfigConstants {
    public static String ENV_ASSEMBLE_NAME;
    public static String ENV_SERVER_SERVER_NAME;

    public static String URL_NGINX_PREFIX;
    public static String API_BASE_URL;
    public static String TOKEN;

    public static String FEISHU_BOT_WEB_HOOK_URL;

    // 默认文件名称及关键字名称，不可更改！
    public static final String CONFIG_URL_CONFIG_FILE_NAME = "url_config.properties";
    public static final String CONFIG_ANDROID_GRADLE_FILE_NAME = "dependentConfig.gradle";
    public static final String CONFIG_KEYSTORE_FILE_NAME = "ace_android_key_config.json";
    public static final String CONFIG_DEFAULT_CHANNEL_FILE_NAME = "ace_app_channel_config.json";
    public static final String CONFIG_CUSTOM_CHANNEL_FILE_NAME = "ace_custom_channel.txt";
    public static final String CONFIG_DEFAULT_CHANNEL_PLATFORM = "UMENG_CHANNEL";
    public static final String CONFIG_DEFAULT_CHANNEL_SPLIT_CHAR = "&";

    // Properties文件中的Key名
    public static final String CONFIG_URL_NGINX_KEY = "URL_NGINX";
    public static final String CONFIG_API_KEY_PREFIX = "URL_";
    public static final String CONFIG_TOKEN_KEY_PREFIX = "TOKEN_";
    public static final String CONFIG_FEISHU_URL_KEY = "FEISHU_BOT_HOOK_URL_TEST";
    public static final String CONFIG_ASS_ENV_VALUE1 = "JTZW";
    public static final String CONFIG_ASS_ENV_VALUE2 = "JTZW_TEST";
    public static final String CONFIG_ASS_ENV_VALUE3 = "TW";
    public static final String CONFIG_SERVER_ENV_VALUE1 = "CSF";
    public static final String CONFIG_SERVER_ENV_VALUE2 = "KFF";
    public static final String CONFIG_SERVER_ENV_VALUE3 = "XSF";

    // Oss Remote 远程地址
    public static final String RES_UPLOAD_PATH_APK = "app/release/android/";
    public static final String RES_UPLOAD_MODE_APK = "android_apk";
}
