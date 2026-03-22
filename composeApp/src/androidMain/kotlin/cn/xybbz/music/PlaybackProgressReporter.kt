package cn.xybbz.music

import android.util.Log
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.enums.PlayStateEnum
import cn.xybbz.config.setting.SettingsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaybackProgressReporter @Inject constructor(
    private val musicController: MusicController,
    private val dataSourceManager: DataSourceManager,
    private val settingsManager: SettingsManager
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var reportJob: Job? = null

    fun start() {
        if (!settingsManager.get().ifEnableSyncPlayProgress) {
            stop()
            return
        }
        if (reportJob?.isActive == true) {
            return
        }
        reportJob = scope.launch {
            while (true) {
                delay(REPORT_INTERVAL_MILLIS)
                if (!shouldContinue()) {
                    reportJob = null
                    break
                }
                reportCurrentProgress()
            }
        }
    }

    fun reportNow() {
        if (!settingsManager.get().ifEnableSyncPlayProgress) {
            stop()
            return
        }
        scope.launch {
            reportCurrentProgress()
        }
    }

    fun stop() {
        reportJob?.cancel()
        reportJob = null
    }

    private fun shouldContinue(): Boolean {
        return settingsManager.get().ifEnableSyncPlayProgress &&
                musicController.state == PlayStateEnum.Playing &&
                musicController.musicInfo != null
    }

    private suspend fun reportCurrentProgress() {
        val musicId = musicController.musicInfo?.itemId.orEmpty()
        if (musicId.isBlank()) {
            return
        }
        runCatching {
            dataSourceManager.reportProgress(
                musicId,
                settingsManager.get().playSessionId,
                musicController.progressStateFlow.value
            )
        }.onFailure {
            Log.e(Constants.LOG_ERROR_PREFIX, "循环上报播放进度失败", it)
        }
    }

    companion object {
        private const val REPORT_INTERVAL_MILLIS = 10_000L
    }
}
