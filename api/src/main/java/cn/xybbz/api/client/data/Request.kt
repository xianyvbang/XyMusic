package cn.xybbz.api.client.data

import cn.xybbz.api.utils.convertToMap

open class Request {

    /**
     * 将对象转换成map
     * @param [isConvertList] 是否转化list为逗号(',')分割的字符串
     * @return [Map<String, Any>]
     */
    fun toMap(): Map<String, String> {
        return convertToMap(this,true)
    }

    /**
     * 将对象转换成map
     * @param [isConvertList] 是否转化list为逗号(',')分割的字符串
     * @return [Map<String, Any>]
     */
    fun toMap(isConvertList: Boolean = false): Map<String, Any> {
        return convertToMap(this,isConvertList)
    }
}