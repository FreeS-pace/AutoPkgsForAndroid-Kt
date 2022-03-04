package com.ky.auto_pkg.utils

import com.ky.auto_pkg.ConfigConstants

/**
 * Created by 王金瑞
 * 2021/12/3
 * 16:59
 * com.ky.auto_pkg.utils
 */
object EnvUtils {
    fun getAssembleName(value: String): String {
        return when (value) {
            ConfigConstants.CONFIG_ASS_ENV_VALUE2
            -> "简体中文_test"
            ConfigConstants.CONFIG_ASS_ENV_VALUE3
            -> "台湾"
            else -> "简体中文"
        }
    }

    fun getServerName(value: String): String {
        return when(value) {
            ConfigConstants.CONFIG_SERVER_ENV_VALUE1
                    -> "测试服"
            ConfigConstants.CONFIG_SERVER_ENV_VALUE3
                    -> "线上服"
            else -> "开发服"
        }
    }
}