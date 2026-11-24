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

package cn.xybbz.config

import cn.xybbz.config.scope.IoScoped
import cn.xybbz.localdata.config.LocalDatabaseClient
import cn.xybbz.localdata.data.album.XyAlbum
import cn.xybbz.localdata.data.count.XyDataCount
import cn.xybbz.localdata.data.music.XyMusic
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalAtomicApi::class, FlowPreview::class)
class HomeDataRepository(
    private val db: LocalDatabaseClient
) : IoScoped() {

    init {
        createScope()
    }

    /** 首页数据监听只启动一次，避免 ViewModel 重建后重复订阅。 */
    private val observerStarted = AtomicBoolean(false)

    /** 串行刷新首页快照，保证同一时刻只占用一个读连接。 */
    private val refreshMutex = Mutex()

    private val _mostPlayedMusic = MutableStateFlow<List<XyMusic>>(emptyList())
    private val _newestAlbums = MutableStateFlow<List<XyAlbum>>(emptyList())
    private val _recentMusic = MutableStateFlow<List<XyMusic>>(emptyList())
    private val _recentAlbums = MutableStateFlow<List<XyAlbum>>(emptyList())
    private val _mostPlayedAlbums = MutableStateFlow<List<XyAlbum>>(emptyList())
    private val _recommendedMusic = MutableStateFlow<List<XyMusic>>(emptyList())
    private val _playlists = MutableStateFlow<List<XyAlbum>>(emptyList())
    private val _dataCount = MutableStateFlow<XyDataCount?>(null)

    //最多播放音乐
    val mostPlayedMusic: StateFlow<List<XyMusic>> get() = _mostPlayedMusic
    //最新专辑
    val newestAlbums: StateFlow<List<XyAlbum>> get() = _newestAlbums
    //最近播放音乐
    val recentMusic: StateFlow<List<XyMusic>> get() = _recentMusic
    //最近播放专辑
    val recentAlbums: StateFlow<List<XyAlbum>> get() = _recentAlbums
    //最多播放专辑
    val mostPlayedAlbums: StateFlow<List<XyAlbum>> get() = _mostPlayedAlbums
    //推荐音乐
    val recommendedMusic: StateFlow<List<XyMusic>> get() = _recommendedMusic
    //播放列表
    val playlists: StateFlow<List<XyAlbum>> get() = _playlists
    val dataCount: StateFlow<XyDataCount?> get() = _dataCount


    suspend fun initData() {
        loadOnce()
    }

    /**
     * 启动首页数据监听。
     *
     * 用一条表失效流汇总所有首页相关表的变更，再串行刷新快照，
     * 避免写库后多条重查询同时占满 Room 的读连接，拖慢其他页面。
     */
    fun startObservers() {
        if (!observerStarted.compareAndSet(expectedValue = false, newValue = true)) {
            return
        }
        scope.launch {
            db.invalidationTracker
                .createFlow(*HOME_DATA_TABLES)
                // 首页刷新会连续写多张表，先合并短时间内的多次失效。
                .debounce(OBSERVE_DEBOUNCE_MS.milliseconds)
                .conflate()
                .collect {
                    // 收集期间产生的新信号会被合并，不会并发发起多组查询。
                    refreshMutex.withLock { refreshSnapshot() }
                }
        }
    }


    /**
     * 首屏并发加载首页快照，用最短时间填满首页。
     * 运行期刷新走 [refreshSnapshot] 的串行版本，避免抢占读连接。
     */
    private suspend fun loadOnce() = coroutineScope {
        launch {
            _mostPlayedMusic.value = db.musicDao.selectMaximumPlayMusicExtendList(20)
        }
        launch {
            _newestAlbums.value =
                db.albumDao.selectNewestList(20)
        }
        launch {
            _recentMusic.value = db.musicDao.selectPlayHistoryMusicExtendList(20)
        }
        launch {
            _recentAlbums.value =
                db.albumDao.selectPlayHistoryAlbumList(20)
        }
        launch {
            _mostPlayedAlbums.value =
                db.albumDao.selectMaximumPlayAlbumList(20)
        }
        launch {
            _recommendedMusic.value = db.musicDao.selectRecommendedMusicExtendList(20)
        }
        launch {
            _playlists.value =
                db.albumDao.selectPlaylist()
        }
        launch {
            _dataCount.value =
                db.dataCountDao.selectOne()
        }
    }

    /**
     * 按当前数据库内容刷新首页各区块的快照。
     */
    private suspend fun refreshSnapshot() {
        _mostPlayedMusic.value = db.musicDao.selectMaximumPlayMusicExtendList(HOME_LIMIT)
        _newestAlbums.value = db.albumDao.selectNewestList(HOME_LIMIT)
        _recentMusic.value = db.musicDao.selectPlayHistoryMusicExtendList(HOME_LIMIT)
        _recentAlbums.value = db.albumDao.selectPlayHistoryAlbumList(HOME_LIMIT)
        _mostPlayedAlbums.value = db.albumDao.selectMaximumPlayAlbumList(HOME_LIMIT)
        _recommendedMusic.value = db.musicDao.selectRecommendedMusicExtendList(HOME_LIMIT)
        _playlists.value = db.albumDao.selectPlaylist()
        _dataCount.value = db.dataCountDao.selectOne()
    }

    private companion object {
        /** 首页各区块展示条数。 */
        const val HOME_LIMIT = 20

        /** 表变更合并窗口，避免一次刷新内的连续写入触发多轮查询。 */
        const val OBSERVE_DEBOUNCE_MS = 120L

        /** 影响首页各区块的数据表，任一表变化都会触发一次统一刷新。 */
        val HOME_DATA_TABLES = arrayOf(
            "xy_music",
            "xy_album",
            "xy_settings",
            "xy_connection_config",
            "xy_data_count",
            "xy_daily_recommend_history",
            "PlayHistoryMusic",
            "MaximumPlayMusic",
            "PlayHistoryAlbum",
            "MaximumPlayAlbum",
            "FavoriteMusic",
            "PlaylistMusic",
        )
    }
}
