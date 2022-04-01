package com.ky.auto_pkg.model

/**
 * Created by 王金瑞
 * 2021/11/29
 * 11:23
 * com.ky.auto_pkg.model
 */
data class FeiShuObj<T>(
    var msg_type: String,
    var content: T
)