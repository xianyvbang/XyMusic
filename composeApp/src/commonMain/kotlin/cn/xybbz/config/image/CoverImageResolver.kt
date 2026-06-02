package cn.xybbz.config.image

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import cn.xybbz.api.AuthenticatedRequestState
import cn.xybbz.api.TokenServer
import cn.xybbz.api.client.custom.CustomMediaApiClient
import cn.xybbz.api.client.custom.data.CustomCoverQuery
import cn.xybbz.common.constants.Constants.HTTP
import cn.xybbz.common.constants.Constants.HTTPS
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.localdata.data.album.XyAlbum
import cn.xybbz.localdata.data.artist.XyArtist
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.data.music.XyPlayMusic
import io.ktor.http.URLBuilder
import io.ktor.util.appendAll
import kotlinx.coroutines.flow.StateFlow
import org.koin.compose.koinInject

data class CoverImageUrls(
    val primaryUrl: String?,
    val fallbackUrl: String?
)

class CoverImageResolver(
    private val settingsManager: SettingsManager,
    private val customMediaApiClient: CustomMediaApiClient
) {
    val baseUrlFlow: StateFlow<String?> = settingsManager.baseUrl

    fun resolveRaw(primaryUrl: String?, fallbackUrl: String? = null): CoverImageUrls {
        val baseUrl = settingsManager.baseUrl.value
        val normalizedPrimaryUrl = primaryUrl.normalizeCoverUrl(baseUrl)
        val normalizedFallbackUrl = fallbackUrl.normalizeCoverUrl(baseUrl)
        return CoverImageUrls(
            primaryUrl = normalizedPrimaryUrl,
            fallbackUrl = normalizedFallbackUrl?.takeIf { it != normalizedPrimaryUrl }
        )
    }

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
            album = music?.albumName,
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
        val baseUrl = settingsManager.baseUrl.value
        val normalizedServiceUrl = serviceUrl.normalizeCoverUrl(baseUrl)
        val normalizedCustomUrl = customUrl.normalizeCoverUrl(baseUrl)
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

interface CoverImageEntryPoint {
    fun coverImageResolver(): CoverImageResolver
}

@Composable
fun rememberMusicCoverUrls(music: XyMusic?): CoverImageUrls {
    val resolver = rememberCoverImageResolver()
    val baseUrl = currentCoverImageBaseUrl(resolver)
    val authState = currentCoverImageAuthState()
    val artists = music?.artists?.joinToString("/")
    return remember(music?.itemId, music?.pic, music?.name, music?.albumName, artists, baseUrl, authState) {
        resolver.resolveWhenAuthReady(authState.ready) {
            resolveMusic(music)
        }
    }
}

@Composable
fun rememberPlayMusicCoverUrls(music: XyPlayMusic?, refreshKey: Any? = null): CoverImageUrls {
    val resolver = rememberCoverImageResolver()
    val baseUrl = currentCoverImageBaseUrl(resolver)
    val authState = currentCoverImageAuthState()
    val artists = music?.artists?.joinToString("/")
    return remember(music?.itemId, music?.pic, music?.name, artists, baseUrl, authState, refreshKey) {
        resolver.resolveWhenAuthReady(authState.ready) {
            resolveMusic(music)
        }
    }
}

@Composable
fun rememberAlbumCoverUrls(album: XyAlbum?, albumPic: String? = null): CoverImageUrls {
    val resolver = rememberCoverImageResolver()
    val baseUrl = currentCoverImageBaseUrl(resolver)
    val authState = currentCoverImageAuthState()
    return remember(album?.itemId, albumPic ?: album?.pic, album?.name, album?.artists, baseUrl, authState) {
        resolver.resolveWhenAuthReady(authState.ready) {
            resolveAlbum(album)
        }
    }
}

@Composable
fun rememberArtistCoverUrls(artist: XyArtist?): CoverImageUrls {
    val resolver = rememberCoverImageResolver()
    val baseUrl = currentCoverImageBaseUrl(resolver)
    val authState = currentCoverImageAuthState()
    return remember(artist?.artistId, artist?.pic, artist?.name, baseUrl, authState) {
        resolver.resolveWhenAuthReady(authState.ready) {
            resolveArtist(artist)
        }
    }
}

@Composable
fun rememberArtistBackdropCoverUrls(artist: XyArtist?): CoverImageUrls {
    val resolver = rememberCoverImageResolver()
    val baseUrl = currentCoverImageBaseUrl(resolver)
    val authState = currentCoverImageAuthState()
    return remember(artist?.artistId, artist?.backdrop, artist?.name, baseUrl, authState) {
        resolver.resolveWhenAuthReady(authState.ready) {
            resolveArtistBackdrop(artist)
        }
    }
}

@Composable
fun rememberRawCoverUrls(primaryUrl: String?, fallbackUrl: String? = null): CoverImageUrls {
    val resolver = rememberCoverImageResolver()
    val baseUrl = currentCoverImageBaseUrl(resolver)
    val authState = currentCoverImageAuthState()
    return remember(primaryUrl, fallbackUrl, baseUrl, authState) {
        resolver.resolveWhenAuthReady(authState.ready) {
            resolveRaw(primaryUrl, fallbackUrl)
        }
    }
}

@Composable
private fun currentCoverImageBaseUrl(resolver: CoverImageResolver): String? {
    return resolver.baseUrlFlow.collectAsState().value
}

@Composable
private fun currentCoverImageAuthState(): AuthenticatedRequestState {
    // 认证参数未就绪时只显示占位图；参数变化时通过 version 触发封面 URL 重算。
    return TokenServer.authenticatedRequestStateFlow.collectAsState().value
}

@Composable
private fun rememberCoverImageResolver(): CoverImageResolver {
    val context = koinInject<CoverImageResolver>()
    return remember {
        context
    }
}

fun String?.normalizeCoverUrl(baseUrl: String?): String? {
    val normalizedValue = this?.trim()?.takeIf { it.isNotBlank() } ?: return null
    val imageUrl = if (normalizedValue.isAbsoluteNetworkUrl()) {
        normalizedValue
    } else {
        baseUrl.orEmpty() + normalizedValue
    }
    val urlBuilder = URLBuilder(imageUrl)
    urlBuilder.parameters.appendAll(TokenServer.queryMap)
    return urlBuilder.buildString()
}

fun String.isAbsoluteNetworkUrl(): Boolean {
    return startsWith(HTTP, ignoreCase = true) ||
            startsWith(HTTPS, ignoreCase = true)
}

private fun CoverImageResolver.resolveWhenAuthReady(
    authReady: Boolean,
    resolve: CoverImageResolver.() -> CoverImageUrls
): CoverImageUrls {
    return if (authReady) {
        resolve()
    } else {
        // 返回空模型让 XyImage 保持占位图，避免启动期带空 query/header 抢先加载失败。
        CoverImageUrls(primaryUrl = null, fallbackUrl = null)
    }
}
