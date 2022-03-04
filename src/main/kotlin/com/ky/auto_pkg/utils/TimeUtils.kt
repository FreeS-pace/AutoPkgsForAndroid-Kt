package com.ky.auto_pkg.utils

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by 王金瑞
 * 2022/2/23
 * 17:02
 * com.ky.auto_pkg.utils
 */
object TimeUtils {
    fun local2UtcStr(localDate: Date): String {
        val localTimeInMillis = localDate.time

        /** long时间转换成Calendar  */
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = localTimeInMillis
        /** 取得时间偏移量 */
        val zoneOffset = calendar[Calendar.ZONE_OFFSET]

        /** 取得夏令时差 */
        val dstOffset = calendar[Calendar.DST_OFFSET]
        /** 从本地时间里扣除这些差量，即可以取得UTC时间*/
        calendar.add(Calendar.MILLISECOND, -(zoneOffset + dstOffset))
        /** 取得的时间就是UTC标准时间 */
        val utcDate = Date(calendar.timeInMillis)
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(utcDate)
    }

    fun local2UTCLong(localDate: Date): Long {
        val localTimeInMillis = localDate.time

        /** long时间转换成Calendar  */
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = localTimeInMillis
        /** 取得时间偏移量  */
        val zoneOffset = calendar[Calendar.ZONE_OFFSET]

        /** 取得夏令时差  */
        val dstOffset = calendar[Calendar.DST_OFFSET]
        /** 从本地时间里扣除这些差量，即可以取得UTC时间 */
        calendar.add(Calendar.MILLISECOND, -(zoneOffset + dstOffset))
        /** 取得的时间就是UTC标准时间  */
        val utcDate = Date(calendar.timeInMillis)
        return utcDate.time
    }

    /**
     *
     * Description: UTC时间转本地时间（计算时差方式）
     */
    fun utc2LocalDate(utcDate: Date): Date? {
        val localTimeInMillis = utcDate.time

        /** long时间转换成Calendar  */
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = localTimeInMillis
        /** 取得时间偏移量  */
        val zoneOffset = calendar[Calendar.ZONE_OFFSET]

        /** 取得夏令时差  */
        val dstOffset = calendar[Calendar.DST_OFFSET]
        /** 从本地时间里扣除这些差量，即可以取得UTC时间 */
        calendar.add(Calendar.MILLISECOND, zoneOffset + dstOffset)
        return Date(calendar.timeInMillis)
    }

    /**
     * utc字符串转utc时间戳
     *
     * @param utcTime utc字符串
     * @return utc时间
     */
    fun utcStr2UtcDate(utcTime: String?): Date? {
        val sf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        return try {
            sf.parse(utcTime)
        } catch (e: ParseException) {
            Date()
        }
    }

    /**
     * utc时间转本地时间
     *
     * @param utcTime utc时间戳
     * @return 本地时间
     */
    fun utc2LocalDate(utcTime: String?): Date? {
        val sf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        val utcZone = TimeZone.getTimeZone("UTC")
        sf.timeZone = utcZone
        return try {
            sf.parse(utcTime)
        } catch (e: ParseException) {
            Date()
        }
    }

    /**
     * utc时间转默认时间
     *
     * @param utcTime utc时间戳
     * @return 本地时间
     */
    fun utc2DefaultDate(utcTime: String?): Date? {
        val sf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        val utcZone = TimeZone.getDefault()
        sf.timeZone = utcZone
        return try {
            sf.parse(utcTime)
        } catch (e: ParseException) {
            Date()
        }
    }

    /**
     * utc时间转成local时间
     */
    fun utc2Local(utcTimeInMillis: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = utcTimeInMillis
        val zoneOffset = calendar[Calendar.ZONE_OFFSET]
        val dstOffset = calendar[Calendar.DST_OFFSET]
        calendar.add(Calendar.MILLISECOND, zoneOffset + dstOffset)
        return calendar.timeInMillis
    }
}