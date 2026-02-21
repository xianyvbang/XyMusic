package cn.xybbz.api.client

import cn.xybbz.api.base.IDownLoadApi
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
     * 下载相关接口
     */
    fun downloadApi(restart: Boolean = false): IDownLoadApi


    /**
     * 获得前缀地址
     */
    fun getBaseUrl(): String

    /**
     * 获得Retrofit
     */
    fun instance(): Retrofit

    /**
     * 清空数据
     */
    fun release()
}