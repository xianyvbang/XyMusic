package cn.xybbz.common.enums

import cn.xybbz.localdata.enums.DataSourceType
import org.jetbrains.compose.resources.DrawableResource
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