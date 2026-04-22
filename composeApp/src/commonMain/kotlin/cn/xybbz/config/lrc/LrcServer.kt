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

import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.api.client.custom.CustomMediaApiClient
import cn.xybbz.api.client.custom.data.CustomLyricsQuery
import cn.xybbz.common.enums.AllDataEnum
import cn.xybbz.common.enums.LrcDataType
import cn.xybbz.common.utils.Log
import cn.xybbz.common.utils.LrcUtils.getIndex
import cn.xybbz.config.music.MusicCommonController
import cn.xybbz.config.scope.IoScoped
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.entity.data.LrcEntryData
import cn.xybbz.localdata.config.LocalDatabaseClient
import cn.xybbz.localdata.data.lrc.XyLrcConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * 歌词显示主状态。
 */
data class LrcState(
    // 当前高亮歌词索引
    val indexData: Int = -1,
    // 当前高亮歌词文本
    val lrcText: String? = null,
    // 当前歌词所属歌曲 id
    val itemId: String = "",
    // 当前歌词偏移配置
    val lrcConfig: XyLrcConfig? = null
)

class LrcServer(
    private val musicController: MusicCommonController,
    private val dataSourceManager: DataSourceManager,
    private val db: LocalDatabaseClient,
    private val settingsManager: SettingsManager,
    private val customMediaApiClient: CustomMediaApiClient
) : IoScoped() {

    /**
     * 歌词信息
     */
    private val _lcrEntryListFlow = MutableStateFlow(emptyList<LrcEntryData>())
    val lcrEntryListFlow = _lcrEntryListFlow.asStateFlow()

    // 歌词显示状态的唯一响应式来源
    private val _lrcStateFlow = MutableStateFlow(LrcState())
    val lrcStateFlow = _lrcStateFlow.asStateFlow()

    // 当前高亮歌词索引
    val indexData: Int
        get() = lrcStateFlow.value.indexData

    // 当前高亮歌词文本
    val lrcText: String?
        get() = lrcStateFlow.value.lrcText

    // 当前歌词所属歌曲 id
    val itemId: String
        get() = lrcStateFlow.value.itemId

    // 当前歌词偏移配置
    val lrcConfig: XyLrcConfig?
        get() = lrcStateFlow.value.lrcConfig

    /**
     * 初始化歌词监听逻辑。
     */
    fun init(coroutineContext: CoroutineContext) {
        createScope(coroutineContext)
        scope.launch {
            combine(
                musicController.progressStateFlow,
                lcrEntryListFlow
            ) { progress, lrcList ->
                progress to lrcList
            }.collect { (progress, lrcList) ->
                if (lrcList.isNotEmpty()) {
                    val index = lrcList.getIndex(progress, getLrcConfig(itemId).lrcOffsetMs)
                    if (index != indexData && index != -1) {
                        _lrcStateFlow.update {
                            it.copy(
                                indexData = index,
                                lrcText = lrcList[index].displayText
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * 拉取当前歌曲歌词。
     */
    fun getMusicLyricList() {
        scope.launch {
            if (_lcrEntryListFlow.value.isEmpty()) {
                musicController.musicInfo?.itemId?.let { itemId ->
                    val settings = settingsManager.get()
                    val musicLyricList = if (settings.ifPriorityMusicApi) {
                        // 歌词查询只依赖歌曲元数据，不需要触发封面优先级逻辑。
                        // API 模块负责网络请求，App 模块负责业务数据组装与 LRC 解析。
                        getMusicLyricListByMusicService(itemId) ?: getMusicLyricListByCustomApi(itemId)
                    } else {
                        getMusicLyricListByCustomApi(itemId) ?: getMusicLyricListByMusicService(itemId)
                    }
                    if (!musicLyricList.isNullOrEmpty()) {
                        createLrcList(musicLyricList, LrcDataType.NETWORK)
                    }
                }
            }
        }
    }

    /**
     * 获得音乐歌词信息
     */
    private suspend fun getMusicLyricListByMusicService(itemId: String): List<LrcEntryData>? {
        return dataSourceManager.getMusicLyricList(itemId)?.takeIf { it.isNotEmpty() }
    }

    /**
     * 通过自定义歌词接口获取歌词。
     */
    private suspend fun getMusicLyricListByCustomApi(itemId: String): List<LrcEntryData>? {
        return try {
            val musicInfo = dataSourceManager.selectMusicInfoById(itemId) ?: return null
            val settings = settingsManager.get()
            val customLrcSingleApi = settings.customLrcSingleApi.trim()
            if (customLrcSingleApi.isBlank()) {
                return null
            }

            val query = CustomLyricsQuery(
                singleApi = customLrcSingleApi,
                authKey = settings.customLrcApiAuth,
                title = musicInfo.name,
                artist = musicInfo.artists?.firstOrNull().orEmpty(),
                album = musicInfo.albumName.orEmpty(),
                path = musicInfo.path
            )

            val lyricsText = customMediaApiClient.getLyricsText(query) ?: return null
            val lyricsList = cn.xybbz.common.utils.LrcUtils.parseLrc(lyricsText)
            lyricsList.takeIf { it.isNotEmpty() }
        } catch (e: Exception) {
            Log.e("LrcServer", "自定义歌词接口获取失败", e)
            null
        }
    }

    /**
     * 根据歌词列表创建歌词列表
     */
    fun createLrcList(lrcList: List<LrcEntryData>?, lrcDataType: LrcDataType) {
        if (!lrcList.isNullOrEmpty()) {
            scope.launch {
                musicController.musicInfo?.itemId?.let { musicItemId ->
                    initLrcConfig(musicItemId)
                    _lrcStateFlow.update {
                        it.copy(itemId = musicItemId)
                    }
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
        Log.i("createLrcList", "随机数111 $lrcDataType 歌词列表：${_lcrEntryListFlow.value}")
    }

    /**
     * 清空当前歌词状态。
     */
    fun clear() {
        _lcrEntryListFlow.update {
            emptyList()
        }
        _lrcStateFlow.update {
            it.copy(
                lrcText = null,
                indexData = -1
            )
        }
    }

    /**
     * 初始化当前歌曲的歌词配置。
     */
    suspend fun initLrcConfig(itemId: String) {
        val config = db.lrcConfigDao.getLrcConfig(itemId) ?: XyLrcConfig(
            itemId = itemId,
            lrcOffsetMs = 0L,
            connectionId = dataSourceManager.getConnectionId()
        )
        _lrcStateFlow.update {
            it.copy(lrcConfig = config)
        }
    }

    /**
     * 获取歌词偏移配置，没有则返回默认配置。
     */
    fun getLrcConfig(itemId: String): XyLrcConfig {
        return lrcConfig ?: XyLrcConfig(
            itemId = itemId,
            lrcOffsetMs = 0L,
            connectionId = dataSourceManager.getConnectionId()
        )
    }

    /**
     * 更新歌词偏移配置。
     */
    suspend fun updateLrcConfig(offsetMs: Long) {
        val config = getLrcConfig(itemId)
        val updatedConfig = config.copy(lrcOffsetMs = offsetMs)
        _lrcStateFlow.update {
            it.copy(lrcConfig = updatedConfig)
        }
        if (config.id != AllDataEnum.All.code) {
            val xyLrcConfig = XyLrcConfig(
                itemId = itemId,
                lrcOffsetMs = offsetMs,
                connectionId = dataSourceManager.getConnectionId()
            )

            val id = db.lrcConfigDao.insert(xyLrcConfig)
            _lrcStateFlow.update {
                it.copy(lrcConfig = xyLrcConfig.copy(id = id))
            }
        } else {
            db.lrcConfigDao.update(config)
        }
    }

    override fun close() {
        super.close()
        clear()
    }
}
