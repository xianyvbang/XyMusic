package cn.xybbz.common.enums

import cn.xybbz.R
import cn.xybbz.localdata.enums.DataSourceType

val DataSourceType.img: Int
    get() = when (this) {
        DataSourceType.JELLYFIN -> R.drawable.icon_jellyfin
        DataSourceType.SUBSONIC -> R.drawable.subsonic_logo
        DataSourceType.NAVIDROME -> R.drawable.navidrome_icon
        DataSourceType.EMBY -> R.drawable.emby_logo_24
        DataSourceType.PLEX -> R.drawable.plex_logos
    }