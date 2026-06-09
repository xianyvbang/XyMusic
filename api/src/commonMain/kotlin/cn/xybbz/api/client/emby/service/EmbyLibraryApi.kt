package cn.xybbz.api.client.emby.service

import cn.xybbz.api.base.BaseApi
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.parameter
import io.ktor.http.parameters

class EmbyLibraryApi(private val httpClient: HttpClient) : BaseApi {

    /**
     * 删除数据
     * @param itemId 数据id
     */
    suspend fun deleteItem(itemId: String) {
        httpClient.delete("/emby/Items/$itemId")
    }


    /**
     * 批量删除数据
     * @param [ids] 数据id
     */
    suspend fun deleteItems(ids: String) {
        httpClient.delete("/emby/Items") {
            parametersXy {
                append("ids", ids)
            }
        }
    }
}