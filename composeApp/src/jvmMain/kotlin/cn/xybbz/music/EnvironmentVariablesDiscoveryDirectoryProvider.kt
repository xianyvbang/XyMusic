package cn.xybbz.music

import uk.co.caprica.vlcj.factory.discovery.provider.DiscoveryDirectoryProvider
import java.io.File

/**
 * 保留对显式 VLC 目录覆盖的支持，方便本地调试或外部安装目录接入。
 */
class EnvironmentVariablesDiscoveryDirectoryProvider : DiscoveryDirectoryProvider {

    override fun priority(): Int = 0

    override fun directories(): Array<String> {
        return sequenceOf(
            System.getenv("VLC_HOME"),
            System.getenv("VLC_DIR")
        )
            .mapNotNull { it?.takeIf(String::isNotBlank) }
            .map(::File)
            .filter(File::isDirectory)
            .map(File::getAbsolutePath)
            .distinct()
            .toList()
            .toTypedArray()
    }

    override fun supported(): Boolean = true
}
