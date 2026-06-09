package cn.xybbz.common.exception

/**
 * 取消下载异常
 */
class CancelDownloadException(
    override val message: String?,
    override val cause: Throwable? = null
) : RuntimeException(message, cause) {
}