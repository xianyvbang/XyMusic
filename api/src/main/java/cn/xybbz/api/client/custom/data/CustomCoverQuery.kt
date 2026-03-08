package cn.xybbz.api.client.custom.data

/**
 * 自定义封面接口请求参数
 * 说明：由上层业务模块组装，API 模块只做网络请求和响应解析。
 */
data class CustomCoverQuery(
    val coverApi: String,
    val authKey: String,
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null,
    val path: String? = null
)
