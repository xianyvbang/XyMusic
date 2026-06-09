package cn.xybbz.api.client.navidrome.data

import cn.xybbz.api.constants.ApiConstants
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse

data class FullResponse<T>(
    val data: T?,
    val totalCount: Int?
)

suspend inline fun <reified T> HttpResponse.toFullResponse(): FullResponse<T> {
    val totalCountHeader = this.headers[ApiConstants.NAVIDROME_TOTAL_COUNT]
    val totalCount = totalCountHeader?.toIntOrNull()
    return FullResponse(
        data = this.body<T>(),
        totalCount = totalCount
    )
}
