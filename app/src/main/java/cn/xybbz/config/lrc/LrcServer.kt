package cn.xybbz.config.lrc

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cn.xybbz.api.client.IDataSourceManager
import cn.xybbz.common.utils.CoroutineScopeUtils
import cn.xybbz.config.ConnectionConfigServer
import cn.xybbz.entity.data.LrcEntryData
import kotlinx.coroutines.launch

object LrcServer {

    /**
     * 歌词信息
     */
    var lcrEntryList by mutableStateOf<List<LrcEntryData>>(emptyList())
        private set


    var indexData by mutableIntStateOf(-1)
    var lrcText by mutableStateOf("")

    val lrcCoroutineScope = CoroutineScopeUtils.getIo("LrcServer")


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
        lcrEntryList = emptyList()
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
            lcrEntryList = list
        }
    }


    /**
     * 根据索引获得歌词列表
     */
    fun getLrcIndex(currentPosition: Long) {
        lrcCoroutineScope.launch {
            if (lcrEntryList.isNotEmpty()) {
                indexData = lcrEntryList.indexOfFirst { lrc ->
                    lrc.startTime <= currentPosition && currentPosition < lrc.endTime
                }
                lcrEntryList.let {
                    if (indexData == -1) {
                        indexData = it.size - 1
                    }
                    lrcText = it[indexData].displayText
                }
            }
        }
    }

}