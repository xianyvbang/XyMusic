package cn.xybbz.config.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import cn.xybbz.common.enums.PlayStateEnum
import cn.xybbz.common.music.MusicController
import cn.xybbz.common.utils.CoroutineScopeUtils
import cn.xybbz.api.client.IDataSourceManager
import cn.xybbz.config.alarm.AlarmConfig
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

//上报服务
@AndroidEntryPoint
class ReportReceiver : BroadcastReceiver() {

    @Inject
    lateinit var alarmConfig: AlarmConfig

    @Inject
    lateinit var musicController: MusicController

    @Inject
    lateinit var datasourceManager: IDataSourceManager


    override fun onReceive(context: Context, intent: Intent?) {
        // 条件判断：是否还需要继续上报
        val shouldContinue = checkIfShouldContinue()
        if (shouldContinue) {
            doUploadWork()
            alarmConfig.scheduleNextReport()
        } else {
            alarmConfig.cancelScheduleNextReport()
        }
    }

    private fun doUploadWork() {
        CoroutineScopeUtils.getIo("ReportReceiver").launch {
            datasourceManager.reportProgress(
                musicController.musicInfo?.itemId ?: "",
                musicController.musicInfo?.playSessionId ?: "",
                0
            )
        }
    }

    private fun checkIfShouldContinue(): Boolean {
        // 这里可以是 SharedPreferences 的某个值、计数器、远程配置等
        return musicController.state == PlayStateEnum.Playing || musicController.musicInfo == null // 或 false 来控制是否终止
    }
}