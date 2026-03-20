package cn.xybbz.api.client.plex.service

import cn.xybbz.api.base.BaseApi
import io.ktor.client.HttpClient
import io.ktor.client.request.delete

class PlexLibraryApi(private val httpClient: HttpClient) : BaseApi {

    suspend fun deleteItem(itemId: String) {
        httpClient.delete("/library/metadata/${itemId}") {}
    }
}