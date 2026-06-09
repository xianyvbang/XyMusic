package cn.xybbz.music

import uk.co.caprica.vlcj.factory.discovery.provider.DiscoveryDirectoryProvider
import uk.co.caprica.vlcj.factory.discovery.provider.DiscoveryProviderPriority
import java.io.File

/**
 * 参考 mediamp 的实现，直接从 Compose Desktop 的 app resources 中发现 VLC 本地库目录。
 */
class ComposeResourcesDiscoveryDirectoryProvider : DiscoveryDirectoryProvider {

    override fun priority(): Int = DiscoveryProviderPriority.USER_DIR

    override fun directories(): Array<String> {
        val path = System.getProperty("compose.application.resources.dir")
            ?.takeIf { it.isNotBlank() }
            ?: return emptyArray()
        val libs = File(path).resolve("lib")
        if (!libs.isDirectory) {
            return emptyArray()
        }
        return arrayOf(libs.absolutePath)
    }

    override fun supported(): Boolean = true
}
