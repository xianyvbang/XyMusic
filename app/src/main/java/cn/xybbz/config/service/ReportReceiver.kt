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

package cn.xybbz.config.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.common.enums.PlayStateEnum
import cn.xybbz.common.music.MusicController
import cn.xybbz.config.alarm.AlarmConfig
import cn.xybbz.config.setting.SettingsManager
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
    lateinit var datasourceManager: DataSourceManager

    @Inject
    lateinit var settingsManager: SettingsManager


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
        datasourceManager.dataSourceScope().launch {
            datasourceManager.reportProgress(
                musicController.musicInfo?.itemId ?: "",
                settingsManager.get().playSessionId,
                musicController.progressStateFlow.value
            )
        }
    }

    private fun checkIfShouldContinue(): Boolean {
        // 这里可以是 SharedPreferences 的某个值、计数器、远程配置等
        return musicController.state == PlayStateEnum.Playing || musicController.musicInfo == null // 或 false 来控制是否终止
    }
}