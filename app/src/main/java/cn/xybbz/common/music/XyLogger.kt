package cn.xybbz.common.music

import android.os.SystemClock
import android.text.TextUtils
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.C.TrackType
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.Metadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Player.DiscontinuityReason
import androidx.media3.common.Player.MediaItemTransitionReason
import androidx.media3.common.Player.PlayWhenReadyChangeReason
import androidx.media3.common.Player.PlaybackSuppressionReason
import androidx.media3.common.Player.TimelineChangeReason
import androidx.media3.common.Timeline
import androidx.media3.common.Tracks
import androidx.media3.common.VideoSize
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.DecoderCounters
import androidx.media3.exoplayer.DecoderReuseEvaluation
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.analytics.AnalyticsListener.EventTime
import androidx.media3.exoplayer.audio.AudioSink.AudioTrackConfig
import androidx.media3.exoplayer.drm.DrmSession
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import androidx.media3.extractor.metadata.id3.TextInformationFrame
import androidx.media3.extractor.metadata.vorbis.VorbisComment
import cn.xybbz.common.utils.LrcUtils
import cn.xybbz.config.lrc.LrcServer
import cn.xybbz.localdata.config.DatabaseClient
import java.io.IOException
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.min


/** Logs events from [Player] and other core components using [Log].  */
class XyLogger @JvmOverloads constructor(private val tag: String = DEFAULT_TAG,private val db: DatabaseClient) :
    AnalyticsListener {
    private val window: Timeline.Window
    private val period: Timeline.Period
    private val startTimeMs: Long

    /**
     * Creates an instance.
     *
     * @param tag The tag used for logging.
     */
    /** Creates an instance.  */
    init {
        window = Timeline.Window()
        period = Timeline.Period()
        startTimeMs = SystemClock.elapsedRealtime()
    }

    // AnalyticsListener
    @UnstableApi
    override fun onIsLoadingChanged(eventTime: EventTime, isLoading: Boolean) {
        logd(eventTime, "loading", isLoading.toString())
    }

    @UnstableApi
    override fun onPlaybackStateChanged(eventTime: EventTime, state: @Player.State Int) {
        logd(eventTime, "state", getStateString(state))
    }

    @UnstableApi
    override fun onPlayWhenReadyChanged(
        eventTime: EventTime, playWhenReady: Boolean, reason: @PlayWhenReadyChangeReason Int
    ) {
        logd(
            eventTime,
            "playWhenReady",
            playWhenReady.toString() + ", " + getPlayWhenReadyChangeReasonString(reason)
        )
    }

    @UnstableApi
    override fun onPlaybackSuppressionReasonChanged(
        eventTime: EventTime, playbackSuppressionReason: @PlaybackSuppressionReason Int
    ) {
        logd(
            eventTime,
            "playbackSuppressionReason",
            getPlaybackSuppressionReasonString(playbackSuppressionReason)
        )
    }

    @UnstableApi
    override fun onIsPlayingChanged(eventTime: EventTime, isPlaying: Boolean) {
        logd(eventTime, "isPlaying", isPlaying.toString())
    }

    @UnstableApi
    override fun onRepeatModeChanged(eventTime: EventTime, repeatMode: @Player.RepeatMode Int) {
        logd(eventTime, "repeatMode", getRepeatModeString(repeatMode))
    }

    @UnstableApi
    override fun onShuffleModeChanged(eventTime: EventTime, shuffleModeEnabled: Boolean) {
        logd(eventTime, "shuffleModeEnabled", shuffleModeEnabled.toString())
    }

    @UnstableApi
    override fun onPositionDiscontinuity(
        eventTime: EventTime,
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        reason: @DiscontinuityReason Int
    ) {
        val builder = StringBuilder()
        builder
            .append("reason=")
            .append(getDiscontinuityReasonString(reason))
            .append(", PositionInfo:old [")
            .append("mediaItem=")
            .append(oldPosition.mediaItemIndex)
            .append(", period=")
            .append(oldPosition.periodIndex)
            .append(", pos=")
            .append(oldPosition.positionMs)
        if (oldPosition.adGroupIndex != C.INDEX_UNSET) {
            builder
                .append(", contentPos=")
                .append(oldPosition.contentPositionMs)
                .append(", adGroup=")
                .append(oldPosition.adGroupIndex)
                .append(", ad=")
                .append(oldPosition.adIndexInAdGroup)
        }
        builder
            .append("], PositionInfo:new [")
            .append("mediaItem=")
            .append(newPosition.mediaItemIndex)
            .append(", period=")
            .append(newPosition.periodIndex)
            .append(", pos=")
            .append(newPosition.positionMs)
        if (newPosition.adGroupIndex != C.INDEX_UNSET) {
            builder
                .append(", contentPos=")
                .append(newPosition.contentPositionMs)
                .append(", adGroup=")
                .append(newPosition.adGroupIndex)
                .append(", ad=")
                .append(newPosition.adIndexInAdGroup)
        }
        builder.append("]")
        logd(eventTime, "positionDiscontinuity", builder.toString())
    }

    @UnstableApi
    override fun onPlaybackParametersChanged(
        eventTime: EventTime, playbackParameters: PlaybackParameters
    ) {
        logd(eventTime, "playbackParameters", playbackParameters.toString())
    }

    @UnstableApi
    override fun onTimelineChanged(eventTime: EventTime, reason: @TimelineChangeReason Int) {
        val periodCount = eventTime.timeline.getPeriodCount()
        val windowCount = eventTime.timeline.getWindowCount()
        logd(
            ("timeline ["
                    + getEventTimeString(eventTime)
                    + ", periodCount="
                    + periodCount
                    + ", windowCount="
                    + windowCount
                    + ", reason="
                    + getTimelineChangeReasonString(reason))
        )
        for (i in 0..<min(periodCount.toDouble(), MAX_TIMELINE_ITEM_LINES.toDouble()).toInt()) {
            eventTime.timeline.getPeriod(i, period)
            logd("  " + "period [" + getTimeString(period.getDurationMs()) + "]")
        }
        if (periodCount > MAX_TIMELINE_ITEM_LINES) {
            logd("  ...")
        }
        for (i in 0..<min(windowCount.toDouble(), MAX_TIMELINE_ITEM_LINES.toDouble()).toInt()) {
            eventTime.timeline.getWindow(i, window)
            logd(
                ("  "
                        + "window ["
                        + getTimeString(window.getDurationMs())
                        + ", seekable="
                        + window.isSeekable
                        + ", dynamic="
                        + window.isDynamic
                        + "]")
            )
        }
        if (windowCount > MAX_TIMELINE_ITEM_LINES) {
            logd("  ...")
        }
        logd("]")
    }

    @UnstableApi
    override fun onMediaItemTransition(
        eventTime: EventTime, mediaItem: MediaItem?, reason: Int
    ) {
        logd(
            ("mediaItem ["
                    + getEventTimeString(eventTime)
                    + ", reason="
                    + getMediaItemTransitionReasonString(reason)
                    + "]")
        )
    }

    @UnstableApi
    override fun onPlayerError(eventTime: EventTime, error: PlaybackException) {
        loge(eventTime, "playerFailed", error)
    }

    @UnstableApi
    override fun onTracksChanged(eventTime: EventTime, tracks: Tracks) {
        logd("tracks [" + getEventTimeString(eventTime))
        // Log tracks associated to renderers.
        val trackGroups = tracks.getGroups()
        for (groupIndex in trackGroups.indices) {
            val trackGroup = trackGroups.get(groupIndex)
            logd("  group [")
            for (trackIndex in 0..<trackGroup.length) {
                val status = getTrackStatusString(trackGroup.isTrackSelected(trackIndex))
                val formatSupport =
                    Util.getFormatSupportString(trackGroup.getTrackSupport(trackIndex))
                logd(
                    ("    "
                            + status
                            + " Track:"
                            + trackIndex
                            + ", "
                            + Format.toLogString(trackGroup.getTrackFormat(trackIndex))
                            + ", supported="
                            + formatSupport)
                )
            }
            logd("  ]")
        }
        // TODO: Replace this with an override of onMediaMetadataChanged.
        // Log metadata for at most one of the selected tracks.
        var loggedMetadata = false
        var groupIndex = 0
        while (!loggedMetadata && groupIndex < trackGroups.size) {
            val trackGroup = trackGroups.get(groupIndex)
            var trackIndex = 0
            while (!loggedMetadata && trackIndex < trackGroup.length) {
                if (trackGroup.isTrackSelected(trackIndex)) {
                    val metadata = trackGroup.getTrackFormat(trackIndex).metadata
                    if (metadata != null && metadata.length() > 0) {
                        logd("  Metadata [")
                        printMetadata(metadata, "    ")
                        logd("  ]")
                        loggedMetadata = true
                    }
                }
                trackIndex++
            }
            groupIndex++
        }
        logd("]")
    }

    @UnstableApi
    override fun onMetadata(eventTime: EventTime, metadata: Metadata) {
        logd("metadata [" + getEventTimeString(eventTime))
        printMetadata(metadata, "  ")
        logd("]")
    }

    @UnstableApi
    override fun onAudioEnabled(eventTime: EventTime, decoderCounters: DecoderCounters) {
        logd(eventTime, "audioEnabled")
    }

    @UnstableApi
    override fun onAudioDecoderInitialized(
        eventTime: EventTime,
        decoderName: String,
        initializedTimestampMs: Long,
        initializationDurationMs: Long
    ) {
        logd(eventTime, "audioDecoderInitialized", decoderName)
    }

    @UnstableApi
    override fun onAudioInputFormatChanged(
        eventTime: EventTime, format: Format, decoderReuseEvaluation: DecoderReuseEvaluation?
    ) {
        logd(eventTime, "audioInputFormat", Format.toLogString(format))
    }

    @UnstableApi
    override fun onAudioUnderrun(
        eventTime: EventTime, bufferSize: Int, bufferSizeMs: Long, elapsedSinceLastFeedMs: Long
    ) {
        loge(
            eventTime,
            "audioTrackUnderrun",
            bufferSize.toString() + ", " + bufferSizeMs + ", " + elapsedSinceLastFeedMs,  /* throwable= */
            null
        )
    }

    @UnstableApi
    override fun onAudioDecoderReleased(eventTime: EventTime, decoderName: String) {
        logd(eventTime, "audioDecoderReleased", decoderName)
    }

    @UnstableApi
    override fun onAudioDisabled(eventTime: EventTime, decoderCounters: DecoderCounters) {
        logd(eventTime, "audioDisabled")
    }

    @UnstableApi
    override fun onAudioSessionIdChanged(eventTime: EventTime, audioSessionId: Int) {
        logd(eventTime, "audioSessionId", audioSessionId.toString())
    }

    @UnstableApi
    override fun onAudioAttributesChanged(eventTime: EventTime, audioAttributes: AudioAttributes) {
        logd(
            eventTime,
            "audioAttributes",
            (audioAttributes.contentType
                .toString() + ","
                    + audioAttributes.flags
                    + ","
                    + audioAttributes.usage
                    + ","
                    + audioAttributes.allowedCapturePolicy)
        )
    }

    @UnstableApi
    override fun onSkipSilenceEnabledChanged(eventTime: EventTime, skipSilenceEnabled: Boolean) {
        logd(eventTime, "skipSilenceEnabled", skipSilenceEnabled.toString())
    }

    @UnstableApi
    override fun onVolumeChanged(eventTime: EventTime, volume: Float) {
        logd(eventTime, "volume", volume.toString())
    }

    @UnstableApi
    override fun onAudioTrackInitialized(
        eventTime: EventTime, audioTrackConfig: AudioTrackConfig
    ) {
        logd(eventTime, "audioTrackInit", getAudioTrackConfigString(audioTrackConfig))
    }

    @UnstableApi
    override fun onAudioTrackReleased(
        eventTime: EventTime, audioTrackConfig: AudioTrackConfig
    ) {
        logd(eventTime, "audioTrackReleased", getAudioTrackConfigString(audioTrackConfig))
    }

    @UnstableApi
    override fun onVideoEnabled(eventTime: EventTime, decoderCounters: DecoderCounters) {
        logd(eventTime, "videoEnabled")
    }

    @UnstableApi
    override fun onVideoDecoderInitialized(
        eventTime: EventTime,
        decoderName: String,
        initializedTimestampMs: Long,
        initializationDurationMs: Long
    ) {
        logd(eventTime, "videoDecoderInitialized", decoderName)
    }

    @UnstableApi
    override fun onVideoInputFormatChanged(
        eventTime: EventTime, format: Format, decoderReuseEvaluation: DecoderReuseEvaluation?
    ) {
        logd(eventTime, "videoInputFormat", Format.toLogString(format))
    }

    @UnstableApi
    override fun onDroppedVideoFrames(eventTime: EventTime, droppedFrames: Int, elapsedMs: Long) {
        logd(eventTime, "droppedFrames", droppedFrames.toString())
    }

    @UnstableApi
    override fun onVideoDecoderReleased(eventTime: EventTime, decoderName: String) {
        logd(eventTime, "videoDecoderReleased", decoderName)
    }

    @UnstableApi
    override fun onVideoDisabled(eventTime: EventTime, decoderCounters: DecoderCounters) {
        logd(eventTime, "videoDisabled")
    }

    @UnstableApi
    override fun onRenderedFirstFrame(eventTime: EventTime, output: Any, renderTimeMs: Long) {
        logd(eventTime, "renderedFirstFrame", output.toString())
    }

    @UnstableApi
    override fun onVideoSizeChanged(eventTime: EventTime, videoSize: VideoSize) {
        logd(eventTime, "videoSize", videoSize.width.toString() + ", " + videoSize.height)
    }

    @UnstableApi
    override fun onLoadError(
        eventTime: EventTime,
        loadEventInfo: LoadEventInfo,
        mediaLoadData: MediaLoadData,
        error: IOException,
        wasCanceled: Boolean
    ) {
        printInternalError(eventTime, "loadError", error)
    }

    @UnstableApi
    override fun onSurfaceSizeChanged(eventTime: EventTime, width: Int, height: Int) {
        logd(eventTime, "surfaceSize", width.toString() + ", " + height)
    }

    @UnstableApi
    override fun onUpstreamDiscarded(eventTime: EventTime, mediaLoadData: MediaLoadData) {
        logd(eventTime, "upstreamDiscarded", Format.toLogString(mediaLoadData.trackFormat))
    }

    @UnstableApi
    override fun onDownstreamFormatChanged(eventTime: EventTime, mediaLoadData: MediaLoadData) {
        logd(eventTime, "downstreamFormat", Format.toLogString(mediaLoadData.trackFormat))
    }

    @UnstableApi
    override fun onDrmSessionAcquired(eventTime: EventTime, state: @DrmSession.State Int) {
        logd(eventTime, "drmSessionAcquired", "state=" + state)
    }

    @UnstableApi
    override fun onDrmSessionManagerError(eventTime: EventTime, error: Exception) {
        printInternalError(eventTime, "drmSessionManagerError", error)
    }

    @UnstableApi
    override fun onDrmKeysRestored(eventTime: EventTime) {
        logd(eventTime, "drmKeysRestored")
    }

    @UnstableApi
    override fun onDrmKeysRemoved(eventTime: EventTime) {
        logd(eventTime, "drmKeysRemoved")
    }

    @UnstableApi
    override fun onDrmKeysLoaded(eventTime: EventTime) {
        logd(eventTime, "drmKeysLoaded")
    }

    @UnstableApi
    override fun onDrmSessionReleased(eventTime: EventTime) {
        logd(eventTime, "drmSessionReleased")
    }

    @UnstableApi
    override fun onRendererReadyChanged(
        eventTime: EventTime,
        rendererIndex: Int,
        rendererTrackType: @TrackType Int,
        isRendererReady: Boolean
    ) {
        logd(
            eventTime,
            "rendererReady",
            ("rendererIndex="
                    + rendererIndex
                    + ", "
                    + Util.getTrackTypeString(rendererTrackType)
                    + ", "
                    + isRendererReady)
        )
    }

    /**
     * Logs a debug message.
     *
     * @param msg The message to log.
     */
    @UnstableApi
    protected fun logd(msg: String) {
        Log.d(tag, msg)
    }

    /**
     * Logs an error message.
     *
     * @param msg The message to log.
     */
    @UnstableApi
    protected fun loge(msg: String) {
        Log.e(tag, msg)
    }

    // Internal methods
    @OptIn(UnstableApi::class)
    private fun logd(eventTime: EventTime, eventName: String) {
        logd(
            getEventString(
                eventTime,
                eventName,  /* eventDescription= */
                null,  /* throwable= */
                null
            )
        )
    }

    @OptIn(UnstableApi::class)
    private fun logd(eventTime: EventTime, eventName: String, eventDescription: String) {
        logd(getEventString(eventTime, eventName, eventDescription,  /* throwable= */null))
    }

    @OptIn(UnstableApi::class)
    private fun loge(eventTime: EventTime, eventName: String, throwable: Throwable?) {
        loge(getEventString(eventTime, eventName,  /* eventDescription= */null, throwable))
    }

    @OptIn(UnstableApi::class)
    private fun loge(
        eventTime: EventTime,
        eventName: String,
        eventDescription: String,
        throwable: Throwable?
    ) {
        loge(getEventString(eventTime, eventName, eventDescription, throwable))
    }

    @OptIn(UnstableApi::class)
    private fun printInternalError(eventTime: EventTime, type: String, e: Exception) {
        loge(eventTime, "internalError", type, e)
    }

    @OptIn(UnstableApi::class)
    private fun printMetadata(metadata: Metadata, prefix: String) {
        for (i in 0..<metadata.length()) {
            val entry = metadata.get(i)
            logd(prefix + entry)
            if (entry is TextInformationFrame) {
                val lyrics = entry.values[0]

                Log.d("Lyrics", "Lyrics found: $lyrics")
                LrcServer.createLrcList(LrcUtils.parseLrc(lyrics))
            }else if (entry is VorbisComment && entry.key == "LYRICS"){
                val lyrics = entry.value
                LrcServer.createLrcList(LrcUtils.parseLrc(lyrics))
                Log.d("Lyrics", "Lyrics found: $lyrics")
            }
        }
    }

    @OptIn(UnstableApi::class)
    private fun getEventString(
        eventTime: EventTime,
        eventName: String,
        eventDescription: String?,
        throwable: Throwable?
    ): String {
        var eventString = eventName + " [" + getEventTimeString(eventTime)
        if (throwable is PlaybackException) {
            eventString += ", errorCode=" + throwable.getErrorCodeName()
        }
        if (eventDescription != null) {
            eventString += ", " + eventDescription
        }
        val throwableString = Log.getThrowableString(throwable)
        if (!TextUtils.isEmpty(throwableString)) {
            eventString += "\n  " + throwableString!!.replace("\n", "\n  ") + '\n'
        }
        eventString += "]"
        return eventString
    }

    @OptIn(UnstableApi::class)
    private fun getEventTimeString(eventTime: EventTime): String {
        var windowPeriodString = "window=" + eventTime.windowIndex
        if (eventTime.mediaPeriodId != null) {
            windowPeriodString +=
                ", period=" + eventTime.timeline.getIndexOfPeriod(eventTime.mediaPeriodId!!.periodUid)
            if (eventTime.mediaPeriodId!!.isAd()) {
                windowPeriodString += ", adGroup=" + eventTime.mediaPeriodId!!.adGroupIndex
                windowPeriodString += ", ad=" + eventTime.mediaPeriodId!!.adIndexInAdGroup
            }
        }
        return ("eventTime="
                + getTimeString(eventTime.realtimeMs - startTimeMs)
                + ", mediaPos="
                + getTimeString(eventTime.eventPlaybackPositionMs)
                + ", "
                + windowPeriodString)
    }

    companion object {
        private const val DEFAULT_TAG = "XyLogger"
        private const val MAX_TIMELINE_ITEM_LINES = 3
        private val TIME_FORMAT: NumberFormat = NumberFormat.getInstance(Locale.CHINA)

        init {
            TIME_FORMAT.setMinimumFractionDigits(2)
            TIME_FORMAT.setMaximumFractionDigits(2)
            TIME_FORMAT.isGroupingUsed = false
        }

        private fun getTimeString(timeMs: Long): String {
            return if (timeMs == C.TIME_UNSET) "?" else TIME_FORMAT.format(((timeMs) / 1000f).toDouble())
        }

        private fun getStateString(state: Int): String {
            when (state) {
                Player.STATE_BUFFERING -> return "BUFFERING"
                Player.STATE_ENDED -> return "ENDED"
                Player.STATE_IDLE -> return "IDLE"
                Player.STATE_READY -> return "READY"
                else -> return "?"
            }
        }

        private fun getTrackStatusString(selected: Boolean): String {
            return if (selected) "[X]" else "[ ]"
        }

        private fun getRepeatModeString(repeatMode: @Player.RepeatMode Int): String {
            when (repeatMode) {
                Player.REPEAT_MODE_OFF -> return "OFF"
                Player.REPEAT_MODE_ONE -> return "ONE"
                Player.REPEAT_MODE_ALL -> return "ALL"
                else -> return "?"
            }
        }

        private fun getDiscontinuityReasonString(reason: @DiscontinuityReason Int): String {
            when (reason) {
                Player.DISCONTINUITY_REASON_AUTO_TRANSITION -> return "AUTO_TRANSITION"
                Player.DISCONTINUITY_REASON_SEEK -> return "SEEK"
                Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT -> return "SEEK_ADJUSTMENT"
                Player.DISCONTINUITY_REASON_REMOVE -> return "REMOVE"
                Player.DISCONTINUITY_REASON_SKIP -> return "SKIP"
                Player.DISCONTINUITY_REASON_INTERNAL -> return "INTERNAL"
                Player.DISCONTINUITY_REASON_SILENCE_SKIP -> return "SILENCE_SKIP"
                else -> return "?"
            }
        }

        private fun getTimelineChangeReasonString(reason: @TimelineChangeReason Int): String {
            when (reason) {
                Player.TIMELINE_CHANGE_REASON_SOURCE_UPDATE -> return "SOURCE_UPDATE"
                Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED -> return "PLAYLIST_CHANGED"
                else -> return "?"
            }
        }

        private fun getMediaItemTransitionReasonString(
            reason: @MediaItemTransitionReason Int
        ): String {
            when (reason) {
                Player.MEDIA_ITEM_TRANSITION_REASON_AUTO -> return "AUTO"
                Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED -> return "PLAYLIST_CHANGED"
                Player.MEDIA_ITEM_TRANSITION_REASON_REPEAT -> return "REPEAT"
                Player.MEDIA_ITEM_TRANSITION_REASON_SEEK -> return "SEEK"
                else -> return "?"
            }
        }

        private fun getPlaybackSuppressionReasonString(
            playbackSuppressionReason: @PlaybackSuppressionReason Int
        ): String {
            when (playbackSuppressionReason) {
                Player.PLAYBACK_SUPPRESSION_REASON_NONE -> return "NONE"
                Player.PLAYBACK_SUPPRESSION_REASON_TRANSIENT_AUDIO_FOCUS_LOSS -> return "TRANSIENT_AUDIO_FOCUS_LOSS"
                Player.PLAYBACK_SUPPRESSION_REASON_UNSUITABLE_AUDIO_OUTPUT -> return "UNSUITABLE_AUDIO_OUTPUT"
                else -> return "?"
            }
        }

        private fun getPlayWhenReadyChangeReasonString(
            reason: @PlayWhenReadyChangeReason Int
        ): String {
            when (reason) {
                Player.PLAY_WHEN_READY_CHANGE_REASON_AUDIO_BECOMING_NOISY -> return "AUDIO_BECOMING_NOISY"
                Player.PLAY_WHEN_READY_CHANGE_REASON_AUDIO_FOCUS_LOSS -> return "AUDIO_FOCUS_LOSS"
                Player.PLAY_WHEN_READY_CHANGE_REASON_REMOTE -> return "REMOTE"
                Player.PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST -> return "USER_REQUEST"
                Player.PLAY_WHEN_READY_CHANGE_REASON_END_OF_MEDIA_ITEM -> return "END_OF_MEDIA_ITEM"
                else -> return "?"
            }
        }

        @OptIn(UnstableApi::class)
        private fun getAudioTrackConfigString(audioTrackConfig: AudioTrackConfig): String {
            return (audioTrackConfig.encoding
                .toString() + ","
                    + audioTrackConfig.channelConfig
                    + ","
                    + audioTrackConfig.sampleRate
                    + ","
                    + audioTrackConfig.tunneling
                    + ","
                    + audioTrackConfig.offload
                    + ","
                    + audioTrackConfig.bufferSize)
        }
    }
}
