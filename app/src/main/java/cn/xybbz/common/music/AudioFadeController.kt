package cn.xybbz.common.music

import android.media.AudioTrack
import android.media.VolumeShaper
import android.os.Handler
import android.os.Looper

class AudioFadeController(
    private val fadeDurationMs: Long = 1200L
) {

    private var fadeIn: VolumeShaper? = null
    private var fadeOut: VolumeShaper? = null
    private var currentTrack: AudioTrack? = null

    private var isFading = false

    fun attach(track: AudioTrack) {
        if (currentTrack === track) return

        release()
        currentTrack = track

        fadeIn = track.createVolumeShaper(
            VolumeShaper.Configuration.Builder()
                .setDuration(fadeDurationMs)
                .setCurve(
                    floatArrayOf(0f, 1f),
                    floatArrayOf(0f, 1f)
                )
                .build()
        )

        fadeOut = track.createVolumeShaper(
            VolumeShaper.Configuration.Builder()
                .setDuration(fadeDurationMs)
                .setCurve(
                    floatArrayOf(0f, 1f),
                    floatArrayOf(1f, 0f)
                )
                .build()
        )
    }

    fun fadeIn() {
        if (isFading) return
        isFading = true

        fadeOut?.apply(VolumeShaper.Operation.REVERSE)
        fadeIn?.apply(VolumeShaper.Operation.PLAY)

        resetFlagLater()
    }

    fun fadeOut() {
        if (isFading) return
        isFading = true

        fadeIn?.apply(VolumeShaper.Operation.REVERSE)
        fadeOut?.apply(VolumeShaper.Operation.PLAY)

        resetFlagLater()
    }

    private fun resetFlagLater() {
        Handler(Looper.getMainLooper()).postDelayed({
            isFading = false
        }, fadeDurationMs)
    }

    fun release() {
        fadeIn?.close()
        fadeOut?.close()
        fadeIn = null
        fadeOut = null
        currentTrack = null
        isFading = false
    }
}
