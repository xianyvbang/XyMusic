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

package cn.xybbz.api.client.jellyfin.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Class SystemInfo.
 */
@Serializable
data class SystemInfoResponse(
    /**
     * The local address.
     */
    @SerialName(value= "LocalAddress")
    val localAddress: String? = null,
    /**
     * The name of the server.
     */
    @SerialName(value= "ServerName")
    val serverName: String? = null,
    /**
     * The server version.
     */
    @SerialName(value= "Version")
    val version: String? = null,
    /**
     * The product name. This is the AssemblyProduct name.
     */
    @SerialName(value= "ProductName")
    val productName: String? = null,
    /**
     * The operating system.
     */
    @Deprecated("This member is deprecated and may be removed in the future")
    @SerialName(value= "OperatingSystem")
    val operatingSystem: String? = null,
    /**
     * The id.
     */
    @SerialName(value= "Id")
    val id: String? = null,
    /**
     * A value indicating whether the startup wizard is completed.
     */
    @SerialName(value= "StartupWizardCompleted")
    val startupWizardCompleted: Boolean? = null,
    /**
     * The package name.
     */
    @SerialName(value= "PackageName")
    val packageName: String? = null,
    /**
     * A value indicating whether this instance has pending restart.
     */
    @SerialName(value= "HasPendingRestart")
    val hasPendingRestart: Boolean,
    @SerialName(value= "IsShuttingDown")
    val isShuttingDown: Boolean,
    /**
     * A value indicating whether [supports library monitor].
     */
    @SerialName(value= "SupportsLibraryMonitor")
    val supportsLibraryMonitor: Boolean,
    /**
     * The web socket port number.
     */
    @SerialName(value= "WebSocketPortNumber")
    val webSocketPortNumber: Int,
    /**
     * The program data path.
     */
    @SerialName(value= "ProgramDataPath")
    val programDataPath: String? = null,
    /**
     * The web UI resources path.
     */
    @SerialName(value= "WebPath")
    val webPath: String? = null,
    /**
     * The items by name path.
     */
    @SerialName(value= "ItemsByNamePath")
    val itemsByNamePath: String? = null,
    /**
     * The cache path.
     */
    @SerialName(value= "CachePath")
    val cachePath: String? = null,
    /**
     * The log path.
     */
    @SerialName(value= "LogPath")
    val logPath: String? = null,
    /**
     * The internal metadata path.
     */
    @SerialName(value= "InternalMetadataPath")
    val internalMetadataPath: String? = null,
    /**
     * The transcode path.
     */
    @SerialName(value= "TranscodingTempPath")
    val transcodingTempPath: String? = null,
)
