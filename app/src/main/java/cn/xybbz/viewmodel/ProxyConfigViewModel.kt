package cn.xybbz.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.xybbz.localdata.config.DatabaseClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ProxyConfigViewModel @Inject constructor(
    private val db: DatabaseClient
) : ViewModel() {

    val proxyConfig = db.proxyConfigDao.getConfigFlow().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )
}