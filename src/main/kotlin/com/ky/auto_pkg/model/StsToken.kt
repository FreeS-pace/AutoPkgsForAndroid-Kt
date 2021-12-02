package com.ky.auto_pkg.model

/**
 * Created by 王金瑞
 * 2021/11/29
 * 11:05
 * com.ky.auto_pkg.model
 */
class StsToken {
    private var access_key: String? = null
    private var access_Secret: String? = null
    private var security_token: String? = null
    private var endpoint: String? = null
    private var bucket: String? = null
    private var expiration: String? = null

    override fun toString(): String {
        return "StsToken(access_key=$access_key, access_Secret=$access_Secret, security_token=$security_token, endpoint=$endpoint, bucket=$bucket, expiration=$expiration)"
    }
}