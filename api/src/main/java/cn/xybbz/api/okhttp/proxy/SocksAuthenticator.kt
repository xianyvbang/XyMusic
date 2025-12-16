package cn.xybbz.api.okhttp.proxy

import cn.xybbz.api.client.data.ProxyConfig
import java.net.Authenticator
import java.net.PasswordAuthentication

object SocksAuthenticator {

    fun apply(config: ProxyConfig) {
        if (config.username != null && config.password != null) {
            Authenticator.setDefault(object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(
                        config.username,
                        config.password.toCharArray()
                    )
                }
            })
        }
    }

    fun apply(username:String? = null,password:String? = null) {
        if (username != null && password != null) {
            Authenticator.setDefault(object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(
                        username,
                        password.toCharArray()
                    )
                }
            })
        }
    }

    fun clear() {
        Authenticator.setDefault(null)
    }
}
