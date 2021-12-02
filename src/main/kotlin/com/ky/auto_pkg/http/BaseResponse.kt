package com.ky.auto_pkg.http

/**
 * Created by 王金瑞
 * 2021/11/26
 * 20:02
 * com.ky.auto_pkg.http
 */
class BaseResponse<T> {
    private var code = 0
    private var error: String? = null
    private var data: T? = null
    private var timestamp: Long = 0

    constructor()

    constructor(code: Int, error: String?, data: T, timestamp: Long) {
        this.code = code
        this.error = error
        this.data = data
        this.timestamp = timestamp
    }
}