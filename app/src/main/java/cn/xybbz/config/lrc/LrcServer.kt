/*
 *   XyMusic
 *   Copyright (C) 2023 xianyvbang
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package cn.xybbz.config.lrc

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.common.enums.AllDataEnum
import cn.xybbz.common.enums.LrcDataType
import cn.xybbz.common.music.MusicController
import cn.xybbz.common.utils.CoroutineScopeUtils
import cn.xybbz.common.utils.LrcUtils.getIndex
import cn.xybbz.config.connection.ConnectionConfigServer
import cn.xybbz.entity.data.LrcEntryData
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.lrc.XyLrcConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class LrcServer(
    private val musicController: MusicController,
    private val ataSourceManager: DataSourceManager,
    private val db: DatabaseClient,
    private val connectionConfigServer: ConnectionConfigServer
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

    var itemId by mutableStateOf<String>("")
        private set

    val lrcCoroutineScope = CoroutineScopeUtils.getIo("LrcServer")

    var lrcConfig: XyLrcConfig? by mutableStateOf(null)
        private set

    init {
        lrcCoroutineScope.launch {

            combine(
                musicController.progressStateFlow,
                lcrEntryListFlow
            ) { progress, lrcList ->
                progress to lrcList
            }.collect { (progress, lrcList) ->

                if (lrcList.isNotEmpty()) {
                    val index = lrcList.getIndex(progress, getLrcConfig(itemId).lrcOffsetMs)
                    if (index != indexData) {
                        indexData = index
                        lrcText = lrcList[index].displayText
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
            if (_lcrEntryListFlow.value.isEmpty()) {
                musicController.musicInfo?.itemId?.let { itemId ->
                    val musicLyricList = ataSourceManager.getMusicLyricList(itemId)
                    if (!musicLyricList.isNullOrEmpty())
                        createLrcList(musicLyricList, LrcDataType.NETWORK)
                }
            }
        }
    }

    /**
     * 根据歌词列表创建歌词列表
     */
    fun createLrcList(lrcList: List<LrcEntryData>?, lrcDataType: LrcDataType) {
//        clear()
        if (!lrcList.isNullOrEmpty()) {
            lrcCoroutineScope.launch {
                musicController.musicInfo?.itemId?.let { itemId ->
                    initLrcConfig(itemId)
                    this@LrcServer.itemId = itemId
                }
            }

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

    fun clear() {
        _lcrEntryListFlow.update {
            emptyList()
        }
        lrcText = null
        indexData = -1
    }

    suspend fun initLrcConfig(itemId: String) {
        this.lrcConfig = db.lrcConfigDao.getLrcConfig(itemId) ?: XyLrcConfig(
            itemId = itemId,
            lrcOffsetMs = 0L,
            connectionId = connectionConfigServer.getConnectionId()
        )
    }

    fun getLrcConfig(itemId: String): XyLrcConfig {
        return this.lrcConfig ?: XyLrcConfig(
            itemId = itemId,
            lrcOffsetMs = 0L,
            connectionId = connectionConfigServer.getConnectionId()
        )
    }

    suspend fun updateLrcConfig(offsetMs: Long) {
        val config = getLrcConfig(itemId)
        lrcConfig = config.copy(lrcOffsetMs = offsetMs)
        if (config.id != AllDataEnum.All.code) {
            val xyLrcConfig =
                XyLrcConfig(
                    itemId = itemId,
                    lrcOffsetMs = offsetMs,
                    connectionId = connectionConfigServer.getConnectionId()
                )

            val id = db.lrcConfigDao.insert(xyLrcConfig)
            this.lrcConfig = xyLrcConfig.copy(id = id)
        } else {
            db.lrcConfigDao.update(config)
        }
    }

}