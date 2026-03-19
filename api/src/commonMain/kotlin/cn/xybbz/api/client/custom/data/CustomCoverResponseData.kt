package cn.xybbz.api.client.custom.data

import kotlinx.serialization.Serializable

/**
 * 自定义封面接口响应对象
 * 说明：兼容常见字段命名，统一由 data class 承载解析。
 */
@Serializable
data class CustomCoverResponseData(
    val cover: String? = null,
    val pic: String? = null,
    val url: String? = null,
    val image: String? = null,
    val imageUrl: String? = null
) {
    /**
     * 提取可用的封面地址
     */
    fun pickCoverUrl(): String? {
        return listOf(imageUrl, url, cover, pic, image)
            .firstOrNull { !it.isNullOrBlank() }
            ?.trim()
    }
}
