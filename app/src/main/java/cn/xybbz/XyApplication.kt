package cn.xybbz

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.config.BackgroundConfig
import cn.xybbz.config.connection.ConnectionConfigServer
import cn.xybbz.config.HomeDataRepository
import cn.xybbz.config.download.DownLoadManager
import cn.xybbz.config.proxy.ProxyConfigServer
import cn.xybbz.config.setting.SettingsManager
import com.hjq.language.MultiLanguages
import com.kongzue.dialogx.DialogX
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class XyApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var dataSourceManager: DataSourceManager

    @Inject
    lateinit var connectionConfigServer: ConnectionConfigServer

    @Inject
    lateinit var proxyConfigServer: ProxyConfigServer

    @Inject
    lateinit var settingsManager: SettingsManager

    @Inject
    lateinit var backgroundConfig: BackgroundConfig

    @Inject
    lateinit var downloadManager: DownLoadManager

    @Inject
    lateinit var homeDataRepository: HomeDataRepository

    override fun onCreate() {
        // 初始化语种切换框架
        super.onCreate()
        MultiLanguages.init(this)
        DialogX.init(this)
        DialogX.DEBUGMODE = true;
        DialogX.onlyOnePopTip = false
        //是否默认可以关闭
        DialogX.cancelableTipDialog = false
        DialogX.globalTheme = DialogX.THEME.DARK
        settingsManager.setSettingsData()
        backgroundConfig.load()
        proxyConfigServer.initConfig()
        downloadManager.initData()
        connectionConfigServer.initData()
        dataSourceManager.initDataSource()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}