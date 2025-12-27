package cn.xybbz.common.music

import android.media.AudioTrack
import android.media.VolumeShaper
import android.os.Handler
import android.os.Looper

class AudioFadeController(
    private val fadeDurationMs: Long = 300L
) {

    private var currentTrack: AudioTrack? = null
    private var volumeShaper: VolumeShaper? = null
    private var pauseToken = 0
    private var released = false  // 新增标志

    fun attach(track: AudioTrack) {
        if (currentTrack === track) return
        release()
        currentTrack = track
        released = false
    }

    fun fadeIn() {
        val track = currentTrack ?: return
        if (released) return
        pauseToken++
        try {
            volumeShaper?.close()
            volumeShaper = track.createVolumeShaper(
                VolumeShaper.Configuration.Builder()
                    .setDuration(fadeDurationMs)
                    .setCurve(
                        floatArrayOf(0f, 1f),
                        floatArrayOf(0f, 1f)
                    )
                    .build()
            )

            volumeShaper?.apply(VolumeShaper.Operation.PLAY)
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun fadeOut(onEnd: () -> Unit) {
        val track = currentTrack ?: run {
            onEnd()
            return
        }
        if (released) {
            onEnd()
            return
        }
        val token = ++pauseToken
        try{
            volumeShaper?.close()
            volumeShaper = track.createVolumeShaper(
                VolumeShaper.Configuration.Builder()
                    .setDuration(fadeDurationMs)
                    .setCurve(
                        floatArrayOf(0f, 1f),
                        floatArrayOf(1f, 0f)
                    )
                    .build()
            )
            volumeShaper?.apply(VolumeShaper.Operation.PLAY)
            Handler(Looper.getMainLooper()).postDelayed({
                if (token == pauseToken) {
                    onEnd()
                }
            }, fadeDurationMs)
        }catch (e: Exception){
            e.printStackTrace()
            onEnd()
        }
    }

    fun release() {
        volumeShaper?.close()
        volumeShaper = null
        currentTrack = null
        released = true
    }
}

