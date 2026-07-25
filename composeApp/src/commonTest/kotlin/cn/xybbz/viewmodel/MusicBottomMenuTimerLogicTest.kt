package cn.xybbz.viewmodel

import cn.xybbz.common.enums.PlayStateEnum
import cn.xybbz.config.music.PlayerEvent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** 定时关闭等待逻辑的回归测试。 */
class MusicBottomMenuTimerLogicTest {

    /** 进度回退且没有自然结束事件时不应构成停止原因。 */
    @Test
    fun backwardSeekDoesNotStopWaiting() {
        assertNull(
            resolveMusicTimerStopReason(
                waitingMusicId = "song-a",
                currentMusicId = "song-a",
                playbackState = PlayStateEnum.Playing
            )
        )
    }

    /** 手动 seek 事件不应构成曲目自然结束。 */
    @Test
    fun manualSeekDoesNotStopWaiting() {
        assertNull(
            resolveMusicTimerStopReason(
                waitingMusicId = "song-a",
                currentMusicId = "song-a",
                playbackState = PlayStateEnum.Playing,
                playerEvent = PlayerEvent.PositionSeekTo(
                    positionMs = 20_000L,
                    musicId = "song-a"
                )
            )
        )
    }

    /** 当前曲目的自然结束事件应触发停止。 */
    @Test
    fun matchingTrackEndStopsWaiting() {
        assertEquals(
            MusicTimerStopReason.TrackEnded,
            resolveMusicTimerStopReason(
                waitingMusicId = "song-a",
                currentMusicId = "song-a",
                playbackState = PlayStateEnum.Playing,
                playerEvent = PlayerEvent.RemovePlaybackProgress("song-a")
            )
        )
    }

    /** 其他曲目的自然结束事件不应影响当前等待曲目。 */
    @Test
    fun differentTrackEndDoesNotStopWaiting() {
        assertNull(
            resolveMusicTimerStopReason(
                waitingMusicId = "song-a",
                currentMusicId = "song-a",
                playbackState = PlayStateEnum.Playing,
                playerEvent = PlayerEvent.RemovePlaybackProgress("song-b")
            )
        )
    }

    /** 当前曲目发生切换时应保持原有停止行为。 */
    @Test
    fun changedTrackStopsWaiting() {
        assertEquals(
            MusicTimerStopReason.ItemChanged,
            resolveMusicTimerStopReason(
                waitingMusicId = "song-a",
                currentMusicId = "song-b",
                playbackState = PlayStateEnum.Playing
            )
        )
    }

    /** 暂停和空闲状态应保持原有停止行为。 */
    @Test
    fun pausedOrIdlePlaybackStopsWaiting() {
        assertEquals(
            MusicTimerStopReason.PlaybackStopped,
            resolveMusicTimerStopReason(
                waitingMusicId = "song-a",
                currentMusicId = "song-a",
                playbackState = PlayStateEnum.Pause
            )
        )
        assertEquals(
            MusicTimerStopReason.PlaybackStopped,
            resolveMusicTimerStopReason(
                waitingMusicId = "song-a",
                currentMusicId = "song-a",
                playbackState = PlayStateEnum.None
            )
        )
    }

    /** 旧结束事件不能触发新的定时关闭等待。 */
    @Test
    fun oldTrackEndSignalIsIgnored() {
        assertFalse(
            isNewMatchingTrackEndSignal(
                signalSequence = 3L,
                baselineSequence = 3L,
                waitingMusicId = "song-a",
                endedMusicId = "song-a"
            )
        )
        assertTrue(
            isNewMatchingTrackEndSignal(
                signalSequence = 4L,
                baselineSequence = 3L,
                waitingMusicId = "song-a",
                endedMusicId = "song-a"
            )
        )
        assertFalse(
            isNewMatchingTrackEndSignal(
                signalSequence = 4L,
                baselineSequence = 3L,
                waitingMusicId = "song-a",
                endedMusicId = "song-b"
            )
        )
    }
}
