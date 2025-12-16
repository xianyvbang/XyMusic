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
import cn.xybbz.api.okhttp.proxy.SocksAuthenticator
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
import okhttp3.Credentials
import okhttp3.Request
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.ProxySelector
import java.net.Socket
import java.net.SocketAddress
import java.net.URI
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

    var usernameValue by mutableStateOf(TextFieldValue(text = "", selection = TextRange(0)))
        private set

    var passwordValue by mutableStateOf(TextFieldValue(text = "", selection = TextRange(0)))
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
            val usernameTmp = proxyConfig?.username ?: ""
            usernameValue =
                TextFieldValue(text = usernameTmp, selection = TextRange(usernameTmp.length))
            val passwordTmp = proxyConfig?.password ?: ""
            passwordValue =
                TextFieldValue(text = passwordTmp, selection = TextRange(passwordTmp.length))
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


    fun updateUsername(username: String) {
        viewModelScope.launch {
            usernameValue =
                TextFieldValue(text = username, selection = TextRange(username.length))
        }
    }

    fun updatePassword(password: String) {
        viewModelScope.launch {
            passwordValue =
                TextFieldValue(text = password, selection = TextRange(password.length))
        }
    }

    fun saveConfig() {
        viewModelScope.launch {
            //TODO 改成统一传入,而不是每个都update一下配置
            poxyConfigServer.updateEnabled(enabled)
            poxyConfigServer.updateAddress(addressValue.text)
            poxyConfigServer.updateUsername(usernameValue.text)
            poxyConfigServer.updatePassword(passwordValue.text)
        }
    }

    //todo 测试的okhttp需要自己创建?
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
        val username = usernameValue.text
        val password = passwordValue.text
        SocksAuthenticator.apply(username, password)

        val addressTmp = poxyConfigServer.getAddress(addressValue.text)

        val (host, port) = poxyConfigServer.parseHostPortSafe(addressTmp)

        val client = dataSourceManager.getOkhttpClient().newBuilder()
            .proxySelector(object :ProxySelector() {
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

            })
            .proxyAuthenticator { _, response ->
                if (username.isNotBlank() && password.isNotBlank()) {
                    val credential = Credentials.basic(username, password)
                    response.request.newBuilder()
                        .header(ApiConstants.PROXY_AUTHORIZATION, credential)
                        .build()
                } else {
                    response.request
                }
            }.build()
        try {
            client.newCall(request).execute().use { response ->
                response.isSuccessful || response.isRedirect
            }

        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            //todo 因为测试有延迟,可能删除的时候把保存的代理验证的数据删除了
            SocksAuthenticator.clear()
        }


    }


}