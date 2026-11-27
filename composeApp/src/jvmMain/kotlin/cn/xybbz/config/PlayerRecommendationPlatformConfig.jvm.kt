package cn.xybbz.config

/**
 * JVM 播放页不展示相似歌曲和歌手热门歌曲，切歌后不再请求这两类推荐数据。
 */
actual val ifLoadPlayerRecommendation: Boolean = false
