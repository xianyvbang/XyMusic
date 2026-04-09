package cn.xybbz.music

import cn.xybbz.common.utils.Log
import com.sun.jna.Native
import com.sun.jna.NativeLibrary
import com.sun.jna.Pointer
import com.sun.jna.WString
import com.sun.jna.win32.StdCallLibrary
import com.sun.jna.win32.W32APIOptions
import java.io.File
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery

/**
 * 负责为 vlcj 配置 libvlc 的搜索路径。
 *
 * 搜索顺序优先级：
 * 1. 应用内置并已解压的 VLC 运行时
 * 2. 用户显式指定的 VLC_HOME / VLC_DIR
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
        val bundledRuntimeDirectory = VlcBundledRuntime.extractIfBundled()
        val candidateDirectories = buildCandidateDirectories(bundledRuntimeDirectory)
        val discoveredDirectory = candidateDirectories.firstOrNull(::containsLibVlc)

        if (discoveredDirectory != null) {
            val absolutePath = discoveredDirectory.absolutePath
            // 告诉 JNA 优先从当前目录加载 libvlc。
            NativeLibrary.addSearchPath("libvlc", absolutePath)
            NativeLibrary.addSearchPath("libvlccore", absolutePath)
            System.setProperty("jna.library.path", appendLibraryPath(absolutePath))
            configureWindowsDllDirectory(discoveredDirectory)

            // VLC 的插件目录单独通过环境属性指定，确保解码/输出模块可被发现。
            val pluginDirectory = File(discoveredDirectory, "plugins")
            if (pluginDirectory.isDirectory) {
                System.setProperty("VLC_PLUGIN_PATH", pluginDirectory.absolutePath)
            }

            Log.i("vlc", "已配置 VLC 目录: $absolutePath")
        }

        val discovered = runCatching {
            NativeDiscovery().discover()
        }.getOrElse {
            Log.e("vlc", "自动发现 VLC 失败", it)
            false
        }

        if (!discovered && discoveredDirectory == null) {
            Log.i(
                "vlc",
                "未找到 VLC 本地库，请安装桌面版 VLC，或设置 VLC_HOME/VLC_DIR 指向包含 libvlc.dll 的目录"
            )
        }

        return discovered || discoveredDirectory != null
    }

    /**
     * 汇总所有候选目录，并按优先级顺序返回。
     */
    private fun buildCandidateDirectories(bundledRuntimeDirectory: File?): List<File> {
        val candidates = linkedSetOf<String>()

        fun add(path: String?) {
            if (!path.isNullOrBlank()) {
                candidates += path
            }
        }

        add(bundledRuntimeDirectory?.absolutePath)
        add(System.getenv("VLC_HOME"))
        add(System.getenv("VLC_DIR"))
        add(buildProgramFilesPath("ProgramFiles"))
        add(buildProgramFilesPath("ProgramFiles(x86)"))

        System.getenv("PATH")
            ?.split(File.pathSeparatorChar)
            ?.forEach(::add)

        return candidates.map(::File)
    }

    /**
     * 读取 Windows 常见安装目录下的 VideoLAN/VLC。
     */
    private fun buildProgramFilesPath(key: String): String? {
        val baseDir = System.getenv(key)?.takeIf { it.isNotBlank() } ?: return null
        return File(baseDir, "VideoLAN/VLC").absolutePath
    }

    /**
     * 仅当目录下存在 `libvlc.dll` 时，才认为它是有效的 VLC 根目录。
     */
    private fun containsLibVlc(directory: File): Boolean {
        return directory.isDirectory && File(directory, "libvlc.dll").isFile
    }

    /**
     * 将新目录追加到 `jna.library.path`，同时去重，避免多次初始化污染属性值。
     */
    private fun appendLibraryPath(path: String): String {
        val existing = System.getProperty("jna.library.path").orEmpty()
        return sequenceOf(existing, path)
            .filter { it.isNotBlank() }
            .flatMap { it.split(File.pathSeparatorChar).asSequence() }
            .distinct()
            .joinToString(File.pathSeparator)
    }

    /**
     * Windows 下仅设置 `jna.library.path` 不足以让插件解析它们的二级依赖；
     * 这里显式把 VLC 根目录加入系统 DLL 搜索目录，避免插件扫描时命中 error 126。
     */
    private fun configureWindowsDllDirectory(directory: File) {
        if (!isWindows()) {
            return
        }

        val absolutePath = directory.absolutePath
        runCatching {
            val kernel32 = WindowsKernel32.instance
            val addDirectoryEnabled = kernel32.SetDefaultDllDirectories(
                WindowsKernel32.LOAD_LIBRARY_SEARCH_DEFAULT_DIRS or
                    WindowsKernel32.LOAD_LIBRARY_SEARCH_USER_DIRS
            )

            val configured = if (addDirectoryEnabled) {
                kernel32.AddDllDirectory(WString(absolutePath)) != null
            } else {
                kernel32.SetDllDirectoryW(WString(absolutePath))
            }

            if (!configured) {
                Log.i("vlc", "注册 Windows DLL 搜索目录失败: $absolutePath, error=${Native.getLastError()}")
            }
        }.onFailure {
            Log.e("vlc", "配置 Windows DLL 搜索目录失败", it)
        }
    }

    private fun isWindows(): Boolean =
        System.getProperty("os.name").orEmpty().startsWith("Windows", ignoreCase = true)

    private interface Kernel32Library : StdCallLibrary {
        fun SetDefaultDllDirectories(directoryFlags: Int): Boolean

        fun AddDllDirectory(newDirectory: WString): Pointer?

        fun SetDllDirectoryW(pathName: WString?): Boolean
    }

    private object WindowsKernel32 {
        const val LOAD_LIBRARY_SEARCH_DEFAULT_DIRS = 0x00001000
        const val LOAD_LIBRARY_SEARCH_USER_DIRS = 0x00000400

        val instance: Kernel32Library by lazy {
            Native.load("kernel32", Kernel32Library::class.java, W32APIOptions.DEFAULT_OPTIONS)
        }
    }
}
