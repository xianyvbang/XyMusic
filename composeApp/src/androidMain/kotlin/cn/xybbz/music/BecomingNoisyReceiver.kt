package cn.xybbz.music

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import cn.xybbz.config.music.MusicCommonController
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

/**
 * 用户 拔掉有线耳机
 * 蓝牙耳机 断开连接
 * 蓝牙音箱断开
 * 车载音频断开
 * 切换到设备扬声器播放
 * 以上情况的时候暂停音乐播放
 */
class BecomingNoisyReceiver : BroadcastReceiver(), KoinComponent {
    val musicController: MusicCommonController = get()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
            // Pause the playback
            musicController.pause()
        }
    }
}