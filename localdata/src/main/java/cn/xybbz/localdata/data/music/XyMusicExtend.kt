package cn.xybbz.localdata.data.music

import androidx.room.Embedded

data class XyMusicExtend(
    @Embedded
    val music: XyMusic,
    val filePath:String? = null
)
