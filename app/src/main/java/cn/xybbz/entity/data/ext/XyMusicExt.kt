/*
 *   XyMusic
 *   Copyright (C) 2023 xianyvbang
 *  
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *  
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package cn.xybbz.entity.data.ext

import cn.xybbz.api.client.subsonic.data.SongID3
import cn.xybbz.api.client.subsonic.data.Songs
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.utils.DateUtil.toSecondMs
import cn.xybbz.localdata.common.LocalConstants
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.data.music.XyMusicExtend
import cn.xybbz.localdata.data.music.XyPlayMusic

fun SongID3.toXyMusic(pic: String?, downloadUrl: String, connectionId: Long): XyMusic {
    return XyMusic(
        itemId = this.id,
        pic = pic,
        name = this.title,
        downloadUrl = downloadUrl,
        album = this.albumId,
        albumName = this.album,
        genreIds = this.genre?.let { listOf(it) },
        connectionId = connectionId,
        artists = this.artist?.split(Constants.ARTIST_DELIMITER_SEMICOLON),
        artistIds = this.artistId?.let { listOf(it) },
        albumArtist = this.artist?.let { listOf(it) },
        albumArtistIds = this.artistId?.let { listOf(it) },
        year = this.year,
        playedCount = 0,
        ifFavoriteStatus = !this.starred.isNullOrBlank(),
        path = this.path,
        bitRate = this.bitRate,
        sampleRate = 0,
        bitDepth = 0,
        size = this.size,
        runTimeTicks = this.duration,
        container = this.suffix,
        codec = this.suffix,
        ifLyric = true,
        lyric = "",
        playlistItemId = this.id,
        lastPlayedDate = 0L,
        createTime = this.created.toSecondMs()
    )
}

fun XyMusicExtend.toPlayerMusic(): XyPlayMusic {
    return XyPlayMusic(
        itemId = this.music.itemId,
        pic = music.pic,
        name = music.name,
        album = music.album,
        container = music.container,
        artists = music.artists,
        artistIds = music.artistIds,
        ifFavoriteStatus = music.ifFavoriteStatus,
        size = music.size,
        filePath = filePath,
        runTimeTicks = music.runTimeTicks,
        plexPlayKey = music.plexPlayKey
    )
}

fun List<String>.joinToString(): String {
    return this.joinToString(LocalConstants.ARTIST_DELIMITER)
}

fun <T> List<T>.joinToString(transform: ((T) -> CharSequence)): String {
    return this.joinToString(LocalConstants.ARTIST_DELIMITER, transform = transform)
}

fun Songs?.toXyMusic(
    connectionId: Long,
    createDownloadUrl: (String) -> String,
    getImageUrl: (String) -> String
): List<XyMusic>? {
    return this?.song?.map { music ->
        music.toXyMusic(
            pic = if (music.coverArt.isNullOrBlank()) null else music.coverArt?.let {
                getImageUrl(
                    it
                )
            },
            downloadUrl = createDownloadUrl(music.id),
            connectionId = connectionId
        )
    }
}