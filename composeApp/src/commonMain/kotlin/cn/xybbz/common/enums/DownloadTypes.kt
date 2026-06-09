package cn.xybbz.common.enums

import cn.xybbz.localdata.enums.DataSourceType
import cn.xybbz.localdata.enums.DataSourceType.EMBY
import cn.xybbz.localdata.enums.DataSourceType.JELLYFIN
import cn.xybbz.localdata.enums.DataSourceType.NAVIDROME
import cn.xybbz.localdata.enums.DataSourceType.PLEX
import cn.xybbz.localdata.enums.DataSourceType.SUBSONIC

enum class DownloadTypes {

    APK,
    JELLYFIN,
    SUBSONIC,
    NAVIDROME,
    EMBY,
    PLEX
}

fun getDownloadType(dataSourceType: DataSourceType?): DownloadTypes {
    return when (dataSourceType) {
        JELLYFIN -> DownloadTypes.JELLYFIN
        SUBSONIC -> DownloadTypes.SUBSONIC
        NAVIDROME -> DownloadTypes.NAVIDROME
        EMBY -> DownloadTypes.EMBY
        PLEX -> DownloadTypes.PLEX
        null -> DownloadTypes.APK
    }
}