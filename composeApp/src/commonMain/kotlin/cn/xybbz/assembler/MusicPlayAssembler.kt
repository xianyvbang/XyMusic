package cn.xybbz.assembler

import cn.xybbz.download.database.DownloadDatabaseClient
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.data.music.XyPlayMusic

object MusicPlayAssembler {

    fun toPlayMusic(
        music: XyMusic,
        filePath: String? = null,
        ifFavoriteStatus: Boolean
    ): XyPlayMusic {
        return XyPlayMusic(
            itemId = music.itemId,
            pic = music.pic,
            name = music.name,
            album = music.album,
            albumName = music.albumName,
            container = music.container,
            artists = music.artists,
            artistIds = music.artistIds,
            ifFavoriteStatus = ifFavoriteStatus,
            size = music.size,
            filePath = filePath,
            runTimeTicks = music.runTimeTicks,
            plexPlayKey = music.plexPlayKey,
            ifHls = false,
            musicUrl = "",
            static = true,
            audioBitRate = 0
        )
    }

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
            toPlayMusic(
                music = music,
                filePath = downloadMap[music.itemId],
                music.ifFavoriteStatus
            )
        }
    }

    suspend fun toPlayMusic(
        music: XyMusic?,
        downloadDb: DownloadDatabaseClient,
        mediaLibraryId: String? = null
    ): XyPlayMusic? {
        return toPlayMusicList(
            musicList = music?.let(::listOf),
            downloadDb = downloadDb,
            mediaLibraryId = mediaLibraryId
        )?.firstOrNull()
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
