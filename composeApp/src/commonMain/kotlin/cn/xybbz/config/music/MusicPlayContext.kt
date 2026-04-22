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

package cn.xybbz.config.music

import androidx.compose.runtime.Immutable
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.utils.Log
import cn.xybbz.common.utils.MessageUtils
import cn.xybbz.common.utils.MusicListIndexUtils
import cn.xybbz.config.scope.IoScoped
import cn.xybbz.entity.data.music.OnMusicPlayParameter
import cn.xybbz.localdata.config.LocalDatabaseClient
import cn.xybbz.localdata.data.music.XyPlayMusic
import cn.xybbz.localdata.data.player.XyPlayer
import cn.xybbz.localdata.data.progress.Progress
import cn.xybbz.localdata.data.setting.SkipTime
import cn.xybbz.localdata.enums.MusicPlayTypeEnum
import cn.xybbz.localdata.enums.PlayerModeEnum
import cn.xybbz.ui.components.LoadingObject
import cn.xybbz.ui.components.dismiss
import cn.xybbz.ui.components.show
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.get_music_failed_cannot_play

/**
 * 一次播放请求所需的上下文数据。
 */
@Immutable
data class MusicPlayData(
    // 播放请求参数
    var onMusicPlayParameter: OnMusicPlayParameter,
    // 首次加载的歌曲列表提供器
    var xyMusicList: (suspend () -> List<XyPlayMusic>)? = { emptyList() },
    // 下一页歌曲列表提供器
    var onNextMusicList: (suspend (Int) -> List<XyPlayMusic>?)? = null,
    // 当前请求的分页大小
    var pageSize: Int = Constants.MIN_PAGE
)

/**
 * 音乐播放请求上下文
 */
class MusicPlayContext(
    private val dataSourceManager: DataSourceManager,
    private val musicController: MusicCommonController,
    private val db: LocalDatabaseClient
) : IoScoped() {

    /**
     * 是否在加载下一页数据
     */
    private val _ifNextPageNumListFlow = MutableStateFlow(false)
    val ifNextPageNumListFlow = _ifNextPageNumListFlow.asStateFlow()

    // 当前播放流程使用的请求上下文
    var musicPlayData: MusicPlayData? = null

    init {
        createScope()
    }

    /**
     * 更新“是否正在加载下一页”的状态。
     */
    fun updateIfNextPageNumList(isLoading: Boolean) {
        _ifNextPageNumListFlow.value = isLoading
    }

    // todo 增加排序类型存储，并且根据存储的排序类型存储加载列表和加载下一页

    /**
     * 播放音乐列表
     */
    fun musicList(
        onMusicPlayParameter: OnMusicPlayParameter,
        musicList: List<XyPlayMusic>,
        playerModeEnum: PlayerModeEnum? = null
    ) {

        if (playerModeEnum != null)
            musicController.setPlayTypeData(playerModeEnum)
        musicOperate(
            musicPlayData = MusicPlayData(
                onMusicPlayParameter = onMusicPlayParameter,
                xyMusicList = { musicList }),
            ifSkip = false,
            coroutineScope = scope,
            musicPlayTypeEnum = MusicPlayTypeEnum.RECORD
        )
    }

    /**
     * 音乐播放
     */
    fun music(
        onMusicPlayParameter: OnMusicPlayParameter,
        index: Int,
        type: MusicPlayTypeEnum
    ) {
        val musicPlayContext = MusicPlayData(
            onMusicPlayParameter = onMusicPlayParameter,
            xyMusicList = {
                val pageNum =
                    MusicListIndexUtils.getPageNum(index, Constants.UI_LIST_PAGE)

                val loadingObject = LoadingObject()
                loadingObject.show()
                //数据库中获取
                val musicList = dataSourceManager.getMusicList(
                    pageNum = pageNum,
                    pageSize = Constants.UI_LIST_PAGE
                ) ?: listOf()

                loadingObject.dismiss()
                musicList
            },
            onNextMusicList = { thiIndex ->
                dataSourceManager.getMusicList(
                    pageSize = Constants.UI_LIST_PAGE,
                    pageNum = thiIndex
                )
            }
        )
        musicOperate(
            musicPlayData = musicPlayContext,
            ifSkip = false,
            coroutineScope = scope,
            musicPlayTypeEnum = type
        )
    }

    /**
     * 专辑对象
     */
    fun album(onMusicPlayParameter: OnMusicPlayParameter) {
        val musicPlayData = MusicPlayData(
            onMusicPlayParameter = onMusicPlayParameter,
            xyMusicList = {
                val loadingObject = LoadingObject()
                loadingObject.show()
                val musicList = if (onMusicPlayParameter.albumId != null) {
                    val progress =
                        db.progressDao.selectByAlbumIdOne(onMusicPlayParameter.albumId)
                    if (progress != null)
                        onMusicPlayParameter.musicId = progress.musicId
                    val pageNum = MusicListIndexUtils.getPageNum(
                        progress?.index ?: 0,
                        Constants.ALBUM_MUSIC_LIST_PAGE
                    )
                    dataSourceManager.getMusicListByAlbumId(
                        albumId = onMusicPlayParameter.albumId,
                        pageSize = Constants.ALBUM_MUSIC_LIST_PAGE,
                        pageNum = pageNum
                    )
                } else {
                    listOf()
                }
                loadingObject.dismiss()
                musicList ?: listOf()
            },
            onNextMusicList = { thiIndex ->
                if (onMusicPlayParameter.albumId != null)
                    dataSourceManager.getMusicListByAlbumId(
                        pageSize = Constants.ALBUM_MUSIC_LIST_PAGE,
                        pageNum = thiIndex,
                        albumId = onMusicPlayParameter.albumId,
                    )
                else
                    null
            },
            pageSize = Constants.ALBUM_MUSIC_LIST_PAGE
        )
        musicOperate(
            musicPlayData = musicPlayData,
            ifSkip = false,
            coroutineScope = scope,
            musicPlayTypeEnum = MusicPlayTypeEnum.ALBUM
        )
    }


    /**
     * 艺术家
     */
    fun artist(
        onMusicPlayParameter: OnMusicPlayParameter,
        index: Int,
        artistId: String
    ) {
        val musicPlayData = MusicPlayData(
            onMusicPlayParameter = onMusicPlayParameter,
            xyMusicList = {

                val loadingObject = LoadingObject()
                loadingObject.show()
                val pageNum = MusicListIndexUtils.getPageNum(
                    index,
                    Constants.UI_LIST_PAGE
                )
                val musicList = dataSourceManager.getMusicListByArtistId(
                    artistId = artistId,
                    pageNum = pageNum,
                    pageSize = Constants.UI_LIST_PAGE,
                ) ?: listOf()
                loadingObject.dismiss()
                musicList
            },
            onNextMusicList = { thiIndex ->
                dataSourceManager.getMusicListByArtistId(
                    pageSize = Constants.UI_LIST_PAGE,
                    pageNum = thiIndex,
                    artistId = artistId,
                )
            })
        musicOperate(
            musicPlayData = musicPlayData,
            ifSkip = false,
            coroutineScope = scope,
            musicPlayTypeEnum = MusicPlayTypeEnum.ARTIST
        )
    }


    /**
     * 收藏
     */
    fun favorite(onMusicPlayParameter: OnMusicPlayParameter, index: Int) {
        val musicPlayData = MusicPlayData(
            onMusicPlayParameter = onMusicPlayParameter,
            xyMusicList = {
                val loadingObject = LoadingObject()
                loadingObject.show()
                val pageNum = MusicListIndexUtils.getPageNum(
                    index,
                    Constants.UI_LIST_PAGE
                )
                val musicList = dataSourceManager.getMusicListByFavorite(
                    pageSize = Constants.UI_LIST_PAGE,
                    pageNum = pageNum,
                ) ?: listOf()
                loadingObject.dismiss()
                musicList
            },
            onNextMusicList = { pageNum ->
                dataSourceManager.getMusicListByFavorite(
                    pageSize = Constants.UI_LIST_PAGE,
                    pageNum = pageNum
                )
            })
        musicOperate(
            musicPlayData = musicPlayData,
            ifSkip = false,
            coroutineScope = scope,
            musicPlayTypeEnum = MusicPlayTypeEnum.FAVORITE
        )
    }

    /**
     * 随机播放音乐
     */
    fun randomMusic(onMusicPlayParameter: OnMusicPlayParameter) {
        val musicPlayData = MusicPlayData(
            onMusicPlayParameter = onMusicPlayParameter,
            xyMusicList = {
                val loadingObject = LoadingObject()
                loadingObject.show()
                val musicList = dataSourceManager.getRandomMusicExtendList(
                    pageNum = 0,
                    pageSize = Constants.UI_LIST_PAGE,
                ) ?: listOf()
                loadingObject.dismiss()
                musicList
            },
            onNextMusicList = { pageNum ->
                dataSourceManager.getRandomMusicExtendList(
                    pageNum = pageNum,
                    pageSize = Constants.UI_LIST_PAGE,
                )
            },
            pageSize = Constants.UI_LIST_PAGE
        )
        musicOperate(
            musicPlayData = musicPlayData,
            ifSkip = false,
            coroutineScope = scope,
            musicPlayTypeEnum = MusicPlayTypeEnum.RANDOM
        )
    }


    /**
     * 根据上次播放类型进行添加播放
     */
    fun initPlayList(
        musicList: List<XyPlayMusic>?,
        player: XyPlayer?
    ) {
        if (player != null) {
            val onMusicPlayParameter = OnMusicPlayParameter(
                musicId = player.musicId,
                albumId = player.albumId,
                artistId = player.artistId
            )
            val musicPlayData = MusicPlayData(
                onMusicPlayParameter = onMusicPlayParameter,
                xyMusicList = {
                    musicList ?: listOf()
                },
                onNextMusicList = { pageNum ->
                    if (player.dataType == MusicPlayTypeEnum.RECORD) {
                        null
                    } else if (player.dataType == MusicPlayTypeEnum.ALBUM) {
                        onMusicPlayParameter.albumId?.let {
                            dataSourceManager.getMusicListByAlbumId(
                                onMusicPlayParameter.albumId,
                                pageSize = player.pageSize,
                                pageNum = pageNum
                            )
                        }

                    } else if (player.dataType == MusicPlayTypeEnum.ARTIST) {
                        player.artistId?.let {
                            dataSourceManager.getMusicListByArtistId(
                                it,
                                pageSize = player.pageSize,
                                pageNum = pageNum
                            )
                        }

                    } else if (player.dataType == MusicPlayTypeEnum.FAVORITE) {
                        dataSourceManager.getMusicListByFavorite(
                            pageSize = player.pageSize,
                            pageNum = pageNum
                        )
                    } else if (player.dataType == MusicPlayTypeEnum.RANDOM) {
                        dataSourceManager.getRandomMusicExtendList(
                            pageSize = player.pageSize,
                            pageNum = pageNum
                        )
                    } else {
                        null
                    }
                },
                pageSize = player.pageSize
            )
            player.playerType.let {
                Log.i("=====", "当前的播放模式是${it}")
                musicController.setPlayTypeData(it)
            }
            musicOperate(
                musicPlayData = musicPlayData,
                ifSkip = player.ifSkip,
                pageNum = player.pageNum,
                ifInitPlayerList = true,
                coroutineScope = scope,
                musicPlayTypeEnum = player.dataType
            )


        }


    }

    /**
     * 音乐播放地址更新
     */
    fun changeMusicPlaylist(){
        musicController.replacePlaylistItemUrl{
            val andMusicUrlData =
                dataSourceManager.getMusicPlayUrl(it.itemId, it.plexPlayKey)
            it.copy(
                ifHls = andMusicUrlData.ifHls,
                musicUrl = andMusicUrlData.musicUrl,
                static = andMusicUrlData.static,
                audioBitRate = andMusicUrlData.audioBitRate,
            )
        }
    }

    /**
     * 音乐操作
     * @param [coroutineScope] 协程作用域
     * @param [ifSkip] 如果跳过头尾
     */
    fun musicOperate(
        musicPlayData: MusicPlayData,
        ifSkip: Boolean,
        pageNum: Int? = null,
        ifInitPlayerList: Boolean = false,
        coroutineScope: CoroutineScope,
        musicPlayTypeEnum: MusicPlayTypeEnum
    ) {
        this.musicPlayData = musicPlayData
        coroutineScope.launch {
            //2024年4月17日 10:54:36 albumId 可能为null/空 下面方法未判断
            val tmpMusicList = mutableListOf<XyPlayMusic>()
            var tmpIndex: Int? = null
            val xyMusicList = musicPlayData.xyMusicList?.invoke()
            if (xyMusicList?.isNotEmpty() == true) {
                if (musicPlayData.onMusicPlayParameter.musicId.isNotBlank())
                    tmpIndex = xyMusicList
                        .indexOfFirst { item -> item.itemId == musicPlayData.onMusicPlayParameter.musicId }
                tmpMusicList.addAll(xyMusicList.map {
                    val andMusicUrlData =
                        dataSourceManager.getMusicPlayUrl(it.itemId, it.plexPlayKey)
                    it.copy(
                        ifHls = andMusicUrlData.ifHls,
                        musicUrl = andMusicUrlData.musicUrl,
                        static = andMusicUrlData.static,
                        audioBitRate = andMusicUrlData.audioBitRate,
                    )
                })

            } else {
                MessageUtils.sendPopTipError("请选择要播放的歌曲")
                return@launch
            }
            //解决专辑点击播放未从接口获取问题,还有专辑未打开的时候默认是播放历史位置
            //需要解决下一页的问题
            try {

                if (tmpMusicList.isNotEmpty()) {
                    var progressMap: Map<String, Long>? = null
                    //判断是否需要配置跳过
                    if (ifSkip) {
                        //要播放的音乐如果存在不是有声小说的,则不设置跳过
                        musicPlayData.onMusicPlayParameter.albumId?.let { albumId ->
                            if (albumId.isNotBlank()) {
                                val skipTime =
                                    db.skipTimeDao.selectByAlbumId(albumId)
                                        ?: SkipTime(connectionId = 0)
                                val progressList =
                                    db.progressDao.selectByMusicIds(tmpMusicList.map { it.itemId })
                                progressMap =
                                    progressList.associateBy(Progress::musicId) { musicInfo -> musicInfo.progress }
                                //在这里加载配置数据
                                musicController.setHeadAndEntTime(
                                    headTime = skipTime.headTime,
                                    endTime = skipTime.endTime
                                )
                            }
                        }
                    }

                    try {
                        musicController.initMusicList(
                            musicDataList = tmpMusicList,
                            musicCurrentPositionMapData = progressMap ?: emptyMap(),
                            originIndex = tmpIndex,
                            pageNum = pageNum ?: 0,
                            pageSize = musicPlayData.pageSize,
                            artistId = musicPlayData.onMusicPlayParameter.artistId,
                            ifInitPlayerList = ifInitPlayerList,
                            musicPlayTypeEnum = musicPlayTypeEnum
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    MessageUtils.sendPopTip(Res.string.get_music_failed_cannot_play)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

