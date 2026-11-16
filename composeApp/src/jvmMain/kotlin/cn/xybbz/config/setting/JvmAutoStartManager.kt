package cn.xybbz.config.setting

import java.io.File

/**
 * JVM 桌面端开机自启管理器。
 *
 * Windows 通过写入当前用户的 Run 注册表项实现登录后自动启动，
 * 其他系统不支持时由设置页禁用对应开关。
 */
object JvmAutoStartManager {

    /** 当前用户的开机启动注册表路径。 */
    private const val RUN_REGISTRY_KEY = "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run"

    /** 注册表值名称，固定后避免重复写入多条启动项。 */
    private const val RUN_VALUE_NAME = "XyMusic"

    /** 当前系统是否支持开机自启配置。 */
    val isSupported: Boolean
        get() = System.getProperty("os.name").orEmpty().lowercase().contains("win")

    /**
     * 写入或删除开机自启注册表项。
     *
     * @param enabled true 表示写入启动项，false 表示删除启动项。
     * @return 是否操作成功；非 Windows 或无法解析启动命令时返回 false。
     */
    fun setEnabled(enabled: Boolean): Boolean {
        if (!isSupported) {
            return false
        }
        return if (enabled) {
            val command = buildLaunchCommand() ?: return false
            runRegCommand(
                "add", RUN_REGISTRY_KEY,
                "/v", RUN_VALUE_NAME,
                "/t", "REG_SZ",
                "/d", command,
                "/f",
            )
        } else {
            // 删除不存在的启动项时 reg 会返回非 0，这里再查询一次确认已关闭。
            runRegCommand("delete", RUN_REGISTRY_KEY, "/v", RUN_VALUE_NAME, "/f") ||
                !isRegistryEntryPresent()
        }
    }

    /**
     * 查询当前用户是否已经存在开机启动项。
     */
    fun isRegistryEntryPresent(): Boolean {
        if (!isSupported) {
            return false
        }
        val process = runCatching {
            ProcessBuilder("reg", "query", RUN_REGISTRY_KEY, "/v", RUN_VALUE_NAME)
                .redirectErrorStream(true)
                .start()
        }.getOrNull() ?: return false
        val output = runCatching { process.inputStream.bufferedReader().readText() }.getOrDefault("")
        val exitCode = process.waitFor()
        return exitCode == 0 && output.contains(RUN_VALUE_NAME)
    }

    /**
     * 组装开机启动命令。
     *
     * 安装包运行（jpackage）时直接使用应用启动器路径；
     * 开发环境回退到当前 JVM 的 javaw 与运行 classpath。
     */
    private fun buildLaunchCommand(): String? {
        val appPath = System.getProperty("jpackage.app-path")?.takeIf { it.isNotBlank() }
        if (appPath != null) {
            return "\"$appPath\""
        }
        val javaHome = System.getProperty("java.home")?.takeIf { it.isNotBlank() } ?: return null
        val javaExecutable = File(javaHome, "bin/javaw.exe").takeIf { it.exists() } ?: return null
        val classPath = System.getProperty("java.class.path")?.takeIf { it.isNotBlank() } ?: return null
        return "\"${javaExecutable.absolutePath}\" -cp \"$classPath\" cn.xybbz.MainKt"
    }

    /**
     * 执行 reg 命令并返回是否成功。
     */
    private fun runRegCommand(vararg args: String): Boolean {
        return runCatching {
            ProcessBuilder("reg", *args)
                .redirectErrorStream(true)
                .start()
                .waitFor() == 0
        }.getOrDefault(false)
    }
}
