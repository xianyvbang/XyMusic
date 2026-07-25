package cn.xybbz.config.music

import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.enums.PlayStateEnum
import cn.xybbz.common.utils.Log
import cn.xybbz.config.scope.IoScoped
import cn.xybbz.config.setting.SettingsManager
import org.koin.core.annotation.Provided

/**
 * 管理播放进度周期上报、即时上报与应用级资源释放。
 */
@Provided
class PlaybackProgressReporter(
    private val musicController: MusicCommonController,
    private val dataSourceManager: DataSourceManager,
    private val settingsManager: SettingsManager
) : IoScoped() {

    /** 串行管理全部播放进度任务的内部 worker。 */
    private val worker: PlaybackProgressWorker

    init {
        createScope()
        worker = PlaybackProgressWorker(
            parentScope = scope,
            reportIntervalMillis = REPORT_INTERVAL_MILLIS,
            canContinue = ::shouldContinue,
            currentPosition = { musicController.progressStateFlow.value },
            reportProgress = { snapshot ->
                dataSourceManager.reportProgress(
                    snapshot.musicId,
                    snapshot.playSessionId,
                    snapshot.positionMs
                )
            },
            onReportFailure = { throwable ->
                Log.e(Constants.LOG_ERROR_PREFIX, "循环上报播放进度失败", throwable)
            }
        )
    }

    /**
     * 幂等启动指定歌曲的周期播放进度上报。
     */
    fun start(musicId: String) {
        if (!settingsManager.get().ifEnableSyncPlayProgress) {
            stop()
            return
        }
        worker.start(
            musicId = musicId,
            playSessionId = settingsManager.get().playSessionId
        )
    }

    /**
     * 使用播放器事件携带的不可变参数提交即时播放进度。
     */
    fun reportNow(musicId: String, positionMs: Long) {
        if (!settingsManager.get().ifEnableSyncPlayProgress) {
            stop()
            return
        }
        worker.reportNow(musicId, positionMs)
    }

    /**
     * 非阻塞地停止当前播放进度上报会话。
     */
    fun stop() {
        worker.stop()
    }

    /**
     * 停止当前会话并等待其定时器、请求与待处理快照全部结束。
     */
    suspend fun stopAndJoin() {
        worker.stopAndJoin()
    }

    /**
     * 判断指定会话是否仍满足周期或即时上报条件。
     */
    private fun shouldContinue(session: PlaybackProgressSession): Boolean {
        return settingsManager.get().ifEnableSyncPlayProgress &&
                musicController.state == PlayStateEnum.Playing &&
                musicController.musicInfo?.itemId == session.musicId
    }

    /**
     * 关闭 Reporter 自己持有的 worker 与 IO 作用域。
     */
    override fun close() {
        worker.close()
        super.close()
    }

    companion object {
        /** 周期播放进度上报的固定间隔。 */
        private const val REPORT_INTERVAL_MILLIS = 10_000L
    }
}
