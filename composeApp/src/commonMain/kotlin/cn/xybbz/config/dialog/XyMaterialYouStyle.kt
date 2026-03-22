package cn.xybbz.config.dialog

import com.kongzue.dialogxmaterialyou.style.MaterialYouStyle

class XyMaterialYouStyle: MaterialYouStyle() {

    override fun popTipSettings(): PopTipSettings {
        return XyPopTipSettings()
    }
}