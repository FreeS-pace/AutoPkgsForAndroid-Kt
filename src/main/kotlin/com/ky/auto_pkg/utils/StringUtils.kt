package com.ky.auto_pkg.utils

/**
 * Created by 王金瑞
 * 2022/2/23
 * 16:55
 * com.ky.auto_pkg.utils
 */
object StringUtils {
    fun isEmpties(values: Array<String?>?): Boolean {
        if (values != null) {
            for (str in values) {
                if (str == null || str.trim() == "") {
                    return true
                }
            }
        }
        return false
    }
}