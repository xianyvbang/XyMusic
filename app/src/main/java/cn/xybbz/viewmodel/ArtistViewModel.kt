package cn.xybbz.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.filter
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.config.BackgroundConfig
import cn.xybbz.config.ConnectionConfigServer
import cn.xybbz.entity.data.ArtistFilter
import cn.xybbz.localdata.config.DatabaseClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 艺术家
 */
@HiltViewModel
class ArtistViewModel @Inject constructor(
    private val dataSourceManager: DataSourceManager,
    connectionConfigServer: ConnectionConfigServer,
    private val db: DatabaseClient,
    val backgroundConfig: BackgroundConfig
) : ViewModel() {


    private val _sortType = MutableStateFlow(ArtistFilter())

    var ifFavorite by mutableStateOf<Boolean?>(null)
        private set

    val sortBy: StateFlow<ArtistFilter> = _sortType

    /**
     * 艺术家页面右侧筛选字符
     */
    var selectArtistChars by mutableStateOf(emptyList<Char>())
        private set

    init {
        getSelectCharList()
    }

    /**
     * 艺术家列表
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    var artistListPage =
        connectionConfigServer.loginStateFlow.flatMapLatest { bool ->
            if (bool) {
                dataSourceManager.selectArtistFlowList()
                    .distinctUntilChanged()
                    .cachedIn(viewModelScope)
            } else {
                emptyFlow()
            }
        }.cachedIn(viewModelScope)

    val artistList=
        combine(artistListPage, sortBy) { pagingData, favorite ->
            pagingData.filter { artist ->
                favorite.isFavorite == null || artist.favorite == favorite.isFavorite
            }
        }.cachedIn(viewModelScope)


    fun setFavoriteFilterData(isFavorite: Boolean?) {
        ifFavorite = isFavorite
        _sortType.update {
            it.isFavorite = isFavorite
            it.copy()
        }
    }

    /**
     * 根据选择的字母获得index
     */
    suspend fun selectIndexBySelectChat(selectChat: String): Int {
        return db.artistDao.selectIndexBySelectChat(selectChat)
    }

    /**
     * 获得所有艺术家页面右侧筛选字符
     */
    fun getSelectCharList() {
        viewModelScope.launch {
            db.artistDao.getSelectCharList().collect {
                selectArtistChars = it.map { it[0].uppercaseChar() }.distinct()
            }
        }
    }
}