package cn.xybbz.config.lrc

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cn.xybbz.api.client.IDataSourceManager
import cn.xybbz.common.enums.LrcDataType
import cn.xybbz.common.music.MusicController
import cn.xybbz.common.utils.CoroutineScopeUtils
import cn.xybbz.entity.data.LrcEntryData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LrcServer @Inject constructor(
    private val musicController: MusicController,
    private val ataSourceManager: IDataSourceManager
) {

    /**
     * 歌词信息
     */
    private val _lcrEntryListFlow = MutableStateFlow(emptyList<LrcEntryData>())
    val lcrEntryListFlow = _lcrEntryListFlow.asStateFlow()

    var indexData by mutableIntStateOf(-1)
        private set
    var lrcText by mutableStateOf<String?>(null)
        private set

    var itemId by mutableStateOf<String?>(null)
        private set

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
    fun getMusicLyricList() {
        lrcCoroutineScope.launch {
            if (_lcrEntryListFlow.value.isNotEmpty())
                musicController.musicInfo?.itemId?.let { itemId ->
                    val musicLyricList = ataSourceManager.getMusicLyricList(itemId)
                    if (!musicLyricList.isNullOrEmpty())
                        createLrcList(musicLyricList, LrcDataType.NETWORK)
                }
        }
    }

    /**
     * 根据歌词列表创建歌词列表
     */
    fun createLrcList(lrcList: List<LrcEntryData>?, lrcDataType: LrcDataType) {
        _lcrEntryListFlow.update {
            emptyList()
        }
        lrcText = null
        indexData = -1
        if (!lrcList.isNullOrEmpty()) {
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
        Log.i("createLrcList", "随机数111 ${lrcDataType} 歌词列表：${_lcrEntryListFlow.value}")
    }

}