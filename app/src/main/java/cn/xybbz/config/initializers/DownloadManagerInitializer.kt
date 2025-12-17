package cn.xybbz.config.initializers

import android.content.Context
import androidx.startup.Initializer
import cn.xybbz.config.download.DownLoadManager
import cn.xybbz.config.module.AppInitEntryPoint
import dagger.hilt.android.EntryPointAccessors

class DownloadManagerInitializer: Initializer<DownLoadManager> {
    override fun create(context: Context): DownLoadManager {
        val entryPoint = EntryPointAccessors.fromApplication(
            context,
            AppInitEntryPoint::class.java
        )
        return entryPoint.downLoadManager()
    }

    override fun dependencies(): List<Class<out Initializer<*>?>?> {
        return emptyList()
    }
}