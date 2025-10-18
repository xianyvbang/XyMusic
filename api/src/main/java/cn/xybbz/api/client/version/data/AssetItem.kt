package cn.xybbz.api.client.version.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AssetItem(
    val id: String,
    val name:String,
    val size: Long,
    val digest:String,
    //私有仓库下载地址
    val url:String,
    //公开仓库下载地址
    @param:Json(name = "browser_download_url")
    val browserDownloadUrl:String,
    @param:Json(name = "content_type")
    val contentType:String,
    @param:Json(name = "created_at")
    val createdAt:String,
    @param:Json(name = "updated_at")
    val updatedAt:String
)
