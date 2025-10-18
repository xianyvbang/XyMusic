package cn.xybbz.api.client.jellyfin.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


/**
 * 收藏和取消收藏接口返回实体类
 * @author xybbz
 * @date 2025/02/11
 * @constructor 创建[FavoriteResponse]
 * @param [itemId] 项目ID
 * @param [isFavorite] 是否收藏
 */
@JsonClass(generateAdapter = true)
data class FavoriteResponse(
    @param: Json(name = "ItemId")
    val itemId: String? = null,
    @param: Json(name = "IsFavorite")
    val isFavorite: Boolean,
)
