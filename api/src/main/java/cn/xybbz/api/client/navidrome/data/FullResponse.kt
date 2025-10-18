package cn.xybbz.api.client.navidrome.data

import cn.xybbz.api.constants.ApiConstants
import retrofit2.Response

data class FullResponse<T>(
    val data: T?,
    val totalCount: Int?
)

fun <T> Response<T>.toFullResponse(): FullResponse<T> {
    val totalCountHeader = this.headers()[ApiConstants.NAVIDROME_TOTAL_COUNT]
    val totalCount = totalCountHeader?.toIntOrNull()
    return FullResponse(
        data = this.body(),
        totalCount = totalCount
    )
}

suspend fun <T> getWithTotalCount(
    requestCall: suspend () -> Response<T>
): FullResponse<T> {
    val response = requestCall()
    return response.toFullResponse()
}
