package com.ky.auto_pkg.model

/**
 * Created by 王金瑞
 * 2022/4/1
 * 11:52
 * com.ky.auto_pkg.model
 */
class FeishuResponse(
    val code: Int,
    val msg: String,
    val tenant_access_token: String,
    val expire: Int
)