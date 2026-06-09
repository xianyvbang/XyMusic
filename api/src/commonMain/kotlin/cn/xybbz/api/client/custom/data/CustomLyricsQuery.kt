package cn.xybbz.api.client.custom.data

/**
 * 自定义歌词接口请求参数
 * 说明：由上层业务模块组装，API 模块只做网络请求和响应解析。
 */
data class CustomLyricsQuery(
    val singleApi: String,
    val authKey: String,
    val title: String,
    val artist: String = "",
    val album: String = "",
    val path: String = ""
)
