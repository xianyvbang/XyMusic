package cn.xybbz.api.client.plex.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.jellyfin.data.FavoriteResponse
import cn.xybbz.api.client.plex.data.ItemInfoResponse
import cn.xybbz.api.client.plex.data.PlexLibraryItemResponse
import cn.xybbz.api.client.plex.data.PlexResponse
import cn.xybbz.api.enums.plex.PlexSortOrder
import cn.xybbz.api.enums.plex.PlexSortType
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.http.parameters

class PlexUserLibraryApi(private val httpClient: HttpClient) : BaseApi {
    /**
     * 增加收藏合集
     * @return [FavoriteResponse]
     */
    suspend fun addCollection(
        title: String,
        type: Int? = 10,
        smart: String? = "0",
        sectionId: String? = null,
        uri: String
    ): PlexResponse<ItemInfoResponse> {
        return httpClient.post("/library/collections") {
            parameters {
                append(name = "title", value = title)
                append(name = "type", value = type)
                append(name = "smart", value = smart)
                append(name = "sectionId", value = sectionId)
                append(name = "uri", value = uri)
            }
        }.body()
    }

    /**
     * 获得收藏合集
     */
    suspend fun getCollection(
        sectionKey: String,
        subtype: Int? = 10,
        smart: String? = "0",
        sectionId: String? = null,
        sort: String = "${PlexSortType.UPDATED_AT}:${PlexSortOrder.DESCENDING}",
        limit: Int = 1,
        title: String
    ): PlexResponse<ItemInfoResponse> {
        return httpClient.get("/library/sections/${sectionKey}/collections") {
            parameters {
                append(name = "subtype", value = subtype)
                append(name = "smart", value = smart)
                append(name = "sectionId", value = sectionId)
                append(name = "sort", value = sort)
                append(name = "limit", value = limit)
                append(name = "title", value = title)
            }
        }.body()
    }


    /**
     * 标记最喜欢项目
     * @return [FavoriteResponse]
     */
    suspend fun markFavoriteItem(
        collectionId: String,
        type: Int? = 10,
        uri: String
    ): PlexResponse<ItemInfoResponse> {
        return httpClient.put("/library/collections/${collectionId}/items") {
            parameters {
                append(name = "type", value = type)
                append(name = "uri", value = uri)
            }
        }.body()
    }

    /**
     * 取消标记喜欢物品
     * @return [FavoriteResponse]
     */
    suspend fun unmarkFavoriteItem(
        collectionId: String,
        musicId: String,
        excludeAllLeaves: Int? = 1
    ): PlexResponse<ItemInfoResponse> {
        return httpClient.delete("/library/collections/${collectionId}/children/${musicId}") {
            parameters {
                append(name = "excludeAllLeaves", value = excludeAllLeaves)
            }
        }.body()
    }


    suspend fun getFavoriteSongs(
        sectionKey: String,
        excludeAllLeaves: Int = 1,
        includeCollections: Int = 1,
        includeMeta: Int = 1,
        includeExternalMedia: Int = 1,
        start: Int,
        pageSize: Int
    ): PlexResponse<PlexLibraryItemResponse> {
        return httpClient.get("/library/collections/${sectionKey}/children") {
            parameters {
                append(name = "excludeAllLeaves", value = excludeAllLeaves)
                append(name = "includeCollections", value = includeCollections)
                append(name = "includeMeta", value = includeMeta)
                append(name = "includeExternalMedia", value = includeExternalMedia)
                append(name = "X-Plex-Container-Start", value = start)
                append(name = "X-Plex-Container-Size", value = pageSize)
            }
        }.body()
    }

    suspend fun similarItem(
        ids: String,
        count: Int
    ): PlexResponse<PlexLibraryItemResponse> {
        return httpClient.get("/library/metadata/${ids}/similarsimilar") {
            parameters {
                append(name = "count", value = count)
            }
        }.body()
    }

}