package cn.xybbz.config.proxy

import cn.xybbz.api.client.data.ProxyConfig
import cn.xybbz.api.enums.ProxyMode
import cn.xybbz.api.okhttp.proxy.ProxyManager
import cn.xybbz.common.enums.AllDataEnum
import cn.xybbz.common.utils.CoroutineScopeUtils
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.proxy.XyProxyConfig
import kotlinx.coroutines.launch

class ProxyConfigServer(private val db: DatabaseClient) {


    val scope = CoroutineScopeUtils.getIo("ProxyConfig")
    lateinit var proxyConfig: XyProxyConfig

    fun initConfig() {
        scope.launch {
            proxyConfig = db.proxyConfigDao.getConfig() ?: XyProxyConfig(mode = ProxyMode.NONE.name)
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
        if (proxyConfig.enabled)
            ProxyManager.updateProxy(
                ProxyConfig(
                    mode = ProxyMode.getProxyMode(proxyConfig.mode),
                    host = proxyConfig.host,
                    port = proxyConfig.port,
                    username = proxyConfig.username,
                    password = proxyConfig.password
                )
            )
        else
            ProxyManager.clearProxy()
    }

    suspend fun updateEnabled(enabled: Boolean) {
        proxyConfig = get().copy(enabled = enabled)
        if (proxyConfig.id == AllDataEnum.All.code) {
            db.proxyConfigDao.updateEnabled(enabled, get().id)
        } else {
            val configId =
                db.proxyConfigDao.save(proxyConfig)
            proxyConfig = get().copy(id = configId)
        }
        updateProxyConfig()
    }

    /**
     * 更新host
     */
    suspend fun updateHost(host: String) {
        proxyConfig = get().copy(host = host)
        if (proxyConfig.id == AllDataEnum.All.code) {
            db.proxyConfigDao.updateHost(host, get().id)
        } else {
            val configId =
                db.proxyConfigDao.save(proxyConfig)
            proxyConfig = get().copy(id = configId)
        }
        updateProxyConfig()
    }

    suspend fun updatePort(port: Int) {
        proxyConfig = get().copy(port = port)
        if (proxyConfig.id == AllDataEnum.All.code) {
            db.proxyConfigDao.updatePort(port, get().id)
        } else {
            val configId =
                db.proxyConfigDao.save(proxyConfig)
            proxyConfig = get().copy(id = configId)
        }
        updateProxyConfig()
    }

    suspend fun updateUsername(username: String) {
        proxyConfig = get().copy(username = username)
        if (proxyConfig.id == AllDataEnum.All.code) {
            db.proxyConfigDao.updateUsername(username, get().id)
        } else {
            val configId =
                db.proxyConfigDao.save(proxyConfig)
            proxyConfig = get().copy(id = configId)
        }
        updateProxyConfig()
    }

    suspend fun updatePassword(password: String) {
        proxyConfig = get().copy(password = password)
        if (proxyConfig.id == AllDataEnum.All.code) {
            db.proxyConfigDao.updatePassword(password, get().id)
        } else {
            val configId =
                db.proxyConfigDao.save(proxyConfig)
            proxyConfig = get().copy(id = configId)
        }
        updateProxyConfig()
    }


}