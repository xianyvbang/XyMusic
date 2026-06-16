package cn.xybbz.config.security

import cn.xybbz.localdata.data.connection.ConnectionConfig
import cn.xybbz.localdata.enums.CredentialStoreType
import com.sun.jna.platform.win32.Crypt32Util
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Singleton
import java.util.Base64
import java.util.Locale
import java.util.prefs.Preferences

/**
 * JVM 桌面端连接凭据存储工厂。
 */
@Singleton
actual class ConnectionCredentialStoreFactory actual constructor() {
    actual fun create(): ConnectionCredentialStore {
        return DesktopConnectionCredentialStore()
    }
}

/**
 * 桌面端系统安全存储实现。
 */
private class DesktopConnectionCredentialStore : ConnectionCredentialStore {
    override suspend fun currentStoreType(connectionConfig: ConnectionConfig?): CredentialStoreType {
        return CredentialStoreType.DESKTOP_KEYCHAIN
    }

    override suspend fun isAvailable(): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            when (desktopOs()) {
                DesktopOs.WINDOWS -> {
                    val probe = Crypt32Util.cryptProtectData(
                        "xymusic-probe".encodeToByteArray(),
                        WINDOWS_DPAPI_FLAGS
                    )
                    Crypt32Util.cryptUnprotectData(probe, WINDOWS_DPAPI_FLAGS)
                    true
                }

                DesktopOs.MACOS -> runCommand("security", "help").exitCode == 0
                DesktopOs.LINUX -> runCommand("secret-tool", "--help").exitCode == 0
            }
        }.getOrDefault(false)
    }

    override suspend fun save(connectionConfig: ConnectionConfig, password: String): String = withContext(Dispatchers.IO) {
        val key = connectionConfig.currentPassword.takeIf { it.isNotBlank() }
            ?: ConnectionCredentialStoreKey.build(connectionConfig)
        when (desktopOs()) {
            DesktopOs.WINDOWS -> {
                val protected = Crypt32Util.cryptProtectData(
                    password.encodeToByteArray(),
                    WINDOWS_DPAPI_FLAGS
                )
                preferences().put(key, Base64.getEncoder().encodeToString(protected))
                preferences().flush()
            }

            DesktopOs.MACOS -> {
                val result = runCommand(
                    "security",
                    "add-generic-password",
                    "-U",
                    "-a",
                    key,
                    "-s",
                    KEYCHAIN_SERVICE,
                    "-w",
                    password
                )
                ensureSuccess(result, "macOS Keychain 写入失败")
            }

            DesktopOs.LINUX -> {
                val result = runCommandWithInput(
                    password,
                    "secret-tool",
                    "store",
                    "--label=XyMusic Connection Credential",
                    "service",
                    KEYCHAIN_SERVICE,
                    "account",
                    key
                )
                ensureSuccess(result, "Linux Secret Service 写入失败")
            }
        }
        return@withContext key
    }

    override suspend fun load(connectionConfig: ConnectionConfig): String? = withContext(Dispatchers.IO) {
        when (desktopOs()) {
            DesktopOs.WINDOWS -> {
                val storedPayload = preferences().get(connectionConfig.currentPassword, null) ?: return@withContext null
                return@withContext runCatching {
                    val protected = Base64.getDecoder().decode(storedPayload)
                    Crypt32Util.cryptUnprotectData(protected, WINDOWS_DPAPI_FLAGS)
                        .decodeToString()
                }.getOrNull()
            }

            DesktopOs.MACOS -> {
                val result = runCommand(
                    "security",
                    "find-generic-password",
                    "-a",
                    connectionConfig.currentPassword,
                    "-s",
                    KEYCHAIN_SERVICE,
                    "-w"
                )
                if (result.exitCode != 0) {
                    return@withContext null
                }
                return@withContext result.stdout.trim().ifBlank { null }
            }

            DesktopOs.LINUX -> {
                val result = runCommand(
                    "secret-tool",
                    "lookup",
                    "service",
                    KEYCHAIN_SERVICE,
                    "account",
                    connectionConfig.currentPassword
                )
                if (result.exitCode != 0) {
                    return@withContext null
                }
                return@withContext result.stdout.trim().ifBlank { null }
            }
        }
    }

    override suspend fun delete(connectionConfig: ConnectionConfig): Unit = withContext(Dispatchers.IO) {
        when (desktopOs()) {
            DesktopOs.WINDOWS -> {
                preferences().remove(connectionConfig.currentPassword)
                preferences().flush()
            }

            DesktopOs.MACOS -> {
                runCommand(
                    "security",
                    "delete-generic-password",
                    "-a",
                    connectionConfig.currentPassword,
                    "-s",
                    KEYCHAIN_SERVICE
                )
            }

            DesktopOs.LINUX -> {
                runCommand(
                    "secret-tool",
                    "clear",
                    "service",
                    KEYCHAIN_SERVICE,
                    "account",
                    connectionConfig.currentPassword
                )
            }
        }
    }

    /**
     * 读取当前桌面平台类型。
     */
    private fun desktopOs(): DesktopOs {
        val osName = System.getProperty("os.name").orEmpty().lowercase(Locale.ROOT)
        return when {
            osName.contains("win") -> DesktopOs.WINDOWS
            osName.contains("mac") -> DesktopOs.MACOS
            else -> DesktopOs.LINUX
        }
    }

    /**
     * 返回当前应用使用的桌面密码偏好存储节点。
     */
    private fun preferences(): Preferences {
        return Preferences.userRoot().node(PREFERENCES_NODE)
    }

    /**
     * 运行系统命令并收集标准输出与错误输出。
     */
    private fun runCommand(vararg command: String): CommandResult {
        val process = ProcessBuilder(*command)
            .redirectErrorStream(false)
            .start()
        val stdout = process.inputStream.bufferedReader().use { it.readText() }
        val stderr = process.errorStream.bufferedReader().use { it.readText() }
        val exitCode = process.waitFor()
        return CommandResult(exitCode = exitCode, stdout = stdout, stderr = stderr)
    }

    /**
     * 向系统命令写入标准输入，并收集标准输出与错误输出。
     */
    private fun runCommandWithInput(input: String, vararg command: String): CommandResult {
        val process = ProcessBuilder(*command)
            .redirectErrorStream(false)
            .start()
        process.outputStream.bufferedWriter().use {
            it.write(input)
            it.flush()
        }
        val stdout = process.inputStream.bufferedReader().use { it.readText() }
        val stderr = process.errorStream.bufferedReader().use { it.readText() }
        val exitCode = process.waitFor()
        return CommandResult(exitCode = exitCode, stdout = stdout, stderr = stderr)
    }

    /**
     * 将失败命令统一转换成安全存储异常。
     */
    private fun ensureSuccess(result: CommandResult, message: String) {
        if (result.exitCode != 0) {
            throw CredentialStoreException("$message: ${result.stderr.ifBlank { result.stdout }}")
        }
    }

    private companion object {
        /**
         * 桌面端 Keychain / Secret Service 的服务名称。
         */
        const val KEYCHAIN_SERVICE = "cn.xybbz.connection.credentials"

        /**
         * Windows DPAPI 偏好存储节点。
         */
        const val PREFERENCES_NODE = "cn.xybbz.connection.credentials"

        /**
         * Windows DPAPI 在桌面场景下禁用 UI 提示。
         */
        const val WINDOWS_DPAPI_FLAGS = 0x1
    }
}

/**
 * 桌面平台枚举。
 */
private enum class DesktopOs {
    WINDOWS,
    MACOS,
    LINUX,
}

/**
 * 系统命令返回结果。
 */
private data class CommandResult(
    val exitCode: Int,
    val stdout: String,
    val stderr: String,
)
