package cn.xybbz.api.client.jellyfin.data

import cn.xybbz.api.client.data.Request
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * An audio stream.
 */
@JsonClass(generateAdapter = true)
 data class GetAudioStreamRequest(
	/**
	 * The item id.
	 */
	@param:Json(name = "itemId")
	 val itemId: String,
	/**
	 * The audio container.
	 */
	@param:Json(name = "container")
	 val container: List<String>? = null,
	/**
	 * Optional. If true, the original file will be streamed statically without any encoding. Use
	 * either no url extension or the original file extension. true/false.
	 */
	@param:Json(name = "static")
	 val static: Boolean = false,
	/**
	 * The streaming parameters.
	 */
	@param:Json(name = "params")
	 val params: String? = null,
	/**
	 * The tag.
	 */
	@param:Json(name = "tag")
	 val tag: String? = null,
	/**
	 * The play session id.
	 */
	@param:Json(name = "playSessionId")
	 val playSessionId: String? = null,
	/**
	 * The segment container.
	 */
	@param:Json(name = "segmentContainer")
	 val segmentContainer: String? = null,
	/**
	 * The segment length.
	 */
	@param:Json(name = "segmentLength")
	 val segmentLength: Int? = null,
	/**
	 * The minimum number of segments.
	 */
	@param:Json(name = "minSegments")
	 val minSegments: Int? = null,
	/**
	 * The media version id, if playing an alternate version.
	 */
	@param:Json(name = "mediaSourceId")
	 val mediaSourceId: String? = null,
	/**
	 * The device id of the client requesting. Used to stop encoding processes when needed.
	 */
	@param:Json(name = "deviceId")
	 val deviceId: String? = null,
	/**
	 * Optional. Specify a audio codec to encode to, e.g. mp3. If omitted the server will auto-select
	 * using the url's extension. Options: aac, mp3, vorbis, wma.
	 */
	@param:Json(name = "audioCodec")
	 val audioCodec: String? = null,
	/**
	 * Whether or not to allow automatic stream copy if requested values match the original source.
	 * Defaults to true.
	 */
	@param:Json(name = "enableAutoStreamCopy")
	 val enableAutoStreamCopy: Boolean? = null,
	/**
	 * Whether or not to allow copying of the video stream url.
	 */
	@param:Json(name = "allowVideoStreamCopy")
	 val allowVideoStreamCopy: Boolean? = null,
	/**
	 * Whether or not to allow copying of the audio stream url.
	 */
	@param:Json(name = "allowAudioStreamCopy")
	 val allowAudioStreamCopy: Boolean? = null,
	/**
	 * Optional. Whether to break on non key frames.
	 */
	@param:Json(name = "breakOnNonKeyFrames")
	 val breakOnNonKeyFrames: Boolean? = null,
	/**
	 * Optional. Specify a specific audio sample rate, e.g. 44100.
	 */
	@param:Json(name = "audioSampleRate")
	 val audioSampleRate: Int? = null,
	/**
	 * Optional. The maximum audio bit depth.
	 */
	@param:Json(name = "maxAudioBitDepth")
	 val maxAudioBitDepth: Int? = null,
	/**
	 * Optional. Specify an audio bitrate to encode to, e.g. 128000. If omitted this will be left to
	 * encoder defaults.
	 */
	@param:Json(name = "audioBitRate")
	 val audioBitRate: Int? = null,
	/**
	 * Optional. Specify a specific number of audio channels to encode to, e.g. 2.
	 */
	@param:Json(name = "audioChannels")
	 val audioChannels: Int? = null,
	/**
	 * Optional. Specify a maximum number of audio channels to encode to, e.g. 2.
	 */
	@param:Json(name = "maxAudioChannels")
	 val maxAudioChannels: Int? = null,
	/**
	 * Optional. Specify a specific an encoder profile (varies by encoder), e.g. main, baseline, high.
	 */
	@param:Json(name = "profile")
	 val profile: String? = null,
	/**
	 * Optional. Specify a level for the encoder profile (varies by encoder), e.g. 3, 3.1.
	 */
	@param:Json(name = "level")
	 val level: String? = null,
	/**
	 * Optional. A specific video framerate to encode to, e.g. 23.976. Generally this should be omitted
	 * unless the device has specific requirements.
	 */
	@param:Json(name = "framerate")
	 val framerate: Float? = null,
	/**
	 * Optional. A specific maximum video framerate to encode to, e.g. 23.976. Generally this should be
	 * omitted unless the device has specific requirements.
	 */
	@param:Json(name = "maxFramerate")
	 val maxFramerate: Float? = null,
	/**
	 * Whether or not to copy timestamps when transcoding with an offset. Defaults to false.
	 */
	@param:Json(name = "copyTimestamps")
	 val copyTimestamps: Boolean? = null,
	/**
	 * Optional. Specify a starting offset, in ticks. 1 tick = 10000 ms.
	 */
	@param:Json(name = "startTimeTicks")
	 val startTimeTicks: Long? = null,
	/**
	 * Optional. The fixed horizontal resolution of the encoded video.
	 */
	@param:Json(name = "width")
	 val width: Int? = null,
	/**
	 * Optional. The fixed vertical resolution of the encoded video.
	 */
	@param:Json(name = "height")
	 val height: Int? = null,
	/**
	 * Optional. Specify a video bitrate to encode to, e.g. 500000. If omitted this will be left to
	 * encoder defaults.
	 */
	@param:Json(name = "videoBitRate")
	 val videoBitRate: Int? = null,
	/**
	 * Optional. The index of the subtitle stream to use. If omitted no subtitles will be used.
	 */
	@param:Json(name = "subtitleStreamIndex")
	 val subtitleStreamIndex: Int? = null,
	/**
	 * Optional.
	 */
	@param:Json(name = "maxRefFrames")
	 val maxRefFrames: Int? = null,
	/**
	 * Optional. The maximum video bit depth.
	 */
	@param:Json(name = "maxVideoBitDepth")
	 val maxVideoBitDepth: Int? = null,
	/**
	 * Optional. Whether to require avc.
	 */
	@param:Json(name = "requireAvc")
	 val requireAvc: Boolean? = null,
	/**
	 * Optional. Whether to deinterlace the video.
	 */
	@param:Json(name = "deInterlace")
	 val deInterlace: Boolean? = null,
	/**
	 * Optional. Whether to require a non anamorphic stream.
	 */
	@param:Json(name = "requireNonAnamorphic")
	 val requireNonAnamorphic: Boolean? = null,
	/**
	 * Optional. The maximum number of audio channels to transcode.
	 */
	@param:Json(name = "transcodingMaxAudioChannels")
	 val transcodingMaxAudioChannels: Int? = null,
	/**
	 * Optional. The limit of how many cpu cores to use.
	 */
	@param:Json(name = "cpuCoreLimit")
	 val cpuCoreLimit: Int? = null,
	/**
	 * The live stream id.
	 */
	@param:Json(name = "liveStreamId")
	 val liveStreamId: String? = null,
	/**
	 * Optional. Whether to enable the MpegtsM2Ts mode.
	 */
	@param:Json(name = "enableMpegtsM2TsMode")
	 val enableMpegtsM2TsMode: Boolean? = null,
	/**
	 * Optional. Specify a video codec to encode to, e.g. h264. If omitted the server will auto-select
	 * using the url's extension. Options: h265, h264, mpeg4, theora, vp8, vp9, vpx (deprecated), wmv.
	 */
	@param:Json(name = "videoCodec")
	 val videoCodec: String? = null,
	/**
	 * Optional. Specify a subtitle codec to encode to.
	 */
	@param:Json(name = "subtitleCodec")
	 val subtitleCodec: String? = null,
	/**
	 * Optional. The transcoding reason.
	 */
	@param:Json(name = "transcodeReasons")
	 val transcodeReasons: String? = null,
	/**
	 * Optional. The index of the audio stream to use. If omitted the first audio stream will be used.
	 */
	@param:Json(name = "audioStreamIndex")
	 val audioStreamIndex: Int? = null,
	/**
	 * Optional. The index of the video stream to use. If omitted the first video stream will be used.
	 */
	@param:Json(name = "videoStreamIndex")
	 val videoStreamIndex: Int? = null,
	/**
	 * Optional. The streaming options.
	 */
	@param:Json(name = "streamOptions")
	 val streamOptions: Map<String, String?>? = null,
): Request()
