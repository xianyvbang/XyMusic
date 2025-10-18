package cn.xybbz.api.client.subsonic.data

/**
 * SearchResult3，searchResult3
 */
data class SearchResult3(
    /**
     * Matching albums
     */
    val album: List<AlbumID3>? = null,

    /**
     * Matching artists
     */
    val artist: List<ArtistID3>? = null,

    /**
     * Matching songs
     */
    val song: List<SongID3>? = null
)
