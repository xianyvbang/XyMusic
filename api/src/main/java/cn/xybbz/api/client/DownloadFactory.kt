package cn.xybbz.api.client

import cn.xybbz.api.base.IDownLoadApi

/**
 * 下载基类
 */
interface DownloadFactory {
    /**
     * 下载相关接口
     */
    fun downloadApi(restart: Boolean = false): IDownLoadApi


    /**
     * 获得前缀地址
     */
    fun getBaseUrl(): String
}