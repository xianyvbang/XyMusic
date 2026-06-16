package cn.xybbz.di

import cn.xybbz.config.security.ConnectionCredentialStoreFactory
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

/**
 * 连接凭据安全存储依赖模块。
 */
@Module
@Configuration
class ConnectionCredentialModule {
    /**
     * 提供平台凭据存储工厂。
     */
    @Singleton
    fun connectionCredentialStoreFactory(): ConnectionCredentialStoreFactory {
        return ConnectionCredentialStoreFactory()
    }
}
