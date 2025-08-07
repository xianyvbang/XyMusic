package cn.xybbz.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import cn.xybbz.config.ConnectionConfigServer
import cn.xybbz.config.IDataSourceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

@HiltViewModel
class GenresViewModel @Inject constructor(
    private val dataSourceManager: IDataSourceManager,
    private val connectionConfigServer: ConnectionConfigServer
) : ViewModel() {


    @OptIn(ExperimentalCoroutinesApi::class)
    var genresPage =
        connectionConfigServer.loginStateFlow.flatMapLatest { bool ->
            if (bool) {
                dataSourceManager.selectGenresPage()
            }else {
                flow {  }
            }
        }.distinctUntilChanged().cachedIn(viewModelScope)

}