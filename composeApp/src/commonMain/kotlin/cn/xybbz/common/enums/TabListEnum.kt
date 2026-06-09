package cn.xybbz.common.enums

import org.jetbrains.compose.resources.StringResource
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.album
import xymusic_kmp.composeapp.generated.resources.music
import xymusic_kmp.composeapp.generated.resources.resemblance_artist

/**
 * 收藏Tab
 */
enum class TabListEnum(val code: Int, val message: StringResource) {
    Music(1, Res.string.music),
    Album(3, Res.string.album),

    RESEMBLANCE_ARTIST(4,Res.string.resemblance_artist)
}