package cn.xybbz.api.client.jellyfin.service

import cn.xybbz.api.base.BaseApi
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.parameter

class LibraryApi(private val httpClient: HttpClient) : BaseApi {

    /**
     * 删除数据
     * @param [itemId] 数据id
     */
    suspend fun deleteItem(itemId: String){
        httpClient.delete("/Items/$itemId")
    }

    /**
     * 批量删除项目
     * @param [ids] 数据id
     */
    suspend fun deleteItems(ids: String){
        httpClient.delete("/Items") {
            parameter("ids", ids)
        }
    }
}