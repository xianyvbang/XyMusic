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

package cn.xybbz.entity.data

import cn.xybbz.api.client.subsonic.data.SongID3
import cn.xybbz.localdata.data.music.XyMusic

fun SongID3.toXyMusic(pic: String?, downloadUrl: String, connectionId: Long): XyMusic {
    return XyMusic(
        itemId = this.id,
        pic = pic,
        name = this.title,
        downloadUrl = downloadUrl,
        album = this.albumId,
        albumName = this.album,
        genreIds = this.genre,
        connectionId = connectionId,
        artists = this.artist,
        artistIds = this.artistId,
        albumArtist = this.artist,
        albumArtistIds = this.artistId,
        year = this.year,
        playedCount = 0,
        ifFavoriteStatus = this.starred != null,
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
        lastPlayedDate = 0L
    )
}