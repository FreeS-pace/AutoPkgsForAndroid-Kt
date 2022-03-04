package com.ky.auto_pkg.utils

import com.google.gson.reflect.TypeToken

/**
 * Created by 王金瑞
 * 2022/3/2
 * 17:29
 * com.ky.auto_pkg.utils
 */
object GsonUtils {
    inline fun <reified T> genericType() = object : TypeToken<T>() {}.type!!
}