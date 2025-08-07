package cn.xybbz.common.utils

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import cn.xybbz.common.utils.DateUtil.toDateStr
import cn.xybbz.entity.data.file.backup.ExportPlaylistData
import cn.xybbz.localdata.config.DatabaseClient
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.OutputStream

/**
 * 歌单备份文件工具类
 */
object PlaylistFileUtils {

    private val outputStreamMap = mutableMapOf<String, OutputStream>()

    /**
     * 创建json数据,导出歌单
     * @param [applicationContext] 应用程序上下文
     * @param [db] DB
     * @param [key] 钥匙
     * @param [playlistId] 播放列表ID
     */
    suspend fun createJsonStr(
        applicationContext: Context,
        db: DatabaseClient,
        key: String,
        playlistId: String? = null
    ) {
        var playlist =
            db.albumDao.selectPlaylist()
        var playlistMusic = db.musicDao.selectPlaylistMusic()
        if (playlistId != null) {
            val playlistTmp = db.albumDao.selectById(playlistId)
            if (playlistTmp != null) {
                playlist = listOf(playlistTmp)
            }
            playlistMusic = db.musicDao.selectPlaylistMusicById(playlistId)
        }

        val exportPlaylistData = ExportPlaylistData(
            playlist = playlist,
            playlistMusic = playlistMusic,
            musicList = db.musicDao.selectPlaylistMusicList()
        )

        val fileName = "咸鱼音乐歌单导出-${
            System.currentTimeMillis().toDateStr(
                "yyyyMMddHHmmss"
            )
        }${playlistId}.json"

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()


        val adapter = moshi.adapter(ExportPlaylistData::class.java)
        val json = adapter.toJson(exportPlaylistData)

        //将文件写入Download中
        val resolver = applicationContext.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "application/json")
            put(
                MediaStore.Downloads.RELATIVE_PATH,
                Environment.DIRECTORY_DOWNLOADS + "/${applicationContext.packageName}/backup/"
            )
        }

        val selection = "${MediaStore.Downloads.RELATIVE_PATH} = ?"
        val selectionArgs =
            arrayOf("${Environment.DIRECTORY_DOWNLOADS}/${applicationContext.packageName}/backup/")

        val queryList = resolver.query(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Downloads._ID, MediaStore.Downloads.DATE_ADDED),
            selection,
            selectionArgs,
            "${MediaStore.Downloads.DATE_ADDED} ASC"
        )

        if (queryList != null && queryList.count >= 3) {
            if (queryList.moveToFirst()) {
                val idIndex = queryList.getColumnIndex(MediaStore.Downloads._ID)
                val id = queryList.getString(idIndex)
                resolver.delete(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    "${MediaStore.Downloads._ID} = $id",
                    null
                )
            }
            queryList.close()
        }
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let {
            resolver.openOutputStream(it)?.use { outputStream ->
                outputStreamMap.put(key, outputStream)
                outputStream.write(json.toByteArray())
                outputStreamMap.remove(key)
            }
        }
    }

    /**
     * 关闭流
     */
    fun closeOutputStream(key: String) {
        if (outputStreamMap.containsKey(key)) {
            outputStreamMap[key]?.close()
        }
    }
}