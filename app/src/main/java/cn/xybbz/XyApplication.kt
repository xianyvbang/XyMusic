package cn.xybbz

import android.app.Application
import com.hjq.language.LocaleContract
import com.hjq.language.MultiLanguages
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class XyApplication : Application() {
    override fun onCreate() {
        // 初始化语种切换框架
        super.onCreate()
        MultiLanguages.init(this)
    }
}