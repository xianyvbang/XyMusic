package cn.xybbz.api.client

import retrofit2.Retrofit

/**
 * 接口类基类
 */
interface ApiConfig {

    /**
     * 通过Retrofit创建Api接口
     */
    fun <T> createApiObj(clazz: Class<T>): T

    /**
     * 初始化Retrofit
     */
    fun setRetrofitData(baseUrl: String, ifTmp: Boolean)

    /**
     * 获得Retrofit
     */
    fun instance(): Retrofit

    /**
     * 清空数据
     */
    fun release()
}