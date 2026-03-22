package cn.xybbz.config.service.alert

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import cn.xybbz.common.music.MusicController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.system.exitProcess

@AndroidEntryPoint
class AlertService : BroadcastReceiver() {

    @Inject
    lateinit var musicController: MusicController

    override fun onReceive(context: Context, intent: Intent?) {
        val booleanExtra = intent?.getBooleanExtra("ifPlayEndClose", false)
        Log.i("=====", "是否播放完关闭")
        if (booleanExtra == true) {
            MainScope().launch {
                musicController.progressStateFlow.collect {
                    if (it == 0L) {
                        exitApp(context)
                    }
                }
            }
        } else {
            exitApp(context)
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    private fun exitApp(context: Context) {
        if (context is Activity) {
            Log.i("=====", "正常关闭")
            context.finishAffinity()
        } else {
            // 强制退出
            Log.i("=====", "强制退出")
            android.os.Process.killProcess(android.os.Process.myPid())
            exitProcess(0)
        }
    }
}