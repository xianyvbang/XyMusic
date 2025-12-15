package cn.xybbz.api.okhttp.proxy

import cn.xybbz.api.client.data.ProxyConfig
import cn.xybbz.api.enums.ProxyMode
import java.net.Authenticator
import java.net.PasswordAuthentication

object SocksAuthenticator {

    fun apply(config: ProxyConfig) {
        if (config.mode == ProxyMode.SOCKS && config.username != null) {
            Authenticator.setDefault(object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(
                        config.username,
                        config.password!!.toCharArray()
                    )
                }
            })
        }
    }

    fun clear() {
        Authenticator.setDefault(null)
    }
}
