package cn.xybbz.common.enums

import androidx.annotation.StringRes
import cn.xybbz.R

/**
 * 收藏Tab
 */
enum class TabListEnum(val code: Int, @param:StringRes val message: Int) {
    Music(1, R.string.music),
    Album(3, R.string.album),
}