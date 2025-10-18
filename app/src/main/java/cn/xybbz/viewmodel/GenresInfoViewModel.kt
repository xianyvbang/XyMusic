package cn.xybbz.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import cn.xybbz.api.client.IDataSourceManager
import cn.xybbz.config.BackgroundConfig
import cn.xybbz.config.ConnectionConfigServer
import cn.xybbz.localdata.data.genre.XyGenre
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = GenresInfoViewModel.Factory::class)
class GenresInfoViewModel @AssistedInject constructor(
    @Assisted private val genreId: String,
    private val _dataSourceManager: IDataSourceManager,
    private val connectionConfigServer: ConnectionConfigServer,
    private val _backgroundConfig: BackgroundConfig
) : ViewModel() {

    val backgroundConfig = _backgroundConfig

    @AssistedFactory
    interface Factory {
        fun create(genreId: String): GenresInfoViewModel
    }



    var genreInfo by mutableStateOf<XyGenre?>(null)
        private set


    init {
        getGenreInfo()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    var albumList = connectionConfigServer.loginStateFlow.flatMapLatest { bool ->
        if (bool)
            _dataSourceManager.selectAlbumListByGenreId(genreId).distinctUntilChanged()
        else flow { }
    }.cachedIn(viewModelScope)

    private fun getGenreInfo() {
        viewModelScope.launch {
            genreInfo = _dataSourceManager.getGenreById(genreId)
        }
    }
}