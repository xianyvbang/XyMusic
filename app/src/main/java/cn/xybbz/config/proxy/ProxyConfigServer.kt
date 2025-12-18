package cn.xybbz.config.proxy

import android.webkit.URLUtil
import cn.xybbz.api.client.data.ProxyConfig
import cn.xybbz.api.okhttp.proxy.ProxyManager
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.enums.AllDataEnum
import cn.xybbz.common.utils.CoroutineScopeUtils
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.proxy.XyProxyConfig
import kotlinx.coroutines.launch
import java.net.URI

class ProxyConfigServer(private val db: DatabaseClient) {


    val scope = CoroutineScopeUtils.getIo("ProxyConfig")
    lateinit var proxyConfig: XyProxyConfig

    fun initConfig() {
        scope.launch {
            proxyConfig = db.proxyConfigDao.getConfig() ?: XyProxyConfig()
            updateProxyConfig()
        }
    }

    fun get(): XyProxyConfig {
        return proxyConfig
    }

    /**
     * 更新配置
     */
    fun updateProxyConfig() {
        if (proxyConfig.enabled) {
            val addressTmp = getAddress(proxyConfig.address)
            val parseHostPortSafe = parseHostPortSafe(address = addressTmp)
            ProxyManager.updateProxy(
                ProxyConfig(
                    enabled = proxyConfig.enabled,
                    host = parseHostPortSafe.first,
                    port = parseHostPortSafe.second
                )
            )
        } else
            ProxyManager.clearProxy()
    }

    suspend fun updateAddressAndEnabled(address: String, enabled: Boolean){
        proxyConfig = get().copy(address = address, enabled = enabled)
        if (proxyConfig.id != AllDataEnum.All.code) {
            db.proxyConfigDao.updateAddressAndEnabled(address, enabled, get().id)
        }else {
            val configId =
                db.proxyConfigDao.save(proxyConfig)
            proxyConfig = get().copy(id = configId)
        }
        updateProxyConfig()
    }

    fun parseHostPortSafe(address: String): Pair<String, Int> {
        require(address.isNotBlank()) { "address is blank" }

        val uri = URI(
            if (URLUtil.isNetworkUrl(address)) address
            else "http://$address"
        )
        require(uri.host != null && uri.port != -1) {
            "Invalid host:port format"
        }

        return uri.host to uri.port
    }

    fun getAddress(address: String): String{
        var addressTmp = address
        if (addressTmp.isBlank()) {
            addressTmp = Constants.DEFAULT_PROXY_ADDRESS
        }
        return addressTmp
    }

}