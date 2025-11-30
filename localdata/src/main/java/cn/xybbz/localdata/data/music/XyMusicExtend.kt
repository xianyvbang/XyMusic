package cn.xybbz.localdata.data.music

import androidx.room.Embedded

data class XyMusicExtend(
    @Embedded
    val music: XyMusic,
    val filePath:String? = null
){
    fun toPlayMusic(): XyPlayMusic{
        return XyPlayMusic(
            itemId = this.music.itemId,
            pic = music.pic,
            name = music.name,
            album = music.album,
            musicUrl = music.musicUrl,
            container = music.container,
            artists = music.artists,
            ifFavoriteStatus = music.ifFavoriteStatus,
            size = music.size,
            filePath = filePath
        )
    }
}
