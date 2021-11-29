package com.ky.auto_pkg

import com.google.gson.GsonBuilder
import com.ky.auto_pkg.http.BaseResponse
import com.ky.auto_pkg.http.BeanTypeDeserializer

/**
 * Created by 王金瑞
 * 2021/11/26
 * 19:58
 * com.ky.auto_pkg
 */
class Core {
    companion object {
        var GSON = GsonBuilder()
            .registerTypeAdapter(BaseResponse::class.java, BeanTypeDeserializer())
            .serializeNulls()
            .create()
    }
}