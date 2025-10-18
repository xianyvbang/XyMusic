package cn.xybbz.api.client.jellyfin.data

import cn.xybbz.api.client.data.Request
import cn.xybbz.api.enums.jellyfin.BaseItemKind
import cn.xybbz.api.enums.jellyfin.ImageType
import cn.xybbz.api.enums.jellyfin.ItemFields
import cn.xybbz.api.enums.jellyfin.ItemFilter
import cn.xybbz.api.enums.jellyfin.ItemSortBy
import cn.xybbz.api.enums.jellyfin.MediaType
import cn.xybbz.api.enums.jellyfin.SortOrder
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ItemRequest(
    val limit: Int? = null,
    val artistIds: List<String>? = emptyList(),
    val startIndex: Int? = null,
    val sortBy: List<ItemSortBy>? = emptyList(),
    val sortOrder: List<SortOrder>? = emptyList(),
    val includeItemTypes: List<BaseItemKind>? = emptyList(),
    val excludeItemTypes: List<BaseItemKind>? = emptyList(),
    val recursive: Boolean? = null,
    val isFavorite: Boolean? = null,
    val filters: List<ItemFilter>? = emptyList(),
    val fields: List<ItemFields>? = emptyList(),
    val imageTypeLimit: Int? = null,
    val enableImageTypes: List<ImageType>? = emptyList(),
    val searchTerm: String? = null,
    val parentId: String? = null,
    val ids: List<String>? = emptyList(),
    val years: List<Int>? = emptyList(),
    val mediaTypes: Collection<MediaType>? = emptyList(),
    val nameStartsWith: String? = null,
    val nameLessThan: String? = null,
    val genreIds:List<String>? = null,
    val albumIds:List<String>? = null,
    val userId:String? = null,
    val enableTotalRecordCount: Boolean = true,
    /**
     * Optional filter by Path.
     */
    @param:Json(name = "Path")
    val path: String? = null,
) : Request()
