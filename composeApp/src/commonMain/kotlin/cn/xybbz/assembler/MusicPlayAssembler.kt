package cn.xybbz.assembler

import cn.xybbz.download.database.DownloadDatabaseClient
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.data.music.XyMusicExtend
import cn.xybbz.localdata.data.music.XyPlayMusic

object MusicPlayAssembler {

    suspend fun toPlayMusicList(
        musicList: List<XyMusic>?,
        downloadDb: DownloadDatabaseClient,
        mediaLibraryId: String? = null
    ): List<XyPlayMusic>? {
        val safeMusicList = musicList ?: return null
        val downloadMap = buildDownloadMap(
            itemIds = safeMusicList.map { it.itemId },
            downloadDb = downloadDb,
            mediaLibraryId = mediaLibraryId
        )

        return safeMusicList.map { music ->
            music.toPlayMusic().copy(
                filePath = downloadMap[music.itemId]
            )
        }
    }

    suspend fun toMusicExtendList(
        musicList: List<XyMusic>?,
        downloadDb: DownloadDatabaseClient,
        mediaLibraryId: String? = null
    ): List<XyMusicExtend>? {
        val safeMusicList = musicList ?: return null
        val downloadMap = buildDownloadMap(
            itemIds = safeMusicList.map { it.itemId },
            downloadDb = downloadDb,
            mediaLibraryId = mediaLibraryId
        )

        return safeMusicList.map { music ->
            XyMusicExtend(
                music = music,
                filePath = downloadMap[music.itemId]
            )
        }
    }

    suspend fun attachFilePathToMusicExtendList(
        musicExtendList: List<XyMusicExtend>?,
        downloadDb: DownloadDatabaseClient,
        mediaLibraryId: String? = null
    ): List<XyMusicExtend>? {
        val safeMusicExtendList = musicExtendList ?: return null
        val downloadMap = buildDownloadMap(
            itemIds = safeMusicExtendList.map { it.music.itemId },
            downloadDb = downloadDb,
            mediaLibraryId = mediaLibraryId
        )

        return safeMusicExtendList.map { musicExtend ->
            musicExtend.copy(filePath = downloadMap[musicExtend.music.itemId])
        }
    }

    suspend fun attachFilePath(
        playMusicList: List<XyPlayMusic>?,
        downloadDb: DownloadDatabaseClient,
        mediaLibraryId: String? = null
    ): List<XyPlayMusic>? {
        val safePlayMusicList = playMusicList ?: return null
        val downloadMap = buildDownloadMap(
            itemIds = safePlayMusicList.map { it.itemId },
            downloadDb = downloadDb,
            mediaLibraryId = mediaLibraryId
        )

        return safePlayMusicList.map { playMusic ->
            playMusic.copy(filePath = downloadMap[playMusic.itemId])
        }
    }

    suspend fun attachFilePath(
        playMusic: XyPlayMusic?,
        downloadDb: DownloadDatabaseClient,
        mediaLibraryId: String? = null
    ): XyPlayMusic? {
        return attachFilePath(
            playMusicList = playMusic?.let(::listOf),
            downloadDb = downloadDb,
            mediaLibraryId = mediaLibraryId
        )?.firstOrNull()
    }

    private suspend fun buildDownloadMap(
        itemIds: List<String>,
        downloadDb: DownloadDatabaseClient,
        mediaLibraryId: String?
    ): Map<String, String> {
        if (itemIds.isEmpty()) {
            return emptyMap()
        }

        return downloadDb.downloadDao
            .getDataByUids(
                musicIds = itemIds,
                mediaLibraryId = mediaLibraryId
            )
            .mapNotNull { download ->
                download.uid?.let { uid -> uid to download.filePath }
            }
            .toMap()
    }
}
