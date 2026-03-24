package cn.xybbz.music

import cn.xybbz.config.music.MusicCommonController
import cn.xybbz.localdata.data.music.XyPlayMusic

class IosMusicController : MusicCommonController() {

    override fun addMusicList(
        musicList: List<XyPlayMusic>,
        artistId: String?,
        isPlayer: Boolean?
    ) {
    }

    override fun updateCurrentFavorite(isFavorite: Boolean) {
    }

    override fun clearPlayerList() {
    }
}
