package cn.xybbz.common.enums

import cn.xybbz.localdata.enums.DataSourceType
import org.jetbrains.compose.resources.DrawableResource
import org.koin.core.qualifier.Qualifier
import org.koin.core.qualifier.named
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.emby_logo_24
import xymusic_kmp.composeapp.generated.resources.icon_jellyfin
import xymusic_kmp.composeapp.generated.resources.navidrome_icon
import xymusic_kmp.composeapp.generated.resources.plex_logos
import xymusic_kmp.composeapp.generated.resources.subsonic_logo

val DataSourceType.img: DrawableResource
    get() = when (this) {
        DataSourceType.JELLYFIN -> Res.drawable.icon_jellyfin
        DataSourceType.SUBSONIC -> Res.drawable.subsonic_logo
        DataSourceType.NAVIDROME -> Res.drawable.navidrome_icon
        DataSourceType.EMBY -> Res.drawable.emby_logo_24
        DataSourceType.PLEX -> Res.drawable.plex_logos
    }



object DataSourceQualifiers {
    const val JELLYFIN = "JELLYFIN"
    const val SUBSONIC = "SUBSONIC"
    const val NAVIDROME = "NAVIDROME"
    const val EMBY = "EMBY"
    const val PLEX = "PLEX"
}

val DataSourceType.koinQualifierName: String
    get() = when (this) {
        DataSourceType.JELLYFIN -> DataSourceQualifiers.JELLYFIN
        DataSourceType.SUBSONIC -> DataSourceQualifiers.SUBSONIC
        DataSourceType.NAVIDROME -> DataSourceQualifiers.NAVIDROME
        DataSourceType.EMBY -> DataSourceQualifiers.EMBY
        DataSourceType.PLEX -> DataSourceQualifiers.PLEX
    }

fun DataSourceType.koinQualifier(): Qualifier = named(koinQualifierName)