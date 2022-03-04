package com.ky.auto_pkg.model

/**
 * Created by 王金瑞
 * 2021/11/29
 * 11:16
 * com.ky.auto_pkg.model
 */
data class UploadServerBody(
    val version_code: Int,
    val version_name: String,
    var file_url: HashMap<String, String>? = null
)