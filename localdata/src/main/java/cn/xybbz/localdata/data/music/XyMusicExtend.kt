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

package cn.xybbz.localdata.data.music

import androidx.room.Embedded

data class XyMusicExtend(
    @Embedded
    val music: XyMusic,
    val filePath: String? = null
) {
    fun toPlayMusic(): XyPlayMusic {
        return XyPlayMusic(
            itemId = this.music.itemId,
            pic = music.pic,
            name = music.name,
            album = music.album,
            container = music.container,
            artists = music.artists,
            ifFavoriteStatus = music.ifFavoriteStatus,
            size = music.size,
            filePath = filePath,
            runTimeTicks = music.runTimeTicks,
            plexPlayKey = music.plexPlayKey,
            artistIds = music.artistIds
        )
    }
}
