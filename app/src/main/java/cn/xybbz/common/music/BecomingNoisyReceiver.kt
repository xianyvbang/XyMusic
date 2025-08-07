package cn.xybbz.common.music

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BecomingNoisyReceiver : BroadcastReceiver() {

    @Inject lateinit var musicController: MusicController

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
            // Pause the playback
            musicController.pause()
        }
    }
}