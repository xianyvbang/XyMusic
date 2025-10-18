package cn.xybbz.api.client.jellyfin.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Class SystemInfo.
 */
@JsonClass(generateAdapter = true)
data class SystemInfoResponse(
    /**
     * The local address.
     */
    @param:Json(name = "LocalAddress")
    val localAddress: String? = null,
    /**
     * The name of the server.
     */
    @param:Json(name = "ServerName")
    val serverName: String? = null,
    /**
     * The server version.
     */
    @param:Json(name = "Version")
    val version: String? = null,
    /**
     * The product name. This is the AssemblyProduct name.
     */
    @param:Json(name = "ProductName")
    val productName: String? = null,
    /**
     * The operating system.
     */
    @Deprecated("This member is deprecated and may be removed in the future")
    @param:Json(name = "OperatingSystem")
    val operatingSystem: String? = null,
    /**
     * The id.
     */
    @param:Json(name = "Id")
    val id: String? = null,
    /**
     * A value indicating whether the startup wizard is completed.
     */
    @param:Json(name = "StartupWizardCompleted")
    val startupWizardCompleted: Boolean? = null,
    /**
     * The package name.
     */
    @param:Json(name = "PackageName")
    val packageName: String? = null,
    /**
     * A value indicating whether this instance has pending restart.
     */
    @param:Json(name = "HasPendingRestart")
    val hasPendingRestart: Boolean,
    @param:Json(name = "IsShuttingDown")
    val isShuttingDown: Boolean,
    /**
     * A value indicating whether [supports library monitor].
     */
    @param:Json(name = "SupportsLibraryMonitor")
    val supportsLibraryMonitor: Boolean,
    /**
     * The web socket port number.
     */
    @param:Json(name = "WebSocketPortNumber")
    val webSocketPortNumber: Int,
    /**
     * The program data path.
     */
    @param:Json(name = "ProgramDataPath")
    val programDataPath: String? = null,
    /**
     * The web UI resources path.
     */
    @param:Json(name = "WebPath")
    val webPath: String? = null,
    /**
     * The items by name path.
     */
    @param:Json(name = "ItemsByNamePath")
    val itemsByNamePath: String? = null,
    /**
     * The cache path.
     */
    @param:Json(name = "CachePath")
    val cachePath: String? = null,
    /**
     * The log path.
     */
    @param:Json(name = "LogPath")
    val logPath: String? = null,
    /**
     * The internal metadata path.
     */
    @param:Json(name = "InternalMetadataPath")
    val internalMetadataPath: String? = null,
    /**
     * The transcode path.
     */
    @param:Json(name = "TranscodingTempPath")
    val transcodingTempPath: String? = null,
)
