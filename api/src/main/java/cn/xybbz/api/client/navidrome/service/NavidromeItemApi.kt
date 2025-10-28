package cn.xybbz.api.client.navidrome.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.navidrome.data.AlbumItem
import cn.xybbz.api.client.navidrome.data.SongItem
import cn.xybbz.api.client.subsonic.data.SubsonicResponse
import cn.xybbz.api.client.subsonic.data.SubsonicSearchResponse
import cn.xybbz.api.client.subsonic.data.SubsonicStarred2Response
import cn.xybbz.api.enums.navidrome.OrderType
import cn.xybbz.api.enums.navidrome.SortType
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 音乐,专辑,艺术家相关接口
 */
interface NavidromeItemApi : BaseApi {

    @GET("/api/album")
    suspend fun getAlbumList(
        @Query("_start") start: Int,
        @Query("_end") end: Int,
        @Query("_order") order: OrderType = OrderType.ASC,
        @Query("_sort") sort: SortType = SortType.NAME,
        @Query("name") name: String? = null,
        @Query("missing") missing: Boolean? = null,
        @Query("starred") starred: Boolean? = null,
        @Query("year") year: Int? = null,
        @Query("genre_id") genreId: String? = null,
        @Query("artist_id") artistId: String? = null,
        //是否最近播放
        @Query("recently_played") recentlyPlayed: Boolean? = null,
    ): Response<List<AlbumItem>>

    @GET("/api/album")
    suspend fun getAlbum(@Query("id") id: String): AlbumItem

    @GET("/api/song")
    suspend fun getSong(
        @Query("_start") start: Int,
        @Query("_end") end: Int,
        @Query("_order") order: OrderType = OrderType.ASC,
        @Query("_sort") sort: SortType = SortType.TITLE,
        @Query("title") title: String? = null,
        @Query("missing") missing: Boolean? = null,
        @Query("starred") starred: Boolean? = null,
        @Query("genre_id") genreIds: List<String>? = null,
        @Query("album_id") albumId: String? = null,
        @Query("artist_id") artistIds: List<String>? = null,
        @Query("year") year: Int? = null
    ): Response<List<SongItem>>

    @GET("/rest/search3")
    suspend fun search3(
        @Query("query") query: String,
        @Query("artistCount") artistCount: Int = 20,
        @Query("artistOffset") artistOffset: Int = 0,
        @Query("albumCount") albumCount: Int = 20,
        @Query("albumOffset") albumOffset: Int = 0,
        @Query("songCount") songCount: Int = 20,
        @Query("songOffset") songOffset: Int = 0,
        @Query("musicFolderId") musicFolderId: String? = null
    ): SubsonicResponse<SubsonicSearchResponse>

    @GET("/rest/getStarred2")
    suspend fun getStarred2(
        @Query("musicFolderId") musicFolderId: String?
    ): SubsonicResponse<SubsonicStarred2Response>
}