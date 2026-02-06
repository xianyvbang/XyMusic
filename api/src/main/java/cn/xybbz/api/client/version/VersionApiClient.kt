/*
 *   XyMusic
 *   Copyright (C) 2023 xianyvbang
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package cn.xybbz.api.client.version

import cn.xybbz.api.client.ApiConfig
import cn.xybbz.api.client.version.service.GitHubVersionApi
import cn.xybbz.api.converter.kotlinxJsonConverter
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

class VersionApiClient : ApiConfig {

    lateinit var apiOkHttpClient: OkHttpClient

    private lateinit var retrofit: Retrofit

    private lateinit var gitHubVersionApi: GitHubVersionApi

    init {
        setRetrofitData("", false)
    }

    /**
     * 通过Retrofit创建Api接口
     */
    override fun <T> createApiObj(clazz: Class<T>): T {
        return instance().create(clazz)
    }

    override fun setRetrofitData(baseUrl: String, ifTmp: Boolean) {
        retrofit = Retrofit.Builder()
            .baseUrl("https://api.github.com/").client(getOkHttpClient())
            .addConverterFactory(
                kotlinxJsonConverter()
            ).build()
    }

    override fun instance(): Retrofit {
        return retrofit
    }

    /**
     * 清空数据
     */
    override fun release() {

    }

    /**
     * 获得okhttp客户端
     */
    private fun getOkHttpClient(): OkHttpClient {
        apiOkHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                println("➡️ Request URL: ${request.url}")
                val response = chain.proceed(request)
                println("⬅️ Response code: ${response.code}")
                println("⬅️ Response URL: ${response.request.url}")
                response
            }
            .connectTimeout(1000000, TimeUnit.MILLISECONDS)
            .readTimeout(1000000, TimeUnit.MILLISECONDS)
            .writeTimeout(1000000, TimeUnit.MILLISECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            // 可以添加其他配置，比如连接超时、读写超时等
            .build()
        return apiOkHttpClient
    }

    /**
     * 获取版本号信息的Api
     */
    override fun downloadApi(restart: Boolean): GitHubVersionApi {
        if (!this::gitHubVersionApi.isInitialized || restart) {
            gitHubVersionApi = instance().create(GitHubVersionApi::class.java)
        }
        return gitHubVersionApi
    }


}