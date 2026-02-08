package cn.xybbz.common.enums

import cn.xybbz.R
import cn.xybbz.localdata.enums.ThemeTypeEnum

fun ThemeTypeEnum.toResStringInt():Int{
    return when(this){
        ThemeTypeEnum.SYSTEM -> R.string.system
        ThemeTypeEnum.DARK -> R.string.dark
        ThemeTypeEnum.LIGHT -> R.string.light
        ThemeTypeEnum.FLOWER -> R.string.flower
    }
}