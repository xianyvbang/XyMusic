package cn.xybbz.api.client.jellyfin.data

import cn.xybbz.api.client.data.AllResponse

fun <T> Response<T>.toAllResponse(startIndex: Int): AllResponse<T> {
    return AllResponse<T>(
        items = this.items,
        totalRecordCount = this.totalRecordCount,
        startIndex = this.startIndex ?: startIndex
    )
}