package cn.xybbz.config

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher

object OpenSettingStartActivity {

    var startActivity: ActivityResultLauncher<Intent>? = null

    var okFun: (() -> Unit)? = null


}