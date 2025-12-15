package cn.xybbz.entity.data.music

import android.util.Log
import android.webkit.URLUtil
import androidx.annotation.OptIn
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.media3.common.util.UnstableApi
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.music.MusicController
import cn.xybbz.common.utils.CoroutineScopeUtils
import cn.xybbz.common.utils.MessageUtils
import cn.xybbz.common.utils.MusicListIndexUtils
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.music.XyPlayMusic
import cn.xybbz.localdata.data.player.XyPlayer
import cn.xybbz.localdata.data.progress.Progress
import cn.xybbz.localdata.data.setting.SkipTime
import cn.xybbz.localdata.enums.MusicPlayTypeEnum
import cn.xybbz.localdata.enums.PlayerTypeEnum
import cn.xybbz.ui.components.LoadingObject
import cn.xybbz.ui.components.dismiss
import cn.xybbz.ui.components.show
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@Immutable
data class MusicPlayData(
    var onMusicPlayParameter: OnMusicPlayParameter,
    var xyMusicList: (suspend () -> List<XyPlayMusic>)? = { emptyList() },
    var onNextMusicList: (suspend (Int) -> List<XyPlayMusic>?)? = null,
    var pageSize: Int = Constants.MIN_PAGE
)

/**
 * 是否在加载下一页数据
 */
var ifNextPageNumList by mutableStateOf(false)
//todo 增加排序类型存储,并且根据存储的排序类型存储加载列表和加载下一页
/**
 * 音乐播放请求上下文
 */
class MusicPlayContext @Inject constructor(
    private val dataSourceManager: DataSourceManager,
    private val musicController: MusicController,
    private val db: DatabaseClient
) {

    private val playCoroutineScope = CoroutineScopeUtils.getMain(this.javaClass.name)


    /**
     * 播放音乐列表
     */
    fun musicList(
        onMusicPlayParameter: OnMusicPlayParameter,
        musicList: List<XyPlayMusic>,
        playerTypeEnum: PlayerTypeEnum? = null
    ) {

        if (playerTypeEnum != null)
            musicController.setPlayTypeData(playerTypeEnum)
        musicOperate(
            musicPlayData = MusicPlayData(
                onMusicPlayParameter = onMusicPlayParameter,
                xyMusicList = { musicList }),
            ifSkip = false,
            coroutineScope = playCoroutineScope,
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

                val loadingObject = LoadingObject(id = UUID.randomUUID().toString())
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
            coroutineScope = playCoroutineScope,
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
                val loadingObject = LoadingObject(id = UUID.randomUUID().toString())
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
            coroutineScope = playCoroutineScope,
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

                val loadingObject = LoadingObject(id = UUID.randomUUID().toString())
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
            coroutineScope = playCoroutineScope,
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
                val loadingObject = LoadingObject(id = UUID.randomUUID().toString())
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
            coroutineScope = playCoroutineScope,
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
                val loadingObject = LoadingObject(id = UUID.randomUUID().toString())
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
            coroutineScope = playCoroutineScope,
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
            musicOperate(
                musicPlayData = musicPlayData,
                ifSkip = player.ifSkip,
                pageNum = player.pageNum,
                ifInitPlayerList = true,
                coroutineScope = playCoroutineScope,
                musicPlayTypeEnum = player.dataType
            )

            player.playerType.let {
                Log.i("=====", "当前的播放模式是${it}")
                musicController.setPlayTypeData(it)
            }
        }


    }

    /**
     * 音乐操作
     * @param [coroutineScope] 协程作用域
     * @param [ifSkip] 如果跳过头尾
     */
    @OptIn(UnstableApi::class)
    fun musicOperate(
        musicPlayData: MusicPlayData,
        ifSkip: Boolean,
        pageNum: Int? = null,
        ifInitPlayerList: Boolean = false,
        coroutineScope: CoroutineScope,
        musicPlayTypeEnum: MusicPlayTypeEnum
    ) {
        coroutineScope.launch {
            //2024年4月17日 10:54:36 albumId 可能为null/空 下面方法未判断
            val tmpMusicList = mutableListOf<XyPlayMusic>()
            var tmpIndex: Int? = null
            val xyMusicList = musicPlayData.xyMusicList?.invoke()
            if (xyMusicList?.isNotEmpty() == true) {
                if (musicPlayData.onMusicPlayParameter.musicId.isNotBlank())
                    tmpIndex = xyMusicList
                        .indexOfFirst { item -> item.itemId == musicPlayData.onMusicPlayParameter.musicId }
                tmpMusicList.addAll(xyMusicList)
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
                        val tmp = mutableListOf<String>()
                        tmpMusicList.forEach {

                            if (URLUtil.isNetworkUrl(it.musicUrl)) {
                                tmp.add(it.musicUrl)
                            }
                        }

                        musicController.setOnNextListFun { _ ->
                            ifNextPageNumList = true
                            coroutineScope.launch {
                                val newPageNum = musicController.pageNum + 1
                                val newMusicList = musicPlayData.onNextMusicList?.invoke(newPageNum)
                                if (!newMusicList.isNullOrEmpty()) {
                                    musicController.setPageNumData(newPageNum)
                                    if (newMusicList.isNotEmpty())
                                        musicController.addMusicList(
                                            newMusicList,
                                            musicPlayData.onMusicPlayParameter.artistId
                                        )
                                }
                            }.invokeOnCompletion { ifNextPageNumList = false }
                        }
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
                    MessageUtils.sendPopTip("获取音乐失败,无法播放")
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
