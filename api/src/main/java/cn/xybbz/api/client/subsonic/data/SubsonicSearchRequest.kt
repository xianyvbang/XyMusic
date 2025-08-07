package cn.xybbz.api.client.subsonic.data

import cn.xybbz.api.client.data.Request
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SubsonicSearchRequest(
    /**
     * Search query.
     */
    val query:String,
    /**
     * Maximum number of artists to return.
     */
    val artistCount: Int = 20,
    /**
     * Search result offset for artists. Used for paging.
     */
    val artistOffset: Int = 0,
    /**
     * Maximum number of albums to return.
     */
    val albumCount: Int = 20,
    /**
     * Search result offset for albums. Used for paging.
     */
    val albumOffset: Int = 0,
    /**
     * Maximum number of songs to return.
     */
    val songCount: Int = 20,
    /**
     * Search result offset for songs. Used for paging.
     */
    val songOffset: Int = 0,
    /**
     * 	(Since 1.12.0) Only return results from music folder with the given ID. See getMusicFolders.
     */
    val musicFolderId: String? = null
) : Request()