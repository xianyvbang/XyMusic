package cn.xybbz.api.client

/**
 * Ktor 客户端的全局运行配置。
 */
object KtorClient {
    /**
     * 是否输出 HTTP 调试日志，必须在创建客户端前完成设置。
     */
    var debug: Boolean = false
}
