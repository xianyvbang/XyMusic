package cn.xybbz.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.common.utils.MessageUtils
import cn.xybbz.config.proxy.ProxyConfigServer
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.proxy.XyProxyConfig
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpMethod
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.annotation.KoinViewModel
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.test_connection_failed
import xymusic_kmp.composeapp.generated.resources.test_connection_success

@KoinViewModel
class ProxyConfigViewModel(
    val db: DatabaseClient,
    val poxyConfigServer: ProxyConfigServer,
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
            val testSate = testUrlProxy()
            if (testSate) {
                MessageUtils.sendPopTipSuccess(Res.string.test_connection_success)
            } else {
                MessageUtils.sendPopTipError(Res.string.test_connection_failed)
            }

        }
    }


    suspend fun testUrlProxy(): Boolean = withContext(Dispatchers.IO) {
        val httpClient = dataSourceManager.getHttpClient()


        try {
            val response: HttpResponse = httpClient.request("/") {
                method = HttpMethod.Head
            }
            response.status.isSuccess()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


    /**
     * 获取连接地址
     */
    fun getConnectionAddress(): String{
        return dataSourceManager.getConnectionAddress()
    }

}
