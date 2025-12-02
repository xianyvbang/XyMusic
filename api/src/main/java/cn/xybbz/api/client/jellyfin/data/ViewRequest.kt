package cn.xybbz.api.client.jellyfin.data

import cn.xybbz.api.client.data.Request
import cn.xybbz.api.enums.jellyfin.CollectionType
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * 媒体库请求实体类
 * @author Administrator
 * @date 2025/12/02
 * @constructor 创建[ViewRequest]
 */
@JsonClass(generateAdapter = true)
data class ViewRequest(
    /**
     * 是否包括外部视图，如频道或直播电视。
     */
    @param:Json(name = "IncludeExternalContent")
    val includeExternalContent: Boolean? = null,
    /**
     * 查询视图类型
     */
    @param:Json(name = "PresetViews")
    val presetViews: List<CollectionType>? = emptyList(),
    /**
     * 是否显示隐藏内容
     */
    @param:Json(name = "IncludeHidden")
    val includeHidden: Boolean? = null
) : Request()
