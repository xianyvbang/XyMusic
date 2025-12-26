package cn.xybbz.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.utils.DefaultObjectUtils
import cn.xybbz.config.BackgroundConfig
import cn.xybbz.config.connection.ConnectionConfigServer
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.library.XyLibrary
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = SelectLibraryViewModel.Factory::class)
class SelectLibraryViewModel @AssistedInject constructor(
    @Assisted private val connectionId: Long,
    @Assisted private val thisLibraryId: String?,
    private val db: DatabaseClient,
    private val _connectionConfigServer: ConnectionConfigServer,
    private val _backgroundConfig: BackgroundConfig
) : ViewModel() {

    val backgroundConfig = _backgroundConfig

    @AssistedFactory
    interface Factory {
        fun create(connectionId: Long, thisLibraryId: String?): SelectLibraryViewModel
    }

    //媒体库
    val libraryList = mutableStateListOf<XyLibrary>()


    //当前媒体库id
    var libraryId by mutableStateOf<String?>(null)
        private set

    init {
        getLibraryList()
    }

    /**
     * 获得媒体库
     */
    private fun getLibraryList() {
        viewModelScope.launch {
            libraryId = thisLibraryId ?: Constants.MINUS_ONE_INT.toString()
            val libraryData = db.libraryDao.selectListByDataSourceType()
            libraryList.add(
                DefaultObjectUtils.getDefaultXyLibrary(connectionId)
            )
            if (libraryData.isNotEmpty()) {
                libraryList.addAll(libraryData)
            }
        }
    }


    /**
     * 设置媒体库id
     */
    fun updateLibraryId(data: String) {
        libraryId = data
        //更新媒体库
        viewModelScope.launch {
            var tmpData: String? = data
            if (data == Constants.MINUS_ONE_INT.toString()) {
                tmpData = null
            }
            db.connectionConfigDao.updateLibraryId(
                libraryId = tmpData,
                connectionId = connectionId
            )
            if (_connectionConfigServer.getConnectionId() == connectionId) {
                _connectionConfigServer.updateLibraryId(tmpData)
            }
        }
    }

}