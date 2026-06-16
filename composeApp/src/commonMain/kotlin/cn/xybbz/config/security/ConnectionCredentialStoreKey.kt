package cn.xybbz.config.security

import cn.xybbz.localdata.data.connection.ConnectionConfig

/**
 * 连接凭据条目标识工具。
 */
object ConnectionCredentialStoreKey {
    /**
     * 生成稳定的安全存储条目名。
     */
    fun build(connectionConfig: ConnectionConfig): String {
        return buildString {
            append("xymusic.connection.")
            append(connectionConfig.type.name.lowercase())
            append(".")
            append(connectionConfig.id.takeIf { it > 0 } ?: connectionConfig.address.hashCode())
            append(".")
            append(connectionConfig.username.ifBlank { "anonymous" })
        }
    }
}
