package cn.xybbz.music

import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import cn.xybbz.api.TokenServer.baseUrl

class XyDefaultDataSourceFactory(private val delegate: DataSource.Factory) : DataSource.Factory {
    @OptIn(UnstableApi::class)
    override fun createDataSource(): DataSource {
        val upstream = delegate.createDataSource()

        return object : DataSource by upstream {
            override fun open(dataSpec: DataSpec): Long {
                val originalUri = dataSpec.uri

                val newUri = if (originalUri.scheme == null && !isLocalUri(originalUri.toString())) {
                    (baseUrl + originalUri.toString()).toUri()
                } else {
                    originalUri
                }

                val newDataSpec = dataSpec.withUri(newUri)
                return upstream.open(newDataSpec)
            }

            override fun getResponseHeaders(): Map<String, List<String>> {
                return upstream.responseHeaders
            }
        }
    }

    //todo 这里要直接判断downloadConfig里的设置
    private fun isLocalUri(uri: String): Boolean {
        val normalizedUri = uri.trim()
        return normalizedUri.startsWith("/storage/") ||
                normalizedUri.startsWith("/sdcard/") ||
                normalizedUri.startsWith("/mnt/") ||
                normalizedUri.startsWith("/data/")
    }
}
