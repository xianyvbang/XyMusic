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

package cn.xybbz.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.api.events.ReLoginEvent
import cn.xybbz.common.enums.LoginType
import cn.xybbz.common.utils.DateUtil
import cn.xybbz.common.utils.Log
import cn.xybbz.config.music.MusicCommonController
import cn.xybbz.config.music.PlayerEventCoordinator
import cn.xybbz.config.select.SelectControl
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.config.setting.TranscodingState
import cn.xybbz.entity.data.PlayerTypeData
import cn.xybbz.localdata.config.LocalDatabaseClient
import cn.xybbz.localdata.data.era.XyEraItem
import cn.xybbz.localdata.data.music.XyMusicExtend
import cn.xybbz.localdata.enums.PlayerTypeEnum
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.koin.core.annotation.KoinViewModel
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.era_title_decade
import xymusic_kmp.composeapp.generated.resources.list_loop
import xymusic_kmp.composeapp.generated.resources.repeat_24px
import xymusic_kmp.composeapp.generated.resources.repeat_one_24px
import xymusic_kmp.composeapp.generated.resources.shuffle_24px
import xymusic_kmp.composeapp.generated.resources.shuffle_play
import xymusic_kmp.composeapp.generated.resources.single_loop


@KoinViewModel
class MainViewModel(
    val db: LocalDatabaseClient,
    private val musicController: MusicCommonController,
    val dataSourceManager: DataSourceManager,
    val settingsManager: SettingsManager,
    private val playerEventCoordinator: PlayerEventCoordinator,
    val selectControl: SelectControl
) : ViewModel() {

    var eraItemList by mutableStateOf<List<XyEraItem>>(emptyList())
        private set

    //从1900年到当前年份的set列表
    val yearSet by mutableStateOf(DateUtil.getYearSet())

    val favoriteSet = db.musicDao.selectFavoriteListFlow()

    /**
     * 播放页展示的相似歌曲列表。
     * 数据由 PlayerEventCoordinator 维护，这里只做只读暴露。
     */
    val similarMusicList: List<XyMusicExtend>
        get() = playerEventCoordinator.similarMusicList

    /**
     * 播放页展示的热门歌曲列表。
     * 数据由 PlayerEventCoordinator 维护，这里只做只读暴露。
     */
    val popularMusicList: List<XyMusicExtend>
        get() = playerEventCoordinator.popularMusicList


    init {
        Log.i("=====", "MainViewModel初始化")
        startLoginEventBus()
        //初始化年代数据
        initEraData()
        // 这里只监听轻量 UI 信号，不再直接订阅播放器事件总线。
        initSongChangeObserver()
        //初始化版本信息获取
        initGetVersionInfo()
        //设置转码监听
        initTranscodeListener()
    }

    private fun initSongChangeObserver() {
        viewModelScope.launch {
            // 切歌后重置跑马灯次数，这属于页面表现层逻辑，仍放在 ViewModel 中处理。
            snapshotFlow { playerEventCoordinator.songChangeVersion }
                .collect {
                    if (it > 0) {
                        putIterations(0)
                    }
                }
        }
    }

    /**
     * 滚动次数
     */
    var iterations by mutableIntStateOf(1)
        private set

    fun putIterations(iterations: Int) {
        this.iterations = iterations
    }


    /**
     * 音乐播放页面是否显示
     */
    var sheetState by mutableStateOf(false)
        private set

    fun putSheetState(sheetState: Boolean) {
        this.sheetState = sheetState
    }


    //region 音乐循环类型
    val iconList by mutableStateOf(
        listOf(
            PlayerTypeData(icon = Res.drawable.repeat_one_24px, message = Res.string.single_loop),
            PlayerTypeData(icon = Res.drawable.repeat_24px, message = Res.string.list_loop),
            PlayerTypeData(icon = Res.drawable.shuffle_24px, message = Res.string.shuffle_play),
        )
    )


    fun setNowPlayerTypeData() {
        Log.i("=====", "当前播放类型${musicController.playType}")

        when (musicController.playType) {
            PlayerTypeEnum.RANDOM_PLAY -> musicController.setPlayTypeData(PlayerTypeEnum.SINGLE_LOOP)
            PlayerTypeEnum.SINGLE_LOOP -> musicController.setPlayTypeData(PlayerTypeEnum.SEQUENTIAL_PLAYBACK)
            PlayerTypeEnum.SEQUENTIAL_PLAYBACK -> musicController.setPlayTypeData(
                PlayerTypeEnum.RANDOM_PLAY
            )
        }
    }

    //endregion

    /**
     * 初始化年代数据
     */
    private fun initEraData() {
        viewModelScope.launch {
            val eraStart = 1970
            //当前年代
            val year = DateUtil.thisYear()
            val era = (year / 10) * 10

            val eraItemList = db.eraItemDao.selectList()
            if (eraItemList.isEmpty()) {
                val eraList = mutableListOf<XyEraItem>()
                //生成数据
                val num = (era - eraStart) / 10
                for (index in 0..num) {
                    val years = mutableListOf<Int>()
                    val thisEra = eraStart + index * 10

                    for (i in 0 until 10) {
                        years.add((thisEra + i))
                    }
                    eraList.add(
                        XyEraItem(
                            title = "$thisEra",
                            era = thisEra,
                            years = years
                        )
                    )

                }
                db.eraItemDao.saveBatch(eraList)
                Log.i("=====", "当前的年代: $era")
            } else {
                val earItem = db.eraItemDao.selectOneByEra(era)
                if (earItem != null) {
                    val years = earItem.years
                    val yearsNew = mutableListOf<String>()
                    if (!years.contains(year)) {
                        for (i in 0 until 10) {
                            yearsNew.add((era + i).toString())
                        }
                        db.eraItemDao.updateById(
                            earItem.copy(
                                years = years
                            )
                        )
                    }

                } else {
                    val years = mutableListOf<Int>()
                    for (i in 0 until 10) {
                        years.add((era + i))
                    }
                    db.eraItemDao.saveBatch(
                        XyEraItem(
                            title = getString(Res.string.era_title_decade, era),
                            era = era,
                            years = years
                        )
                    )

                }
            }
            getEraList()
        }
    }


    /**
     * 获得年代数据
     */
    private suspend fun getEraList() {
        eraItemList = db.eraItemDao.selectList()
    }

    /**
     * 初始化版本信息获取
     */
    fun initGetVersionInfo() {
//        versionCheckScheduler.enqueueIfNeeded()
    }

    fun initTranscodeListener() {
        viewModelScope.launch {
            settingsManager.transcodingFlow.collect {
                if (it is TranscodingState.NetWorkChange && (settingsManager.get().ifTranscoding
                            || settingsManager.get().mobileNetworkAudioBitRate
                            == settingsManager.get().wifiNetworkAudioBitRate
                            )
                ) {
                    return@collect
                }
                // 转码配置变化后，需要让当前播放列表中的地址重新替换为最新策略。
                musicController.replacePlaylistItemUrl()
            }
        }
    }

    /**
     * 启动登陆监听重试登陆
     */

    @OptIn(ExperimentalCoroutinesApi::class)
    fun startLoginEventBus() {
        viewModelScope.launch {
            dataSourceManager.dataSourceServerFlow
                .filterNotNull()
                .flatMapLatest { server ->
                    server.getApiClient().eventBus.events
                }
                .onEach { event ->
                    if (event is ReLoginEvent.Unauthorized) dataSourceManager.serverLogin(
                        loginType = LoginType.API,
                        db.connectionConfigDao.selectConnectionConfig()
                    )
                }
                .launchIn(viewModelScope)
        }
    }

    fun updateIfShowSnackBar(ifShowSnackBar: Boolean) {
        settingsManager.updateIfShowSnackBar(ifShowSnackBar)
    }

}

