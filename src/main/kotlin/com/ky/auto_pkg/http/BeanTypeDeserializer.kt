package com.ky.auto_pkg.http

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * Created by 王金瑞
 * 2021/11/26
 * 20:11
 * com.ky.auto_pkg.http
 */
class BeanTypeDeserializer : JsonDeserializer<BaseResponse<Any>> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): BaseResponse<Any> {
        return try {
            val jObj = json.asJsonObject
            val resultCode = jObj["code"].asInt
            val msg = jObj["msg"].asString
            val timeStamp = jObj["timestamp"].asLong
            var errMsg: String? = null
            if (jObj["error"] != JsonNull.INSTANCE && jObj["error"] != null) {
                errMsg = jObj["error"].asString
            }
            if (resultCode == 200) {
                BaseResponse(
                    resultCode, errMsg, context.deserialize(
                        jObj["data"],
                        (typeOfT as ParameterizedType).actualTypeArguments[0]
                    ), msg, timeStamp
                )
            } else {
                BaseResponse(resultCode, errMsg, Any(), msg, timeStamp)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            BaseResponse(600, "数据序列化错误：" + e.message, Any(), null, 0)
        }
    }

}