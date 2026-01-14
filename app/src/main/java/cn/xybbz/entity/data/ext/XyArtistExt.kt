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

import cn.xybbz.api.client.data.XyResponse
import cn.xybbz.api.client.subsonic.data.SubsonicArtistInfoResponse
import cn.xybbz.localdata.data.artist.XyArtist

fun SubsonicArtistInfoResponse?.toArtists(connectionId: Long): XyResponse<XyArtist>{
    return XyResponse(
        items = this?.artistInfo?.similarArtist?.map { artist ->
            XyArtist(
                artistId = artist.id,
                name = artist.name,
                connectionId = connectionId,
            )
        },
        totalRecordCount = 0,
        startIndex = 0
    )
}