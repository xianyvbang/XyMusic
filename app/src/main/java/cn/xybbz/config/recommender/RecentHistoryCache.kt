package cn.xybbz.config.recommender

import cn.xybbz.localdata.dao.recommend.XyRecentHistoryDao
import cn.xybbz.localdata.data.recommend.XyRecentHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RecentHistoryCache(
    private val dao: XyRecentHistoryDao,
    private val maxSize: Int = 300,
    private val expireDays: Int = 7
) {

    private val DAY_MS = 24 * 60 * 60 * 1000L

    suspend fun addAll(songIds: List<String>) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val entities = songIds.map { XyRecentHistory(it, now) }
        dao.insertAll(entities)
        dao.trimToMaxSize(maxSize)
    }

    suspend fun contains(songId: String): Boolean = withContext(Dispatchers.IO) {
        val all = dao.getAllIds()
        songId in all
    }

    suspend fun getRecentIds(): List<String> = withContext(Dispatchers.IO) {
        dao.getAllIds()
    }

    suspend fun cleanupExpired() = withContext(Dispatchers.IO) {
        val expireBefore = System.currentTimeMillis() - expireDays * DAY_MS
        dao.deleteExpired(expireBefore)
    }
}
