package cn.xybbz.api.client.version.service

import cn.xybbz.api.base.IDownLoadApi
import cn.xybbz.api.client.version.data.ReleasesData
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class GitHubVersionApi(private val httpClient: HttpClient) : IDownLoadApi(httpClient) {

    suspend fun getLatestReleasesInfo(
        owner: String = "xianyvbang",
        repo: String = "XyMusic"
    ): ReleasesData? {
        return httpClient.get("/repos/${owner}/${repo}/releases/latest").body()
    }


}