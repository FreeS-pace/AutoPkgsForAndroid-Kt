package com.ky.auto_pkg.model

/**
 * Created by 王金瑞
 * 2021/11/29
 * 11:16
 * com.ky.auto_pkg.model
 */
class UploadServerBody {
    private var version_code = 0
    private var version_name: String? = null
    private var file_url: Map<String, String>? = null
    
    override fun toString(): String {
        return "UploadServerBody(version_code=$version_code, version_name=$version_name, file_url=$file_url)"
    }
}