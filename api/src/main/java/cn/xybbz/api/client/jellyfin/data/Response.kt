package cn.xybbz.api.client.jellyfin.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class Response<T>(
    @param:Json(name = "Items")
    val items: List<T>,
    /**
     * The total number of records available.
     */
    @param:Json(name = "TotalRecordCount")
    val totalRecordCount: Int,
    /**
     * The index of the first record in Items.
     */
    @param:Json(name = "StartIndex")
    val startIndex: Int? = null,
)