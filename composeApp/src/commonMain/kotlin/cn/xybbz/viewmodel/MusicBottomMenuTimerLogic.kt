package cn.xybbz.viewmodel

import cn.xybbz.common.enums.PlayStateEnum
import cn.xybbz.config.music.PlayerEvent

/** 定时关闭等待期间使用的自然结束事件信号。 */
internal data class TrackEndSignal(
    /** 播放器自然结束事件的递增序号。 */
    val sequence: Long,
    /** 自然结束曲目的唯一标识。 */
    val musicId: String
)

/** 定时关闭等待期间的一次停止候选状态。 */
internal data class MusicTimerStopCandidate(
    /** 候选状态对应的当前曲目标识。 */
    val currentMusicId: String?,
    /** 候选状态对应的播放器状态。 */
    val playbackState: PlayStateEnum,
    /** 候选状态携带的播放器事件。 */
    val playerEvent: PlayerEvent? = null
)

/** 定时关闭等待结束的原因。 */
internal enum class MusicTimerStopReason {
    /** 当前等待曲目自然结束。 */
    TrackEnded,

    /** 当前播放曲目已被切换。 */
    ItemChanged,

    /** 播放器已经暂停或进入空闲状态。 */
    PlaybackStopped
}

/** 判断自然结束事件是否属于当前定时关闭等待周期。 */
internal fun isNewMatchingTrackEndSignal(
    signalSequence: Long,
    baselineSequence: Long,
    waitingMusicId: String,
    endedMusicId: String
): Boolean {
    return signalSequence > baselineSequence && endedMusicId == waitingMusicId
}

/** 根据播放器事件和状态确定定时关闭等待是否应当结束。 */
internal fun resolveMusicTimerStopReason(
    waitingMusicId: String,
    currentMusicId: String?,
    playbackState: PlayStateEnum,
    playerEvent: PlayerEvent? = null
): MusicTimerStopReason? {
    if (playerEvent is PlayerEvent.RemovePlaybackProgress &&
        playerEvent.musicId == waitingMusicId
    ) {
        return MusicTimerStopReason.TrackEnded
    }
    if (currentMusicId != waitingMusicId) {
        return MusicTimerStopReason.ItemChanged
    }
    if (playbackState == PlayStateEnum.Pause || playbackState == PlayStateEnum.None) {
        return MusicTimerStopReason.PlaybackStopped
    }
    return null
}
