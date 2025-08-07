package cn.xybbz.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import cn.xybbz.config.ConnectionConfigServer
import cn.xybbz.config.IDataSourceManager
import cn.xybbz.entity.data.ArtistFilter
import cn.xybbz.localdata.config.DatabaseClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 艺术家
 */
@HiltViewModel
class ArtistViewModel @Inject constructor(
    private val dataSourceManager: IDataSourceManager,
    private val connectionConfigServer: ConnectionConfigServer,
    private val db: DatabaseClient
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
    var artistList =
        connectionConfigServer.loginStateFlow.flatMapLatest { bool ->
            if (bool) {
                _sortType.flatMapLatest { sortBy ->
                    Log.i("=====", "数据变化 ${sortBy.isFavorite}")
                    val pageFlow = dataSourceManager.selectArtistFlowList(
                        sortBy.isFavorite,
                        null
                    ).distinctUntilChanged()
                    pageFlow
                }
            } else {
                flow {}
            }
        }.distinctUntilChanged().cachedIn(viewModelScope)


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