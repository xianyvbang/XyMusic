package cn.xybbz.api.utils

import cn.xybbz.api.constants.ApiConstants
import io.ktor.http.Headers
import io.ktor.http.HeadersBuilder

/**
 * 追加项目内通用的鉴权请求头。
 * 这里统一处理 token 请求头，以及自定义图片请求场景下需要透传的 Authorization。
 */
fun HeadersBuilder.appendCustomRequestHeaders(
    sourceHeaders: Headers,
    token: String,
    tokenHeaderName: String
) {
    if (token.isNotBlank()) {
        append(tokenHeaderName, token)
    }

    if (sourceHeaders.contains(ApiConstants.CUSTOM_IMAGE_HEADER_NAME)) {
        append(
            ApiConstants.AUTHORIZATION,
            sourceHeaders[ApiConstants.AUTHORIZATION] ?: ""
        )
    }
}
