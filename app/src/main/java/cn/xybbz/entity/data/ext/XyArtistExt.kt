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


import cn.xybbz.api.client.subsonic.data.ArtistID3
import cn.xybbz.localdata.data.artist.XyArtist


fun ArtistID3.convertToArtist(
    pic:String?,
    backdrop:String?,
    index: String? = null,
    indexNumber: Int,
    connectionId:Long,
): XyArtist {
    return XyArtist(
        artistId = this.id,
        pic = pic,
        backdrop = backdrop,
        name = this.name,
        connectionId = connectionId,
        selectChat = index ?: "",
        ifFavorite = !this.starred.isNullOrBlank(),
        indexNumber = indexNumber
    )
}

/**
 * 艺术家详情信息
 */
data class XyArtistInfo(
    val artist: XyArtist? = null,
    val similarArtist: List<XyArtist>? = null
)