package cn.xybbz.common.music

import cn.xybbz.localdata.enums.PlayerTypeEnum

sealed class PlayerEvent {
    //设置删除播放历史进度
    data class RemovePlaybackProgress(val musicId: String) : PlayerEvent()
    //暂停
    data class Pause(val musicId: String, val playSessionId: String, val musicUrl: String) : PlayerEvent()
    //播放
    data class Play(val musicId: String, val playSessionId: String) : PlayerEvent()
    //进度跳转
    data class PositionSeekTo(val positionMs: Long, val musicId: String, val playSessionId: String) : PlayerEvent()
    //手动切换音频之前
    object BeforeChangeMusic : PlayerEvent()
    //音频切换
    data class ChangeMusic(val musicId: String) : PlayerEvent()
    //收藏/取消收藏
    data class Favorite(val musicId: String) : PlayerEvent()
    //加载下一页数据,参数是页码
    data class NextList(val page: Int) : PlayerEvent()
    //播放列表增加数据,传值是艺术家id
    data class AddMusicList(val artistId: String?) : PlayerEvent()
    //设置播放模式变化监听方法
    data class PlayerTypeChange(val playerType: PlayerTypeEnum) : PlayerEvent()
    //更新音乐的图片字节信息方法
    data class UpdateMusicPicData(val musicId: String?, val data: ByteArray?) : PlayerEvent() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as UpdateMusicPicData

            if (musicId != other.musicId) return false
            if (!data.contentEquals(other.data)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = musicId?.hashCode() ?: 0
            result = 31 * result + (data?.contentHashCode() ?: 0)
            return result
        }
    }
}
