package cn.xybbz.viewmodel

import android.icu.math.BigDecimal
import android.icu.math.MathContext
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import androidx.room.withTransaction
import cn.xybbz.R
import cn.xybbz.api.client.IDataSourceManager
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.enums.MusicTypeEnum
import cn.xybbz.common.music.CacheController
import cn.xybbz.common.music.MusicController
import cn.xybbz.common.utils.DateUtil
import cn.xybbz.config.ConnectionConfigServer
import cn.xybbz.config.SettingsConfig
import cn.xybbz.config.alarm.AlarmConfig
import cn.xybbz.config.favorite.FavoriteRepository
import cn.xybbz.config.lrc.LrcServer
import cn.xybbz.config.select.SelectControl
import cn.xybbz.config.update.ApkUpdateManager
import cn.xybbz.entity.data.PlayerTypeData
import cn.xybbz.entity.data.music.MusicPlayContext
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.era.XyEraItem
import cn.xybbz.localdata.data.music.PlayHistoryMusic
import cn.xybbz.localdata.data.music.PlayQueueMusic
import cn.xybbz.localdata.data.player.XyPlayer
import cn.xybbz.localdata.data.progress.Progress
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import cn.xybbz.localdata.enums.PlayerTypeEnum
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.time.Year
import javax.inject.Inject


@OptIn(UnstableApi::class)
@HiltViewModel
class MainViewModel @Inject constructor(
    private val db: DatabaseClient,
    private val musicController: MusicController,
    private val _dataSourceManager: IDataSourceManager,
    private val connectionConfigServer: ConnectionConfigServer,
    private val _settingsConfig: SettingsConfig,
    private val musicPlayContext: MusicPlayContext,
    private val cacheController: CacheController,
    private val alarmConfig: AlarmConfig,
    private val apkUpdateManager: ApkUpdateManager,
    private val favoriteRepository: FavoriteRepository,
    val selectControl: SelectControl,
    private val lrcServer: LrcServer
) : ViewModel() {

    val dataSourceManager = _dataSourceManager
    val settingsConfig = _settingsConfig
    val _db = db

    /**
     * 专辑播放历史功能开启数据
     */
    private var enableProgressMap = mutableStateMapOf<String, Boolean>()


    var eraItemList by mutableStateOf<List<XyEraItem>>(emptyList())
        private set

    //从1900年到当前年份的set列表
    val yearSet by mutableStateOf(DateUtil.getYearSet())


    //下载的异步携程
    var downloadJob: Job? = null

    init {
        Log.i("=====", "MainViewModel初始化")
        //加载是否开启专辑播放历史功能数据
        getEnableProgressMapData()
        //初始化年代数据
        initEraData()
        //音乐服务初始化
        musicControllerInit()
        //初始化版本信息获取
        initGetVersionInfo()
    }

    /**
     * 初始化音乐播放控制器
     */
    private fun musicControllerInit() {
        musicController.setRemovePlaybackProgressFun {
            viewModelScope.launch {
                db.progressDao.removeByMusicId(musicId = it)
            }
        }

        musicController.setOnPlay { itemId, playSessionId ->
            //调用上报接口
            viewModelScope.launch {
                dataSourceManager.reportPlaying(
                    itemId,
                    playSessionId = playSessionId,
                    positionTicks = musicController.currentPosition
                )
            }
            viewModelScope.launch {
                dataSourceManager.reportProgress(
                    itemId,
                    playSessionId = playSessionId,
                    positionTicks = musicController.currentPosition
                )
                alarmConfig.scheduleNextReport()
            }
            /*if (!cacheController.ifCache(it.musicUrl, it.size ?: 20000)) {
                cacheController.cacheMedia(it)
            }*/
        }

        musicController.setOnPauseFun { itemId, playSessionId, musicUrl ->
            viewModelScope.launch {
                dataSourceManager.reportPlaying(
                    itemId,
                    playSessionId = playSessionId,
                    true,
                    musicController.currentPosition
                )
            }
            viewModelScope.launch {
                dataSourceManager.reportProgress(
                    itemId,
                    playSessionId = playSessionId,
                    positionTicks = musicController.currentPosition
                )
            }
            cacheController.pauseCache(musicUrl)
            Log.i("=====", "调用暂停方法")
            setPlayerProgress(musicController.currentPosition)
        }

        musicController.setOnSeekTo { millSeconds, itemId, playSessionId ->
            setPlayerProgress(millSeconds)

            viewModelScope.launch {
                dataSourceManager.reportProgress(
                    itemId,
                    playSessionId = playSessionId,
                    positionTicks = millSeconds
                )
            }
        }
        /**
         * 设置手动音频切换方法
         */
        musicController.setOnManualChangeMusic {
            setPlayerProgress(musicController.currentPosition)
        }
        /**
         * 设置收藏状态
         */
        musicController.setFavoriteMusic {
            viewModelScope.launch {
                _dataSourceManager.setFavoriteData(
                    type = MusicTypeEnum.MUSIC,
                    itemId = it,
                    musicController = musicController,
                    ifFavorite = it in favoriteRepository.favoriteSet.value
                )
            }
        }
        /**
         * 音频切换调用方法
         */
        musicController.setOnChangeMusic { itemId ->

            viewModelScope.launch {
                //数据变化的时候进行数据变化
                putIterations(0)
                db.withTransaction {
                    db.musicDao.updateByPlayedCount(
                        itemId
                    )
                    val recordMusic =
                        db.musicDao.selectById(itemId)

                    if (recordMusic != null) {
                        val index = db.musicDao.selectPlayHistoryIndexAsc() ?: -1
                        db.musicDao.savePlayHistoryMusic(
                            listOf(
                                PlayHistoryMusic(
                                    musicId = itemId,
                                    index = index - 1,
                                    connectionId = connectionConfigServer.getConnectionId()
                                )
                            )
                        )
                    }
                    val recordCount = db.musicDao.selectPlayHistoryCount()
                    if (recordCount > 20) {
                        db.musicDao.deletePlayHistory()
                    }
                    db.playerDao.updateIndex(musicController.musicInfo?.itemId ?: "")
                }
                //只缓存当前正在播放的音乐,防止过度消耗用户流量
                /*musicController.musicInfo?.let { musicInfo ->
                    if (!cacheController.ifCache(musicInfo.musicUrl, musicInfo.size ?: 20000)) {
                        cacheController.cacheMedia(musicInfo)
                    }
                }*/
            }
        }
        /**
         * 设置播放类型
         */
        musicController.setOnPlayerTypeChangeFun {
            viewModelScope.launch {
                val player =
                    db.playerDao.selectPlayerByDataSource()
                if (player != null) {
                    setPlayerType(it)
                } else {
                    savePlayerType(it)
                }
            }

        }

        /**
         * 设置音乐列表变化时调用的方法
         */
        musicController.setOnAddMusicListFun { artist ->
            viewModelScope.launch {
                var index = db.musicDao.selectPlayQueueIndex() ?: -1
                val xyMusicList = musicController.originMusicList.map {
                    index += 1
                    PlayQueueMusic(
                        musicId = it.itemId,
                        index = index,
                        connectionId = connectionConfigServer.getConnectionId()
                    )
                }
                if (xyMusicList.isNotEmpty()) {

                    //先删除数据
                    db.musicDao.removeByType(dataType = MusicDataTypeEnum.PLAY_QUEUE)
                    //存储音乐数据
                    db.musicDao.savePlayQueueMusic(xyMusicList)

                    //存储player设置
                    val player =
                        db.playerDao.selectPlayerByDataSource()
                    val xyPlayer = XyPlayer(
                        connectionId = connectionConfigServer.getConnectionId(),
                        dataType = musicController.playDataType,
                        musicId = musicController.musicInfo?.itemId ?: "",
                        headTime = musicController.headTime,
                        endTime = musicController.endTime,
                        playerType = musicController.playType,
                        pageNum = musicController.pageNum,
                        ifSkip = musicController.headTime > 0,
                        albumId = musicController.musicInfo?.album ?: "",
                        artistId = artist,
                        pageSize = musicController.pageSize
                    )
                    if (player != null) {
                        db.playerDao.updateById(
                            xyPlayer.copy(
                                id = player.id,
                                artistId = player.artistId
                            )
                        )
                    } else {
                        db.playerDao.save(
                            xyPlayer
                        )
                    }
                }
            }
        }

        /**
         * 应用启动,加载播放列表
         */
        musicController.initController {
            // 查询是否存在播放列表,如果存在将内容写入
            viewModelScope.launch {
                loadPlayerList()
            }
        }
    }

    /**
     * 获得专辑是否开启播放历史数据
     */
    private fun getEnableProgressMapData() {
        viewModelScope.launch {
            connectionConfigServer.loginStateFlow.collect { bool ->
                if (bool) {
                    db.enableProgressDao.getAlbumEnableProgressMap().distinctUntilChanged()
                        .collect {
                            if (it.isNotEmpty()) {
                                enableProgressMap.clear()
                                enableProgressMap.putAll(it)
                            }
                        }
                }
            }

        }
    }

    /**
     * 加载播放列表里的数据
     */
    private fun loadPlayerList() {
        viewModelScope.launch {

            connectionConfigServer.loginStateFlow.collect {
                if (it) {
                    val musicList = db.musicDao.selectPlayQueuePlayMusicList()
                    if (dataSourceManager.dataSourceType != null) {
                        val player =
                            db.playerDao.selectPlayerByDataSource()
                        if (musicList.isNotEmpty()) {
                            viewModelScope.launch {
                                musicPlayContext.initPlayList(
                                    musicList = musicList,
                                    player = player
                                )
                                musicController.pause()
                            }
                        }
                    }
                }
            }
        }

    }

    //设置播放进度
    private fun setPlayerProgress(progress: Long) {
        viewModelScope.launch {
            musicController.musicInfo?.let {
                //判断是否需要存储播放历史
                if (_settingsConfig.get().ifEnableAlbumHistory || (enableProgressMap.containsKey(
                        it.album
                    ) && enableProgressMap[it.album] == true)
                ) {
                    savePlayerProgress(
                        progress = progress,
                        progressPercentage = if (musicController.duration > 0) {
                            BigDecimal(progress).divide(
                                BigDecimal(musicController.duration),
                                2,
                                MathContext.ROUND_HALF_EVEN
                            ).multiply(BigDecimal(100)).intValueExact()
                        } else {
                            0
                        },
                        musicId = it.itemId,
                        musicName = it.name,
                        index = musicController.curOriginIndex,
                        albumId = it.album
                    )
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
            PlayerTypeData(icon = Icons.Rounded.RepeatOne, message = R.string.single_loop),
            PlayerTypeData(icon = Icons.Rounded.Repeat, message = R.string.list_loop),
            PlayerTypeData(icon = Icons.Rounded.Shuffle, message = R.string.shuffle_play),
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
            val year = Year.now().value
            val era = (year / 10) * 10

            val eraItemList = db.eraItemDao.selectList()
            if (eraItemList.isEmpty()) {
                val eraList = mutableListOf<XyEraItem>()
                //生成数据
                val num = (era - eraStart) / 10
                for (index in 0..num) {
                    val years = mutableListOf<String>()
                    val thisEra = eraStart + index * 10

                    for (i in 0 until 10) {
                        years.add((thisEra + i).toString())
                    }
                    eraList.add(
                        XyEraItem(
                            title = "$thisEra",
                            era = thisEra,
                            years = years.joinToString(Constants.ARTIST_DELIMITER)
                        )
                    )

                }
                db.eraItemDao.saveBatch(eraList)
                Log.i("=====", "当前的年代: $era")
            } else {
                val earItem = db.eraItemDao.selectOneByEra(era)
                if (earItem != null) {
                    val years = earItem.years.split(',')
                    val yearsNew = mutableListOf<String>()
                    if (!years.contains(year.toString())) {
                        for (i in 0 until 10) {
                            yearsNew.add((era + i).toString())
                        }
                        db.eraItemDao.updateById(
                            earItem.copy(
                                years = years.joinToString(
                                    Constants.ARTIST_DELIMITER
                                )
                            )
                        )
                    }

                } else {
                    val years = mutableListOf<String>()
                    for (i in 0 until 10) {
                        years.add((era + i).toString())
                    }
                    db.eraItemDao.saveBatch(
                        XyEraItem(
                            title = "${era}年代",
                            era = era,
                            years = years.joinToString(Constants.ARTIST_DELIMITER),
                        )
                    )

                }
            }
            getEraList()
        }
    }


    /**
     * 存储播放类型
     */
    private suspend fun setPlayerType(playerTypeEnum: PlayerTypeEnum) {
        db.playerDao.updatePlayType(playerTypeEnum)
    }


    /**
     * 新增播放类型
     */
    private suspend fun savePlayerType(playerTypeEnum: PlayerTypeEnum) {
        db.playerDao.save(
            XyPlayer(
                playerType = playerTypeEnum,
                connectionId = connectionConfigServer.getConnectionId(),
                dataType = musicController.playDataType,
                pageSize = 0
            )
        )
    }

    /**
     * 增加播放历史进度
     */
    private suspend fun savePlayerProgress(
        progress: Long,
        progressPercentage: Int,
        musicId: String,
        musicName: String,
        index: Int,
        albumId: String?,
    ) {
        db.progressDao.save(
            Progress(
                musicId = musicId,
                albumId = albumId ?: "",
                progress = progress,
                progressPercentage = progressPercentage,
                index = index,
                musicName = musicName,
                connectionId = connectionConfigServer.getConnectionId()
            )
        )
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
        viewModelScope.launch {
            apkUpdateManager.initLatestVersion()
        }
    }

    /**
     * 清空分页信息
     */

    fun clearRemoteCurrent() {
        viewModelScope.launch {
            db.remoteCurrentDao.removeAll()
        }
    }
}