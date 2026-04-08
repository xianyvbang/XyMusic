package cn.xybbz.music

import cn.xybbz.common.constants.Constants
import cn.xybbz.common.utils.Log
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.UUID
import java.util.zip.ZipInputStream

/**
 * 负责读取桌面应用内置的 VLC 运行时压缩包，并在首次启动时解压到本地缓存目录。
 *
 * 这样用户无需手动安装 VLC，本应用也能优先加载自带的 libvlc/native plugins。
 */
object VlcBundledRuntime {

    /**
     * 打包进桌面应用 resources 的 VLC 运行时压缩包路径。
     */
    private const val ARCHIVE_RESOURCE_PATH = "vlc/windows-x64/vlc-runtime.zip"

    /**
     * 若应用资源里携带了 VLC 运行时，则解压并返回包含 `libvlc.dll` 的目录。
     *
     * 返回 null 表示当前构建未携带运行时，或解压失败。
     */
    fun extractIfBundled(): File? {
        val resourceUrl = resourceUrl(ARCHIVE_RESOURCE_PATH) ?: return null
        val archiveStream = resourceUrl.openStream()
        archiveStream.use { input ->
            // 用资源大小 + 修改时间构造一个稳定指纹，便于按版本缓存解压结果。
            val resourceFingerprint = resourceUrl.openConnection().run {
                "${contentLengthLong}-${lastModified}"
            }
            val runtimeBaseDirectory = File(resolveRuntimeCacheDirectory(), resourceFingerprint)
            findRuntimeRoot(runtimeBaseDirectory)?.let { return it }

            // 先解压到临时目录，避免解压一半时留下脏目录。
            val tempDirectory = File(
                runtimeBaseDirectory.parentFile,
                "${runtimeBaseDirectory.name}.tmp-${UUID.randomUUID()}"
            )

            runCatching {
                unzip(input, tempDirectory)
                if (!tempDirectory.exists()) {
                    return null
                }

                // 只有目标目录还不存在时才原子移动，避免重复启动时互相覆盖。
                if (!runtimeBaseDirectory.exists()) {
                    Files.move(
                        tempDirectory.toPath(),
                        runtimeBaseDirectory.toPath(),
                        StandardCopyOption.REPLACE_EXISTING
                    )
                }
            }.onFailure {
                tempDirectory.deleteRecursively()
                Log.e("vlc", "解压内置 VLC 运行时失败", it)
                return null
            }

            tempDirectory.deleteRecursively()
            return findRuntimeRoot(runtimeBaseDirectory)
        }
    }

    /**
     * 统一将解压结果放在 LocalAppData 下，避免污染安装目录，也方便后续升级替换。
     */
    private fun resolveRuntimeCacheDirectory(): File {
        val baseDir = System.getenv("LOCALAPPDATA")
            ?.takeIf { it.isNotBlank() }
            ?.let(::File)
            ?: File(System.getProperty("user.home"), "AppData/Local")
        return File(baseDir, "${Constants.APP_NAME}/runtime/vlc/windows-x64").apply {
            mkdirs()
        }
    }

    /**
     * 在解压目录中搜索 `libvlc.dll` 所在目录，作为后续 native 加载根目录。
     */
    private fun findRuntimeRoot(baseDirectory: File): File? {
        if (!baseDirectory.exists()) {
            return null
        }
        return baseDirectory.walkTopDown()
            .firstOrNull { it.isFile && it.name.equals("libvlc.dll", ignoreCase = true) }
            ?.parentFile
    }

    /**
     * 将 zip 内容完整释放到目标目录，并保留 VLC 原有目录结构。
     */
    private fun unzip(input: java.io.InputStream, targetDirectory: File) {
        targetDirectory.deleteRecursively()
        targetDirectory.mkdirs()

        ZipInputStream(input.buffered()).use { zipInput ->
            var entry = zipInput.nextEntry
            while (entry != null) {
                val outputFile = File(targetDirectory, entry.name)
                ensureZipEntryInsideTarget(targetDirectory, outputFile)
                if (entry.isDirectory) {
                    outputFile.mkdirs()
                } else {
                    outputFile.parentFile?.mkdirs()
                    outputFile.outputStream().buffered().use { output ->
                        zipInput.copyTo(output)
                    }
                }
                zipInput.closeEntry()
                entry = zipInput.nextEntry
            }
        }
    }

    /**
     * 防止恶意 zip 条目通过 `../` 等路径逃逸到目标目录外。
     */
    private fun ensureZipEntryInsideTarget(targetDirectory: File, outputFile: File) {
        val targetPath = targetDirectory.canonicalFile.toPath()
        val outputPath = outputFile.canonicalFile.toPath()
        if (!outputPath.startsWith(targetPath)) {
            throw IOException("Illegal zip entry: ${outputFile.path}")
        }
    }

    /**
     * 同时兼容主线程上下文 ClassLoader 和当前类加载器。
     */
    private fun resourceUrl(path: String) =
        Thread.currentThread().contextClassLoader?.getResource(path)
            ?: VlcBundledRuntime::class.java.classLoader?.getResource(path)
}
