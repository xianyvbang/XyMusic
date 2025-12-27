package cn.xybbz.common.music

import android.media.AudioTrack
import android.media.VolumeShaper
import android.os.Handler
import android.os.Looper
import android.util.Log

class AudioFadeController1(
    val fadeDurationMs: Long = 1000L
) {

    private var pauseToken = 0
    private var currentTrack: AudioTrack? = null
    private var currentShaper: VolumeShaper? = null

    fun attach(track: AudioTrack) {
        if (currentTrack === track) return
        cancelFade()
        Log.i("======", "重新创建淡入淡出控制")
        currentTrack = track
        track.setVolume(0f)
    }

    fun fadeIn() {
        pauseToken++
        Log.i("volume", "淡入调用")
        fade(0f, 1f)
    }

    fun fadeOut(onEnd: () -> Unit) {
        Log.i("volume", "淡出调用")
        val token = ++pauseToken
        fade(1f, 0f)
        Handler(Looper.getMainLooper()).postDelayed({
            if (token != pauseToken) return@postDelayed
            onEnd()
        }, fadeDurationMs)
    }

    private fun fade(from: Float, to: Float) {
        val track = currentTrack ?: return
        cancelFade()

        currentShaper = track.createVolumeShaper(
            VolumeShaper.Configuration.Builder()
                .setDuration(fadeDurationMs)
                .setCurve(
                    floatArrayOf(0f, 1f),
                    floatArrayOf(from, to)
                )
                .build()
        ).also {
            it.apply(VolumeShaper.Operation.PLAY)
        }
    }

    private fun cancelFade() {
        currentShaper?.close()
        currentShaper = null
    }

    fun release() {
        cancelFade()
        currentTrack = null
    }
}
