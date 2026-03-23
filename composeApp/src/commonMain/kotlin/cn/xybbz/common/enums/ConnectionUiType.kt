package cn.xybbz.common.enums

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * connection页面的UI类型
 */
@Serializable
enum class ConnectionUiType {
    /**
     * 第一次打开
     */
    @SerialName("first_open")
    FIRST_OPEN,
    /**
     * 添加链接
     */
    @SerialName("add_connection")
    ADD_CONNECTION
}