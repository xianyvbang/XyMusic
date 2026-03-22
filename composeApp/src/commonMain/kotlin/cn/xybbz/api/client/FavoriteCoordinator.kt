package cn.xybbz.api.client

import cn.xybbz.common.enums.MusicTypeEnum
import cn.xybbz.common.music.MusicController

object FavoriteCoordinator {

    suspend fun setFavoriteData(
        dataSourceManager: DataSourceManager,
        type: MusicTypeEnum,
        itemId: String,
        ifFavorite: Boolean,
        musicController: MusicController? = null
    ): Boolean {
        val favorite = dataSourceManager.setFavoriteData(
            type = type,
            itemId = itemId,
            ifFavorite = ifFavorite
        )
        if (type == MusicTypeEnum.MUSIC && favorite != ifFavorite && musicController?.musicInfo?.itemId == itemId) {
            musicController.updateCurrentFavorite(favorite)
        }
        return favorite
    }
}
