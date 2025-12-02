package cn.xybbz.api.client.data

import cn.xybbz.api.utils.convertToMap

open class Request {

    /**
     * 将对象转换成map
     * @return [Map<String, Any>]
     */
    fun toMap(): Map<String, String> {
        return convertToMap(this,true)
    }
}