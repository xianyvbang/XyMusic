package cn.xybbz.localdata.data.artist

import androidx.room.Embedded

data class XyArtistExt(
    @Embedded
    val artist: XyArtist,
    val favorite: Boolean? = false
)
