package com.ky.auto_pkg.model

import java.io.Serializable

/**
 * Created by 王金瑞
 * 2021/8/13
 * 13:55
 * com.wjr.auto_pkgs.model
 */
data class AppChannel(var name: String? = null, var value: String? = null) : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }

    @Transient
    var isCheck = false

    @Transient
    var apkLocalAbsPath: String? = null

    @Transient
    var uploadResult: ResUploadResult? = null
}