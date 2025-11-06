package cn.xybbz.config.recommender

import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.data.recommend.XyRecentHistory
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RecentHistoryCache(
    private val db: DatabaseClient,
    private val maxSize: Int = 300,
    private val expireDays: Int = 7
) {

    private val DAY_MS = 24 * 60 * 60 * 1000L

    suspend fun addAll(songs: List<XyMusic>, connectionId: Long) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val entities = songs.map { XyRecentHistory(it.itemId, connectionId, now) }
        db.recentHistoryDao.insertAll(entities)
        val songIds = db.recentHistoryDao.maxSizeSongIds(maxSize)
        db.recentHistoryDao.trimToMaxSize(maxSize)
        db.musicDao.saveBatch(
            songs,
            MusicDataTypeEnum.RECOMMEND,
            connectionId
        )
        if (songIds.isNotEmpty())
            db.musicDao.removeByType(MusicDataTypeEnum.RECOMMEND, itemIds = songIds)
    }

    suspend fun contains(items: List<XyMusic>): List<XyMusic> = withContext(Dispatchers.IO) {
        val all = db.recentHistoryDao.getAllIds()
        items.filterNot { it.itemId in all }
    }

    suspend fun contains(items: List<XyMusic>, predicate: (XyMusic) -> Boolean): List<XyMusic> =
        withContext(Dispatchers.IO) {
            val all = db.recentHistoryDao.getAllIds()
            items.filter {
                it.itemId !in all && predicate.invoke(it)
            }
        }

    suspend fun getRecentIds(): List<String> = withContext(Dispatchers.IO) {
        db.recentHistoryDao.getAllIds()
    }

    suspend fun cleanupExpired() = withContext(Dispatchers.IO) {
        val expireBefore = System.currentTimeMillis() - expireDays * DAY_MS
        db.recentHistoryDao.deleteExpired(expireBefore)
    }
}
