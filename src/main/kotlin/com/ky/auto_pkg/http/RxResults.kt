package com.ky.auto_pkg.http

import com.ky.auto_pkg.model.FeishuResponse
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.FlowableTransformer

object RxResults {
    /**
     * 处理新接口的统一结果
     */
    fun <T> handleNewResult(): FlowableTransformer<BaseResponse<T>, T> {
        return FlowableTransformer { upstream ->
            upstream.flatMap { (code, error, data) ->
                // 对Utc时间进行转换
                if (code == 200) {
                    // 返回正常数据
                    createFbData<T?>(data)
                } else {
                    Flowable.error(Exception(error))
                }
            }
        }
    }

    fun handleFeishuResult(): FlowableTransformer<FeishuResponse, String> {
        return FlowableTransformer { upstream ->
            upstream.flatMap {
                if (it.code == 200) {
                    // 返回正常数据
                    createFbData(it.tenant_access_token)
                } else {
                    Flowable.error(Exception(it.msg))
                }
            }
        }
    }

    // 生成有背压版Flowable
    fun <T> createFbData(data: T?): Flowable<T> {
        return Flowable.create({ emitter ->
            try {
                if (data != null) {
                    emitter.onNext(data)
                }
                emitter.onComplete()
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }, BackpressureStrategy.BUFFER)
    }
}