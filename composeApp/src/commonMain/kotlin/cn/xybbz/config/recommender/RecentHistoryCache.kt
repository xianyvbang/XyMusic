package cn.xybbz.config.recommender

import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class RecentHistoryCache(
    private val db: DatabaseClient,
    private val maxSize: Int = 100
) {

    suspend fun addAll(songs: List<XyMusic>, connectionId: Long) = withContext(Dispatchers.IO) {
        val mediaLibraryId = db.connectionConfigDao
            .selectConnectionConfig()
            ?.libraryIds
            ?.joinToString(",")
        db.musicDao.saveBatch(
            songs,
            MusicDataTypeEnum.RECOMMEND,
            connectionId,
            mediaLibraryId = mediaLibraryId
        )
        val songIds = db.recentHistoryDao.maxSizeSongIds(maxSize)
        db.recentHistoryDao.trimToMaxSize(maxSize)
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
}
