package cn.xybbz.api.client

import cn.xybbz.api.client.data.ClientLoginInfoReq
import cn.xybbz.api.client.data.LoginSuccessData

abstract class DefaultParentApiClient:DefaultApiClient() {
    /**
     * 创建下载链接
     */
    abstract fun createDownloadUrl(itemId: String): String



    /**
     * 登陆接口
     */
    abstract suspend fun login(clientLoginInfoReq: ClientLoginInfoReq): LoginSuccessData

    abstract suspend fun loginAfter(
        accessToken: String?,
        userId: String? = null,
        subsonicToken: String? = null,
        subsonicSalt: String? = null,
        clientLoginInfoReq: ClientLoginInfoReq
    )

    open suspend fun pingAfter(machineIdentifier:String? = null){}


}