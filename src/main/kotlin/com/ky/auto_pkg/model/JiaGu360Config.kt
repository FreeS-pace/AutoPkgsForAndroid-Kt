package com.ky.auto_pkg.model

import java.io.Serializable

/**
 * Created by 王金瑞
 * 2022/3/5
 * 14:32
 * com.ky.auto_pkg.model
 */
data class JiaGu360Config(
    var userAccount: String? = null,
    var password: String? = null
) : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
}