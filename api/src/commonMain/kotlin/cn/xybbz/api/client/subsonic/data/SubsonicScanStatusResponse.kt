package cn.xybbz.api.client.subsonic.data

import cn.xybbz.api.enums.subsonic.Status
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubsonicScanStatusResponse(
    override val type: String? = null,
    /**
     * 版本号
     */
    override val version: String,
    /**
     * 状态
     */
    override val status: Status,
    /**
     * 热门歌曲
     */
    @SerialName(value = "scanStatus")
    val scanStatus: ScanStatus? = null,
    /**
     * 报错信息
     */
    override val error: SubsonicError? = null
) : SubsonicParentResponse {
}