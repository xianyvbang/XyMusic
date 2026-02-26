package cn.xybbz.common.music

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

                val newUri = if (originalUri.scheme == null) {
                    (baseUrl + originalUri.toString()).toUri()
                } else {
                    originalUri
                }

                val newDataSpec = dataSpec.withUri(newUri)
                return upstream.open(newDataSpec)
            }
        }
    }
}