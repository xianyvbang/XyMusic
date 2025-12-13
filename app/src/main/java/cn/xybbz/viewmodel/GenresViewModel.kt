package cn.xybbz.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.config.BackgroundConfig
import cn.xybbz.config.ConnectionConfigServer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

@HiltViewModel
class GenresViewModel @Inject constructor(
    private val dataSourceManager: DataSourceManager,
    private val connectionConfigServer: ConnectionConfigServer,
    private val _backgroundConfig: BackgroundConfig
) : ViewModel() {

    val backgroundConfig = _backgroundConfig

    @OptIn(ExperimentalCoroutinesApi::class)
    var genresPage =
        connectionConfigServer.loginStateFlow.flatMapLatest { bool ->
            if (bool) {
                dataSourceManager.selectGenresPage()
            } else {
                flow { }
            }
        }.distinctUntilChanged().cachedIn(viewModelScope)

}