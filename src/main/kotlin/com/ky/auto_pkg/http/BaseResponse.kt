package com.ky.auto_pkg.http

/**
 * Created by 王金瑞
 * 2021/11/26
 * 20:02
 * com.ky.auto_pkg.http
 */
data class BaseResponse<T>(
    var code: Int = 0,
    var error: String? = null,
    var data: T? = null,
    var msg: String? = null,
    var timestamp: Long = 0
)