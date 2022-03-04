package com.ky.auto_pkg.model

/**
 * Created by 王金瑞
 * 2021/11/29
 * 11:05
 * com.ky.auto_pkg.model
 */
class StsToken(
    var access_key: String? = null,
    var access_Secret: String? = null,
    var security_token: String? = null,
    var endpoint: String? = null,
    var bucket: String? = null,
    var expiration: String? = null
)