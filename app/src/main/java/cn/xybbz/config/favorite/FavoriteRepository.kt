package cn.xybbz.config.favorite

import cn.xybbz.common.utils.CoroutineScopeUtils
import cn.xybbz.localdata.config.DatabaseClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepository @Inject constructor(
    val db: DatabaseClient
) {
    val scope = CoroutineScopeUtils.getIo("FavoriteRepository")
    val favoriteSet = db.musicDao.selectFavoriteList().map { it.toSet() }
        .stateIn(
            scope,
            SharingStarted.WhileSubscribed(5000),
            emptySet()
        )
}
