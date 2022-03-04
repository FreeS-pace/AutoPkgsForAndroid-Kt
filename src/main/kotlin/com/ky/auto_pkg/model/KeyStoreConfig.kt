package com.ky.auto_pkg.model

import java.io.Serializable

/**
 * Created by 王金瑞
 * 2021/8/12
 * 19:15
 * com.wjr.auto_pkgs
 */
data class KeyStoreConfig(
    var key_path: String = "",
    var store_password: String = "",
    var key_alias: String = "",
    var key_password: String = ""
) : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
}