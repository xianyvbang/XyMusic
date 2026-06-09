package cn.xybbz.api.client

/**
 * 接口类基类
 */
interface ApiFactory {

    /**
     * 初始化HttpClient
     */
    fun createHttpClient(baseUrl: String, ifTmp: Boolean)

    /**
     * 清空数据
     */
    fun release()
}