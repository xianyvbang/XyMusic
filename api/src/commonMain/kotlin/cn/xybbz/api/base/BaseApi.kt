package cn.xybbz.api.base

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.ContentType
import io.ktor.http.HttpMessageBuilder
import io.ktor.http.ParametersBuilder
import io.ktor.http.contentType
import io.ktor.http.parameters
import io.ktor.util.StringValues
import io.ktor.util.appendAll

/**
 * 基础API
 * @author xybbz
 * @date 2025/12/02
 * @constructor 创建[BaseApi]
 */
interface BaseApi {

    fun HttpRequestBuilder.postBlock(block: HttpMessageBuilder.() -> Unit = {}) {
        contentType(ContentType.Application.Json)
        block()
    }

    fun ParametersBuilder.append(name: String, value: String?) {
        value?.let { append(name, it) }
    }

    fun ParametersBuilder.append(name: String, value: Int?) {
        value?.let { append(name, it.toString()) }
    }

    fun ParametersBuilder.appendAll(value: Map<String, String>?) {
        value?.let {
            appendAll(StringValues.build {
                appendAll(it)
            })
        }
    }

    fun ParametersBuilder.append(name: String, value: Boolean?) {
        value?.let {
            append(name, value.toString())
        }
    }

    fun ParametersBuilder.appendAll(name: String, values: List<String>?) {
        values?.let {
            appendAll(name, values)
        }
    }

    fun HttpRequestBuilder.parametersXy(builder: ParametersBuilder.() -> Unit) {
        url { uriBuilder ->
            parameters.appendAll(parameters { builder() })
        }
    }


}