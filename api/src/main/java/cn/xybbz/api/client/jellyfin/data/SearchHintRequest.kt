package cn.xybbz.api.client.jellyfin.data

import cn.xybbz.api.client.data.Request
import cn.xybbz.api.enums.jellyfin.BaseItemKind
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SearchHintRequest(
    val startIndex: Int? = null,
    val limit: Int? = null,
    val searchTerm: String,
    val includeItemTypes: List<BaseItemKind>? = emptyList(),
):Request()
