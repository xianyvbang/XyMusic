package cn.xybbz.api.client.plex.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.plex.data.ItemInfoResponse
import cn.xybbz.api.client.plex.data.PlexLibrary
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
        @Query("type") type: Int? = 10,
        @Query("sort") sort: String? = null,
        @Query("title") title: String? = null,
        @Query("includeCollections") includeCollections: Int = 1,
        @Query("includeMeta") includeMeta: Int = 1,
        @Query("includeExternalMedia") includeExternalMedia: Int = 1,
        @Query("X-Plex-Container-Start") start: Int,
        @Query("X-Plex-Container-Size") pageSize: Int,
        @Query("artist.id") artistId: String? = null,
        @Query("album.id") albumId: String? = null,
        //使用逗号分割
        @Query("genre") genreIds: String? = null,
        @Query("album.collection") albumCollection: Int? = null,
        @Query("track.collection") trackCollection: Int? = null,
        @Query("artist.collection") artistCollection: Int? = null,
        @Query("decade", encoded = false) decade: String? = null,
        @Query("album.decade", encoded = false) albumDecade: String? = null,
        @Query("artist.title", encoded = false) artistTitle: String? = null,
        @QueryMap(encoded = false) params: Map<String, String>? = null
    ): PlexResponse<PlexLibraryItemResponse>


    @GET("/library/metadata/{sectionKey}")
    suspend fun getLibraryInfo(
        @Path("sectionKey") sectionKey: String,
        @Query("includeConcerts") includeConcerts: Int = 1,
        @Query("includeExtras") includeExtras: Int = 1,
        @Query("includeOnDeck") includeOnDeck: Int = 1,
        @Query("includePopularLeaves") includePopularLeaves: Int = 1,
        @Query("includePreferences") includePreferences: Int = 1,
        @Query("includeChapters") includeChapters: Int = 1,
        @Query("includeStations") includeStations: Int = 1,
        @Query("includeMarkers") includeMarkers: Int = 1,
        @Query("includeExternalMedia") includeExternalMedia: Int = 1,
        @Query("asyncAugmentMetadata") asyncAugmentMetadata: Int = 1,
        @Query("includeRelated") includeRelated: Int = 1,
        @Query("checkFiles") checkFiles: Int = 1,
        @Query("asyncRefreshAnalysis") asyncRefreshAnalysis: Int = 1,
        @Query("asyncRefreshLocalMediaAgent") asyncRefreshLocalMediaAgent: Int = 1,
    ): PlexResponse<ItemInfoResponse>


    @GET("/library/sections/{sectionKey}/genre")
    suspend fun getGenres(
        @Path("sectionKey") sectionKey: String,
        @Query("type") type: Int? = 10,
        @Query("sort") sort: String? = null,
        @Query("title") title: String? = null,
        @Query("X-Plex-Container-Start") start: Int? = null,
        @Query("X-Plex-Container-Size") pageSize: Int? = null
    ): PlexResponse<PlexLibrary>
}