package cn.xybbz.api.client.navidrome.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.navidrome.data.Genre
import cn.xybbz.api.enums.navidrome.OrderType
import cn.xybbz.api.enums.navidrome.SortType
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NavidromeGenreApi : BaseApi {

    @GET("/api/genre")
    suspend fun getGenres(
        @Query("_start") start: Int,
        @Query("_end") end: Int,
        @Query("_order") order: OrderType = OrderType.ASC,
        @Query("_sort") sort: SortType = SortType.NAME,
        @Query("name") name: String? = null,
    ): Response<List<Genre>>
}