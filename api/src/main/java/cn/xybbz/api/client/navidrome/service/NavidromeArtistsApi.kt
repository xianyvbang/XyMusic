package cn.xybbz.api.client.navidrome.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.navidrome.data.ArtistItem
import cn.xybbz.api.enums.navidrome.OrderType
import cn.xybbz.api.enums.navidrome.SortType
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface NavidromeArtistsApi : BaseApi{
    @GET("/api/artist")
    suspend fun getArtists(
        @Query("_start") start: Int,
        @Query("_end") end: Int,
        @Query("_order") order: OrderType = OrderType.ASC,
        @Query("_sort") sort: SortType = SortType.NAME,
        @Query("name") name: String? = null,
        @Query("missing") missing: Boolean? = null,
        @Query("starred") starred: Boolean? = null
    ): Response<List<ArtistItem>?>

    @GET("/api/artist/{artistId}")
    suspend fun getArtist(@Path("artistId") artistId: String): ArtistItem?
}