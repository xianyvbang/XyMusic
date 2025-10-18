package cn.xybbz.api.enums.jellyfin

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


/**
 * The base item kind.
 */
@JsonClass(generateAdapter = false)
enum class BaseItemKind(
    private val serialName: String,
) {
    @Json(name = "AggregateFolder")
    AGGREGATE_FOLDER("AggregateFolder"),

    @Json(name = "Audio")
    AUDIO("Audio"),

    @Json(name = "AudioBook")
    AUDIO_BOOK("AudioBook"),

    @Json(name = "BasePluginFolder")
    BASE_PLUGIN_FOLDER("BasePluginFolder"),

    @Json(name = "Book")
    BOOK("Book"),

    @Json(name = "BoxSet")
    BOX_SET("BoxSet"),

    @Json(name = "Channel")
    CHANNEL("Channel"),

    @Json(name = "ChannelFolderItem")
    CHANNEL_FOLDER_ITEM("ChannelFolderItem"),

    @Json(name = "CollectionFolder")
    COLLECTION_FOLDER("CollectionFolder"),

    @Json(name = "Episode")
    EPISODE("Episode"),

    @Json(name = "Folder")
    FOLDER("Folder"),

    @Json(name = "Genre")
    GENRE("Genre"),

    @Json(name = "ManualPlaylistsFolder")
    MANUAL_PLAYLISTS_FOLDER("ManualPlaylistsFolder"),

    @Json(name = "Movie")
    MOVIE("Movie"),

    @Json(name = "LiveTvChannel")
    LIVE_TV_CHANNEL("LiveTvChannel"),

    @Json(name = "LiveTvProgram")
    LIVE_TV_PROGRAM("LiveTvProgram"),

    @Json(name = "MusicAlbum")
    MUSIC_ALBUM("MusicAlbum"),

    @Json(name = "MusicArtist")
    MUSIC_ARTIST("MusicArtist"),

    @Json(name = "MusicGenre")
    MUSIC_GENRE("MusicGenre"),

    @Json(name = "MusicVideo")
    MUSIC_VIDEO("MusicVideo"),

    @Json(name = "Person")
    PERSON("Person"),

    @Json(name = "Photo")
    PHOTO("Photo"),

    @Json(name = "PhotoAlbum")
    PHOTO_ALBUM("PhotoAlbum"),

    @Json(name = "Playlist")
    PLAYLIST("Playlist"),

    @Json(name = "PlaylistsFolder")
    PLAYLISTS_FOLDER("PlaylistsFolder"),

    @Json(name = "Program")
    PROGRAM("Program"),

    @Json(name = "Recording")
    RECORDING("Recording"),

    @Json(name = "Season")
    SEASON("Season"),

    @Json(name = "Series")
    SERIES("Series"),

    @Json(name = "Studio")
    STUDIO("Studio"),

    @Json(name = "Trailer")
    TRAILER("Trailer"),

    @Json(name = "TvChannel")
    TV_CHANNEL("TvChannel"),

    @Json(name = "TvProgram")
    TV_PROGRAM("TvProgram"),

    @Json(name = "UserRootFolder")
    USER_ROOT_FOLDER("UserRootFolder"),

    @Json(name = "UserView")
    USER_VIEW("UserView"),

    @Json(name = "Video")
    VIDEO("Video"),

    @Json(name = "Year")
    YEAR("Year"),
    ;

    /**
     * Get the serial name of the enum member.
     */
    override fun toString(): String = serialName
}