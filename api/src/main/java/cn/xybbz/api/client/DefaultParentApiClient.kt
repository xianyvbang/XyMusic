package cn.xybbz.api.client

abstract class DefaultParentApiClient:DefaultApiClient() {
    /**
     * 创建下载链接
     */
    abstract fun createDownloadUrl(itemId: String): String

}