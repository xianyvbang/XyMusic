package cn.xybbz.api.client.navidrome.service

import cn.xybbz.api.base.BaseApi
import cn.xybbz.api.client.navidrome.data.FullResponse
import cn.xybbz.api.client.navidrome.data.Genre
import cn.xybbz.api.client.navidrome.data.toFullResponse
import cn.xybbz.api.enums.navidrome.OrderType
import cn.xybbz.api.enums.navidrome.SortType
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.http.parameters

class NavidromeGenreApi(private val httpClient: HttpClient) : BaseApi {

    suspend fun getGenres(
        start: Int,
        end: Int,
        order: OrderType = OrderType.ASC,
        sort: SortType = SortType.NAME,
        name: String? = null,
        libraryIds: List<String>? = null
    ): FullResponse<List<Genre>> {
        val httpResponse = httpClient.get("/api/genre") {
            parametersXy {
                append("_start", start.toString())
                append("_end", end.toString())
                append("_order", order.toString())
                append("_sort", sort.toString())
                append("name", name)
                appendAll("library_id", libraryIds)
            }
        }
        return httpResponse.toFullResponse()
    }
}