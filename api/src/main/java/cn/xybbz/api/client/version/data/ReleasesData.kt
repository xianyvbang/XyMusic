package cn.xybbz.api.client.version.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class ReleasesData(
    @param:Json(name = "tag_name")
    val tagName: String,
    @param:Json(name = "name")
    val name: String,
    @param:Json(name = "body")
    val body: String,
    @param:Json(name = "prerelease")
    val prerelease: Boolean,
    @param:Json(name = "published_at")
    val publishedAt: String,
    @param:Json(name = "assets")
    val assets: List<AssetItem>
)