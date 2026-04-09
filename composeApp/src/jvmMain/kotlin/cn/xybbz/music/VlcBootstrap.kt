package cn.xybbz.music

import cn.xybbz.common.utils.Log
import java.io.File
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery

/**
 * 负责为 vlcj 配置 libvlc 的搜索路径。
 *
 * 搜索顺序优先级：
 * 1. 用户显式指定的 VLC_HOME / VLC_DIR
 * 2. Compose Desktop app resources 中的 `lib/`
 * 3. 系统默认安装目录
 * 4. PATH 中已有的目录
 */
object VlcBootstrap {

    @Volatile
    private var configured = false

    @Volatile
    private var available = false

    /**
     * 只执行一次 VLC 引导逻辑，避免重复修改 JNA 和系统属性。
     */
    fun ensureConfigured(): Boolean {
        if (configured) {
            return available
        }

        synchronized(this) {
            if (configured) {
                return available
            }

            available = configureInternal()
            configured = true
            return available
        }
    }

    /**
     * 配置 libvlc 与 plugins 搜索目录，并尝试触发 vlcj 的原生发现逻辑。
     */
    private fun configureInternal(): Boolean {
        val discovery = NativeDiscovery()
        val discovered = runCatching {
            discovery.discover()
        }.getOrElse {
            Log.e("vlc", "自动发现 VLC 失败", it)
            false
        }
        val discoveredDirectory = discovery.discoveredPath()?.let(::File)

        if (discoveredDirectory != null) {
            Log.i("vlc", "已配置 VLC 目录: ${discoveredDirectory.absolutePath}")
        }

        if (!discovered) {
            Log.i(
                "vlc",
                "未找到 VLC 本地库，请安装桌面版 VLC，或设置 VLC_HOME/VLC_DIR 指向包含 libvlc.dll 的目录"
            )
        }

        return discovered || discoveredDirectory != null
    }
}
