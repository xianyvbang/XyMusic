package cn.xybbz.api.client.custom.data

import cn.xybbz.api.constants.ApiConstants

/**
 * 自定义歌词请求参数项
 */
data class CustomLyricsRequestParam(
    val fieldName: String,
    val fieldValue: String
)

/**
 * 自定义歌词请求头参数项
 */
data class CustomLyricsRequestHeader(
    val fieldName: String,
    val fieldValue: String
)

data class CustomLyricsRequestData(
    val apiUrl: String,
    val queryParams: List<CustomLyricsRequestParam>,
    val headers: List<CustomLyricsRequestHeader>
) {
    companion object {
        private const val QUERY_TITLE = "title"
        private const val QUERY_ARTIST = "artist"
        private const val QUERY_ALBUM = "album"
        private const val QUERY_PATH = "path"

        /**
         * 由歌词查询对象构造 HTTP 请求对象
         */
        fun from(apiUrl: String, query: CustomLyricsQuery): CustomLyricsRequestData {
            return createRequestData(
                apiUrl = apiUrl,
                authKey = query.authKey,
                title = query.title,
                artist = query.artist,
                album = query.album,
                path = query.path
            )
        }

        /**
         * 由封面查询对象构造 HTTP 请求对象
         */
        fun from(apiUrl: String, query: CustomCoverQuery): CustomLyricsRequestData {
            return createRequestData(
                apiUrl = apiUrl,
                authKey = query.authKey,
                title = query.title,
                artist = query.artist,
                album = query.album,
                path = query.path
            )
        }

        /**
         * 统一构造请求对象
         */
        private fun createRequestData(
            apiUrl: String,
            authKey: String,
            title: String?,
            artist: String?,
            album: String?,
            path: String?
        ): CustomLyricsRequestData {
            val queryParams = mutableListOf<CustomLyricsRequestParam>()
            title?.trim()?.takeIf { it.isNotBlank() }?.let {
                queryParams.add(CustomLyricsRequestParam(fieldName = QUERY_TITLE, fieldValue = it))
            }


            artist?.trim()?.takeIf { it.isNotBlank() }?.let {
                queryParams.add(CustomLyricsRequestParam(fieldName = QUERY_ARTIST, fieldValue = it))
            }
            album?.trim()?.takeIf { it.isNotBlank() }?.let {
                queryParams.add(CustomLyricsRequestParam(fieldName = QUERY_ALBUM, fieldValue = it))
            }
            path?.trim()?.takeIf { it.isNotBlank() }?.let {
                queryParams.add(CustomLyricsRequestParam(fieldName = QUERY_PATH, fieldValue = it))
            }

            val headers = mutableListOf<CustomLyricsRequestHeader>()
            authKey.trim().takeIf { it.isNotBlank() }?.let { key ->
                queryParams.add(
                    CustomLyricsRequestParam(
                        fieldName = ApiConstants.CUSTOM_IMAGE_HEADER_NAME,
                        fieldValue = key
                    )
                )
                headers.add(
                    CustomLyricsRequestHeader(
                        fieldName = ApiConstants.AUTHORIZATION,
                        fieldValue = key
                    )
                )
            }

            return CustomLyricsRequestData(
                apiUrl = apiUrl.trim(),
                queryParams = queryParams,
                headers = headers
            )
        }
    }
}
