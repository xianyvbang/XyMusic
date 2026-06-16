package cn.xybbz.config.security

import org.koin.core.annotation.Singleton

/**
 * 连接凭据存储工厂。
 */
@Singleton
expect class ConnectionCredentialStoreFactory() {
    /**
     * 返回当前平台使用的凭据存储实现。
     */
    fun create(): ConnectionCredentialStore
}
