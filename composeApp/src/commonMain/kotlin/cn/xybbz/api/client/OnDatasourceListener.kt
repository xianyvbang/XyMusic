package cn.xybbz.api.client

import cn.xybbz.localdata.data.connection.ConnectionConfig

/**
 * 数据源监听事件
 */
interface OnDatasourceListener {

    /**
     * 自动登陆后
     */
    suspend fun autoLoginSuccessAfter(connectionConfig: ConnectionConfig){}

    /**
     * 自动登陆前
     */
    suspend fun autoLoginBefore(connectionConfig: ConnectionConfig){}
}