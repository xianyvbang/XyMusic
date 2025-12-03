package cn.xybbz.config.lrc

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cn.xybbz.api.client.IDataSourceManager
import cn.xybbz.common.music.MusicController
import cn.xybbz.common.utils.CoroutineScopeUtils
import cn.xybbz.config.ConnectionConfigServer
import cn.xybbz.entity.data.LrcEntryData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LrcServer @Inject constructor(
    private val musicController: MusicController
) {

    /**
     * 歌词信息
     */
    private val _lcrEntryListFlow = MutableStateFlow(emptyList<LrcEntryData>())
    val lcrEntryListFlow = _lcrEntryListFlow.asStateFlow()

    var indexData by mutableIntStateOf(-1)
    var lrcText by mutableStateOf("")

    val lrcCoroutineScope = CoroutineScopeUtils.getIo("LrcServer")

    init {
        lrcCoroutineScope.launch {
            musicController.progressStateFlow.collect { progress ->
                _lcrEntryListFlow.value.let { lcrEntryList ->
                    //播放器的播放进度，单位毫秒
                    val index =
                        lcrEntryList.indexOfFirst { item -> item.startTime <= progress && progress < item.endTime }
                    if (index >= 0) {
                        this@LrcServer.indexData = index
                        this@LrcServer.lrcText = lcrEntryList[index].displayText
                    }
                }
            }
        }
    }

    /**
     * 获得音乐歌词信息
     */
    fun getMusicLyricList(
        itemId: String,
        connectionConfigServer: ConnectionConfigServer,
        dataSourceManager: IDataSourceManager
    ) {
        lrcCoroutineScope.launch {
            try {
                connectionConfigServer.loginStateFlow.collect { bool ->
                    if (bool) {
                        val musicLyricList = dataSourceManager.getMusicLyricList(itemId)
                        createLrcList(musicLyricList)
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 根据歌词列表创建歌词列表
     */
    fun createLrcList(lrcList: List<LrcEntryData>?) {
        _lcrEntryListFlow.update {
            emptyList()
        }
        lrcText = ""
        indexData = -1
        if (lrcList?.isNotEmpty() == true) {
            val list = lrcList.sortedBy { it.startTime }
            for (i in list.indices) {
                if (i == list.size - 1) {
                    list[i].endTime = Long.MAX_VALUE
                } else {
                    list[i].endTime = list[i + 1].startTime
                }
            }
            _lcrEntryListFlow.update {
                list
            }
        }
        Log.i("createLrcList", "歌词列表：${_lcrEntryListFlow.value}")
    }

}