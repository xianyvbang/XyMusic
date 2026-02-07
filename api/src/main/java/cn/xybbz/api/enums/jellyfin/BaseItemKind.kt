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

package cn.xybbz.api.enums.jellyfin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * The base item kind.
 */
@Serializable
enum class BaseItemKind(
    private val serialName: String,
) {
    @SerialName(value = "AggregateFolder")
    AGGREGATE_FOLDER("AggregateFolder"),

    @SerialName(value = "Audio")
    AUDIO("Audio"),

    @SerialName(value = "AudioBook")
    AUDIO_BOOK("AudioBook"),

    @SerialName(value = "BasePluginFolder")
    BASE_PLUGIN_FOLDER("BasePluginFolder"),

    @SerialName(value = "Book")
    BOOK("Book"),

    @SerialName(value = "BoxSet")
    BOX_SET("BoxSet"),

    @SerialName(value = "Channel")
    CHANNEL("Channel"),

    @SerialName(value = "ChannelFolderItem")
    CHANNEL_FOLDER_ITEM("ChannelFolderItem"),

    @SerialName(value = "CollectionFolder")
    COLLECTION_FOLDER("CollectionFolder"),

    @SerialName(value = "Episode")
    EPISODE("Episode"),

    @SerialName(value = "Folder")
    FOLDER("Folder"),

    @SerialName(value = "Genre")
    GENRE("Genre"),

    @SerialName(value = "ManualPlaylistsFolder")
    MANUAL_PLAYLISTS_FOLDER("ManualPlaylistsFolder"),

    @SerialName(value = "Movie")
    MOVIE("Movie"),

    @SerialName(value = "LiveTvChannel")
    LIVE_TV_CHANNEL("LiveTvChannel"),

    @SerialName(value = "LiveTvProgram")
    LIVE_TV_PROGRAM("LiveTvProgram"),

    @SerialName(value = "MusicAlbum")
    MUSIC_ALBUM("MusicAlbum"),

    @SerialName(value = "MusicArtist")
    MUSIC_ARTIST("MusicArtist"),

    @SerialName(value = "MusicGenre")
    MUSIC_GENRE("MusicGenre"),

    @SerialName(value = "MusicVideo")
    MUSIC_VIDEO("MusicVideo"),

    @SerialName(value = "Person")
    PERSON("Person"),

    @SerialName(value = "Photo")
    PHOTO("Photo"),

    @SerialName(value = "PhotoAlbum")
    PHOTO_ALBUM("PhotoAlbum"),

    @SerialName(value = "Playlist")
    PLAYLIST("Playlist"),

    @SerialName(value = "PlaylistsFolder")
    PLAYLISTS_FOLDER("PlaylistsFolder"),

    @SerialName(value = "Program")
    PROGRAM("Program"),

    @SerialName(value = "Recording")
    RECORDING("Recording"),

    @SerialName(value = "Season")
    SEASON("Season"),

    @SerialName(value = "Series")
    SERIES("Series"),

    @SerialName(value = "Studio")
    STUDIO("Studio"),

    @SerialName(value = "Trailer")
    TRAILER("Trailer"),

    @SerialName(value = "TvChannel")
    TV_CHANNEL("TvChannel"),

    @SerialName(value = "TvProgram")
    TV_PROGRAM("TvProgram"),

    @SerialName(value = "UserRootFolder")
    USER_ROOT_FOLDER("UserRootFolder"),

    @SerialName(value = "UserView")
    USER_VIEW("UserView"),

    @SerialName(value = "Video")
    VIDEO("Video"),

    @SerialName(value = "Year")
    YEAR("Year"),
    ;

    /**
     * Get the serial name of the enum member.
     */
    override fun toString(): String = serialName
}