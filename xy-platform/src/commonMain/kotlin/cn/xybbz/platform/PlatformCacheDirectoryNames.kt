package cn.xybbz.platform

/**
 * 跨模块共享的缓存目录名称。
 *
 * 放在 xy-platform commonMain，保证 ContextWrapper、composeApp 各平台代码都能复用同一份常量。
 */
object PlatformCacheDirectoryNames {
    /** 可重建缓存根目录名。 */
    const val CACHE = "cache"

    /** 播放/歌曲缓存目录名。 */
    const val SONG = "song"

    /** 图片缓存目录名。 */
    const val ASSETS = "assets"
}