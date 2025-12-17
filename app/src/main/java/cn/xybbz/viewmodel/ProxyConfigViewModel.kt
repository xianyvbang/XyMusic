package cn.xybbz.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.api.constants.ApiConstants
import cn.xybbz.common.utils.MessageUtils
import cn.xybbz.config.BackgroundConfig
import cn.xybbz.config.ConnectionConfigServer
import cn.xybbz.config.proxy.ProxyConfigServer
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.proxy.XyProxyConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.ProxySelector
import java.net.Socket
import java.net.SocketAddress
import java.net.URI
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class ProxyConfigViewModel @Inject constructor(
    val db: DatabaseClient,
    val backgroundConfig: BackgroundConfig,
    val poxyConfigServer: ProxyConfigServer,
    private val connectionConfigServer: ConnectionConfigServer,
    private val dataSourceManager: DataSourceManager
) : ViewModel() {

    var proxyConfig by mutableStateOf<XyProxyConfig?>(null)

    /**
     * 是否开启代理
     */
    var enabled by mutableStateOf(false)
        private set

    var addressValue by mutableStateOf(TextFieldValue(text = "", selection = TextRange(0)))
        private set

    init {
        getProxyConfig()
    }

    fun getProxyConfig() {
        viewModelScope.launch {
            proxyConfig = db.proxyConfigDao.getConfig()
            enabled = proxyConfig?.enabled ?: false
            val addressTmp = proxyConfig?.address ?: ""
            addressValue =
                TextFieldValue(text = addressTmp, selection = TextRange(addressTmp.length))
        }
    }

    fun updateEnabled(enabled: Boolean) {
        viewModelScope.launch {
            this@ProxyConfigViewModel.enabled = enabled
        }
    }

    fun updateAddress(address: String) {
        viewModelScope.launch {
            addressValue = TextFieldValue(text = address, selection = TextRange(address.length))
        }
    }

    fun saveConfig() {
        viewModelScope.launch {
            poxyConfigServer.updateAddressAndEnabled(addressValue.text, enabled)
        }
    }

    fun testProxyConfig() {
        viewModelScope.launch {
            val testSate = testProxy() && testUrlProxy()
            if (testSate) {
                MessageUtils.sendPopTipSuccess("测试连接成功")
            } else {
                MessageUtils.sendPopTipError("测试连接失败")
            }

        }
    }

    suspend fun testProxy(
        timeoutMs: Int = ApiConstants.DEFAULT_TIMEOUT_MILLISECONDS.toInt(),
    ): Boolean = withContext(Dispatchers.IO) {
        val addressTmp = poxyConfigServer.getAddress(addressValue.text)
        val (host, port) = poxyConfigServer.parseHostPortSafe(addressTmp)

        try {
            Socket().use {
                it.connect(InetSocketAddress(host, port), timeoutMs)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


    suspend fun testUrlProxy(): Boolean = withContext(Dispatchers.IO) {
        val address = connectionConfigServer.getAddress()
        val request = Request.Builder()
            .url(address)
            .head() // 用 HEAD，快，不下内容
            .build()
        val addressTmp = poxyConfigServer.getAddress(addressValue.text)

        val (host, port) = poxyConfigServer.parseHostPortSafe(addressTmp)

        val client = dataSourceManager.getOkhttpClient().newBuilder()
            .connectTimeout(1000, TimeUnit.MILLISECONDS)
            .proxySelector(object : ProxySelector() {
                override fun connectFailed(
                    uri: URI?,
                    sa: SocketAddress?,
                    ioe: IOException?
                ) {

                }

                override fun select(uri: URI?): List<Proxy?> {
                    return listOf(
                        Proxy(
                            Proxy.Type.HTTP,
                            InetSocketAddress(host, port)
                        ),
                        Proxy(
                            Proxy.Type.SOCKS,
                            InetSocketAddress(host, port)
                        ),
                        Proxy.NO_PROXY
                    )
                }

            }).build()
        try {
            client.newCall(request).execute().use { response ->
                response.isSuccessful || response.isRedirect
            }

        } catch (e: Exception) {
            e.printStackTrace()
            false
        }


    }


}