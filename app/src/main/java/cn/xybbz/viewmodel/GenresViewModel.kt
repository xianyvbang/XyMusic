package cn.xybbz.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.config.BackgroundConfig
import cn.xybbz.config.ConnectionConfigServer
import cn.xybbz.localdata.data.genre.XyGenre
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@HiltViewModel
class GenresViewModel @Inject constructor(
    private val dataSourceManager: DataSourceManager,
    private val connectionConfigServer: ConnectionConfigServer,
    private val _backgroundConfig: BackgroundConfig
) : ViewModel() {

    val backgroundConfig = _backgroundConfig

    @OptIn(ExperimentalCoroutinesApi::class)
    val genresPage: Flow<PagingData<XyGenre>> =
        connectionConfigServer.loginSuccessEvent
            .flatMapLatest {
                dataSourceManager.selectGenresPage()
            }
            .cachedIn(viewModelScope)


}