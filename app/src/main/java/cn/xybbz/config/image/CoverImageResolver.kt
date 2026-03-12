package cn.xybbz.config.image

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import cn.xybbz.api.client.custom.CustomMediaApiClient
import cn.xybbz.api.client.custom.data.CustomCoverQuery
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.localdata.data.album.XyAlbum
import cn.xybbz.localdata.data.artist.XyArtist
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.data.music.XyPlayMusic
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

data class CoverImageUrls(
    val primaryUrl: String?,
    val fallbackUrl: String?
)

@Singleton
class CoverImageResolver @Inject constructor(
    private val settingsManager: SettingsManager,
    private val customMediaApiClient: CustomMediaApiClient
) {

    fun resolveMusic(music: XyMusic?): CoverImageUrls {
        return resolveMusic(
            serviceUrl = music?.pic,
            title = music?.name,
            album = music?.albumName,
            artist = music?.artists?.joinToString("/")
        )
    }

    fun resolveMusic(music: XyPlayMusic?): CoverImageUrls {
        return resolveMusic(
            serviceUrl = music?.pic,
            title = music?.name,
            album = null,
            artist = music?.artists?.joinToString("/")
        )
    }

    fun resolveAlbum(album: XyAlbum?): CoverImageUrls {
        return resolveByPreference(
            serviceUrl = album?.pic,
            customUrl = buildCustomCoverUrl(
                album = album?.name,
                artist = album?.artists
            )
        )
    }

    fun resolveArtist(artist: XyArtist?): CoverImageUrls {
        return resolveByPreference(
            serviceUrl = artist?.pic,
            customUrl = buildCustomCoverUrl(artist = artist?.name)
        )
    }

    fun resolveArtistBackdrop(artist: XyArtist?): CoverImageUrls {
        return resolveByPreference(
            serviceUrl = artist?.backdrop,
            customUrl = buildCustomCoverUrl(artist = artist?.name)
        )
    }

    private fun resolveMusic(
        serviceUrl: String?,
        title: String?,
        album: String?,
        artist: String?
    ): CoverImageUrls {
        return resolveByPreference(
            serviceUrl = serviceUrl,
            customUrl = buildCustomCoverUrl(
                musicTitle = title,
                album = album,
                artist = artist
            )
        )
    }

    private fun resolveByPreference(
        serviceUrl: String?,
        customUrl: String?
    ): CoverImageUrls {
        val normalizedServiceUrl = serviceUrl.normalizeCoverUrl()
        val normalizedCustomUrl = customUrl.normalizeCoverUrl()
        val ifPriorityMusicApi = settingsManager.get().ifPriorityMusicApi

        val primaryUrl = if (ifPriorityMusicApi) {
            normalizedServiceUrl ?: normalizedCustomUrl
        } else {
            normalizedCustomUrl ?: normalizedServiceUrl
        }

        val fallbackUrl = when (primaryUrl) {
            normalizedServiceUrl -> normalizedCustomUrl
            normalizedCustomUrl -> normalizedServiceUrl
            else -> null
        }

        return CoverImageUrls(
            primaryUrl = primaryUrl,
            fallbackUrl = fallbackUrl
        )
    }

    private fun buildCustomCoverUrl(
        musicTitle: String? = null,
        album: String? = null,
        artist: String? = null
    ): String? {
        val settings = settingsManager.get()
        val customCoverApi = settings.customCoverApi.trim()
        if (customCoverApi.isBlank()) {
            return null
        }
        return customMediaApiClient.getCoverUrl(
            query = CustomCoverQuery(
                coverApi = customCoverApi,
                authKey = settings.customLrcApiAuth,
                title = musicTitle,
                artist = artist,
                album = album
            )
        )
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface CoverImageEntryPoint {
    fun coverImageResolver(): CoverImageResolver
}

@Composable
fun rememberMusicCoverUrls(music: XyMusic?): CoverImageUrls {
    val resolver = rememberCoverImageResolver()
    val artists = music?.artists?.joinToString("/")
    return remember(music?.itemId, music?.pic, music?.name, music?.albumName, artists) {
        resolver.resolveMusic(music)
    }
}

@Composable
fun rememberPlayMusicCoverUrls(music: XyPlayMusic?): CoverImageUrls {
    val resolver = rememberCoverImageResolver()
    val artists = music?.artists?.joinToString("/")
    return remember(music?.itemId, music?.pic, music?.name, artists) {
        resolver.resolveMusic(music)
    }
}

@Composable
fun rememberAlbumCoverUrls(album: XyAlbum?): CoverImageUrls {
    val resolver = rememberCoverImageResolver()
    return remember(album?.itemId, album?.pic, album?.name, album?.artists) {
        resolver.resolveAlbum(album)
    }
}

@Composable
fun rememberArtistCoverUrls(artist: XyArtist?): CoverImageUrls {
    val resolver = rememberCoverImageResolver()
    return remember(artist?.artistId, artist?.pic, artist?.name) {
        resolver.resolveArtist(artist)
    }
}

@Composable
fun rememberArtistBackdropCoverUrls(artist: XyArtist?): CoverImageUrls {
    val resolver = rememberCoverImageResolver()
    return remember(artist?.artistId, artist?.backdrop, artist?.name) {
        resolver.resolveArtistBackdrop(artist)
    }
}

@Composable
private fun rememberCoverImageResolver(): CoverImageResolver {
    val context = LocalContext.current.applicationContext
    return remember(context) {
        context.coverImageResolver()
    }
}

private fun Context.coverImageResolver(): CoverImageResolver {
    return EntryPointAccessors.fromApplication(this, CoverImageEntryPoint::class.java)
        .coverImageResolver()
}

private fun String?.normalizeCoverUrl(): String? {
    return this?.trim()?.takeIf { it.isNotBlank() }
}
