package cn.xybbz.common.utils

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.core.content.ContextCompat


object NetWorkUtils {

    private lateinit var application: Context

    val cm by lazy {
        ContextCompat.getSystemService(
            application,
            ConnectivityManager::class.java
        ) as ConnectivityManager
    }

    fun saveApplication(context: Context) {
        application = context
    }

    /**
     * 判断网络是否连接
     * @return
     */
    fun isConnected(): Boolean {
        val info = cm.activeNetwork
        val capabilities = cm.getNetworkCapabilities(info)
        return capabilities != null
    }

    /**
     * 判断是否是wifi连接
     */
    fun isWifi(): Boolean {
        val networkCapabilities = cm.activeNetwork
        val capabilities = cm.getNetworkCapabilities(networkCapabilities)
        return capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
    }

    /**
     * 判断是否为移动网络
     */
    fun isMobile(): Boolean {
        val networkCapabilities = cm.activeNetwork
        val capabilities = cm.getNetworkCapabilities(networkCapabilities)
        return capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true
    }

    /**
     * 打开网络设置界面
     */
    fun openSetting(activity: Activity) {
        val intent = Intent("/")
        val cm = ComponentName(
            "com.android.settings",
            "com.android.settings.WirelessSettings"
        )
        intent.setComponent(cm)
        intent.setAction("android.intent.action.VIEW")
        activity.startActivityForResult(intent, 0)
    }
}