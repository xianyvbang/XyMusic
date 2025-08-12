package cn.xybbz.api.client.plex.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.plex.data.PlexLibraryItemResponse
import cn.xybbz.api.client.plex.data.PlexResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface PlexItemApi : BaseApi {

    @GET("/library/sections/{sectionKey}/{selectType}")
    suspend fun getSongs(
        @Path("sectionKey") sectionKey: String,
        @Path("selectType") selectType: String,
        @Query("type") type: Int = 10,
        @Query("sort") sort: String,
        @Query("title") title: String? = null,
        @Query("includeCollections") includeCollections: Int = 1,
        @Query("includeMeta") includeMeta: Int = 1,
        @Query("includeExternalMedia") includeExternalMedia: Int = 1,
        @Query("X-Plex-Container-Start") start: Int,
        @Query("X-Plex-Container-Size") pageSize: Int,
        @QueryMap params: Map<String, String>? = null
    ): PlexResponse<PlexLibraryItemResponse>

}