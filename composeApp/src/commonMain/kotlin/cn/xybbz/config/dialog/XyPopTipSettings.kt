package cn.xybbz.config.dialog

import com.kongzue.dialogx.interfaces.DialogXStyle.PopTipSettings
import com.kongzue.dialogxmaterialyou.R

class XyPopTipSettings: PopTipSettings() {
    override fun layout(light: Boolean): Int {
        return if (light) R.layout.layout_dialogx_poptip_material_you else R.layout.layout_dialogx_poptip_material_you_dark
    }

    override fun align(): ALIGN {
        return ALIGN.BOTTOM
    }

    override fun enterAnimResId(light: Boolean): Int {
        return com.kongzue.dialogx.R.anim.anim_dialogx_default_enter
    }

    override fun exitAnimResId(light: Boolean): Int {
        return com.kongzue.dialogx.R.anim.anim_dialogx_default_exit
    }

    override fun tintIcon(): Boolean {
        return true
    }
}