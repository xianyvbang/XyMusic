package cn.xybbz.music

import cn.xybbz.config.music.MusicCommonController
import cn.xybbz.config.music.PlayerEvent
import cn.xybbz.localdata.data.music.XyPlayMusic
import cn.xybbz.localdata.enums.MusicPlayTypeEnum
import cn.xybbz.localdata.enums.PlayerTypeEnum
import uk.co.caprica.vlcj.player.component.AudioPlayerComponent

class JvmMusicController : MusicCommonController() {

    private var mediaPlayerComponent: AudioPlayerComponent = AudioPlayerComponent()

    /**
     * 初始化播放
     */
    override fun initController(onRestorePlaylists: (() -> Unit)?) {
        mediaPlayerComponent.mediaPlayer().events().addMediaPlayerEventListener()
    }

    override fun addMusicList(
        musicList: List<XyPlayMusic>,
        artistId: String?,
        isPlayer: Boolean?
    ) {

        var nowIndex = 0
        val tmpList = mutableListOf<XyPlayMusic>()
        if (originMusicList.isNotEmpty()) {
            nowIndex = curOriginIndex + 1
            val tmpList = mutableListOf<XyPlayMusic>()
            tmpList.addAll(originMusicList)
            tmpList.addAll(nowIndex, musicList)
        } else {
            tmpList.addAll(originMusicList)
            tmpList.addAll(musicList)
        }
        updateOriginMusicList(tmpList)

        val mediaItemList = musicList.map { item -> musicSetMediaItem(item) }
        addMediaItems(nowIndex, mediaItemList)
        if (isPlayer != null && isPlayer) {
            mediaController?.let { media ->
                seekToIndex(media.nextMediaItemIndex)
            }
        }
        updateEvent(PlayerEvent.AddMusicList(artistId))

//        mediaPlayerComponent.mediaPlayer().media().addSlave()
    }

    override fun updateCurrentFavorite(isFavorite: Boolean) {
    }

    override fun clearPlayerList() {
    }

    override fun pause() {
    }

    /**
     * 恢复当前播放
     */
    override fun resume() {
        TODO("Not yet implemented")
    }

    /**
     * 跳转播放到指定位置
     */
    override fun seekTo(millSeconds: Long) {
        TODO("Not yet implemented")
    }

    /**
     * 获取当前播放模式下的下一首歌曲
     */
    override fun seekToNext() {
        TODO("Not yet implemented")
    }

    /**
     * 获取当前播放模式下的上一首歌曲
     */
    override fun seekToPrevious() {
        TODO("Not yet implemented")
    }

    /**
     * 跳转至指定index位置音乐
     */
    override fun seekToIndex(index: Int) {
        TODO("Not yet implemented")
    }

    /**
     * 根据音乐id跳转
     */
    override fun seekToIndex(itemId: String) {
        TODO("Not yet implemented")
    }

    /**
     * 删除指定index位置音乐
     */
    override fun removeItem(index: Int) {
        TODO("Not yet implemented")
    }

    override fun setDoubleSpeed(value: Float) {
    }

    /**
     * 设置播放类型
     */
    override fun setPlayTypeData(playerTypeEnum: PlayerTypeEnum) {
        TODO("Not yet implemented")
    }

    /**
     * 添加下一首播放功能
     */
    override fun addNextPlayer(music: XyPlayMusic) {
        TODO("Not yet implemented")
    }

    /**
     * 获取下一首播放位置的索引
     */
    override fun getNextPlayableIndex(): Int? {
        TODO("Not yet implemented")
    }

    /**
     * 获取上一首播放位置的索引
     */
    override fun getPreviousPlayableIndex(): Int? {
        TODO("Not yet implemented")
    }

    /**
     * 替换音乐播放连接
     */
    override fun replacePlaylistItemUrl() {
        TODO("Not yet implemented")
    }

    /**
     * 设置当前音乐列表
     */
    override fun initMusicList(
        musicDataList: List<XyPlayMusic>,
        musicCurrentPositionMapData: Map<String, Long>?,
        originIndex: Int?,
        pageNum: Int,
        pageSize: Int,
        artistId: String?,
        ifInitPlayerList: Boolean,
        musicPlayTypeEnum: MusicPlayTypeEnum
    ) {
        TODO("Not yet implemented")
    }

    override fun refreshPlaylistCoverMetadata() {
        TODO("Not yet implemented")
    }
}
