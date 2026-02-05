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

package cn.xybbz.api.client.plex.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PlexPingSystemResponse(
    @param:Json(name = "MediaContainer")
    val mediaContainer: MediaContainer? = null
)

@JsonClass(generateAdapter = true)
data class MediaContainer(
    val allowCameraUpload: Boolean? = null,
    val allowChannelAccess: Boolean? = null,
    /**
     * 是否可以删除
     */
    val allowMediaDeletion: Boolean? = null,
    val allowSharing: Boolean? = null,
    /**
     * 是否可以下载
     */
    val allowSync: Boolean? = null,
    val allowTuners: Boolean? = null,
    val backgroundProcessing: Boolean? = null,
    val certificate: Boolean? = null,
    val companionProxy: Boolean? = null,
    val countryCode: String? = null,
    val diagnostics: String? = null,

    @param:Json(name = "Directory")
    val directory: List<Directory>? = null,

    val eventStream: Boolean? = null,
    val friendlyName: String? = null,
    val hubSearch: Boolean? = null,
    val itemClusters: Boolean? = null,
    val livetv: Double? = null,
    val machineIdentifier: String? = null,
    val mediaProviders: Boolean? = null,
    val multiuser: Boolean? = null,
    val musicAnalysis: Double? = null,
    val myPlex: Boolean? = null,
    val myPlexMappingState: String? = null,
    val myPlexSigninState: String? = null,
    val myPlexSubscription: Boolean? = null,
    val myPlexUsername: String? = null,
    val offlineTranscode: Double? = null,
    val ownerFeatures: String? = null,
    val photoAutoTag: Boolean? = null,
    val platform: String? = null,
    val platformVersion: String? = null,
    val pluginHost: Boolean? = null,
    val pushNotifications: Boolean? = null,
    val readOnlyLibraries: Boolean? = null,
    val size: Double? = null,
    val streamingBrainABRVersion: Double? = null,
    val streamingBrainVersion: Double? = null,
    val sync: Boolean? = null,
    val transcoderActiveVideoSessions: Double? = null,
    val transcoderAudio: Boolean? = null,
    val transcoderLyrics: Boolean? = null,
    val transcoderPhoto: Boolean? = null,
    val transcoderSubtitles: Boolean? = null,
    val transcoderVideo: Boolean? = null,
    val transcoderVideoBitrates: String? = null,
    val transcoderVideoQualities: String? = null,
    val transcoderVideoResolutions: String? = null,
    val updatedAt: Double? = null,
    val updater: Boolean? = null,
    val version: String? = null,
    val voiceSearch: Boolean? = null
)