package cn.xybbz.config.security

import cn.xybbz.localdata.data.connection.ConnectionConfig
import cn.xybbz.localdata.enums.CredentialStoreType

/**
 * 连接凭据安全存储抽象。
 */
interface ConnectionCredentialStore {
    /**
     * 当前平台支持的存储类型。
     */
    suspend fun currentStoreType(connectionConfig: ConnectionConfig? = null): CredentialStoreType

    /**
     * 当前平台安全存储是否可用。
     */
    suspend fun isAvailable(): Boolean

    /**
     * 保存连接密码并返回持久化引用值。
     */
    suspend fun save(connectionConfig: ConnectionConfig, password: String): String

    /**
     * 读取连接密码。
     */
    suspend fun load(connectionConfig: ConnectionConfig): String?

    /**
     * 删除连接密码。
     */
    suspend fun delete(connectionConfig: ConnectionConfig)
}
