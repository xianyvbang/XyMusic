package cn.xybbz.music

import cn.xybbz.config.music.DownloadCacheCommonController
import cn.xybbz.localdata.data.music.XyPlayMusic

class JvmDownloadCacheController : DownloadCacheCommonController() {

    override fun clearCache() {
    }

    override fun getCacheSize() {
        updateCacheSizeFlow(0L)
    }

    override fun cacheMedia(
        music: XyPlayMusic,
        ifStatic: Boolean
    ) {
    }

    override fun cancelAllCache() {
    }

    override fun getMediaItem(cacheKey: String): Any? {
        return null
    }
}
