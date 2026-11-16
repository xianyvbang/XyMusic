package cn.xybbz.download.core

/**
 * 下载目录不可用原因。
 */
enum class DownloadDirectoryIssue {
    /** 当前平台不支持自定义下载目录。 */
    Unsupported,

    /** 目录不存在且无法自动创建。 */
    NotCreatable,

    /** 路径指向的是文件而不是目录。 */
    NotDirectory,

    /** 目录没有写入权限。 */
    NotWritable,

    /** 目录所在磁盘没有可用空间。 */
    NoSpace,
}

/**
 * 自定义下载目录的校验与回落入口。
 *
 * 自定义目录来自用户设置，任何不可用情况都回落到平台默认下载目录，避免阻塞下载功能。
 */
object DownloadDirectorySupport {

    /**
     * 当前平台是否支持自定义下载目录。
     */
    fun isCustomDirectorySupported(): Boolean = platformSupportsCustomDownloadDirectory()

    /**
     * 校验目录是否可用于保存下载文件。
     *
     * @param path 用户选择的目录路径。
     * @return 不可用原因；返回 null 表示目录可用。
     */
    fun checkUsable(path: String): DownloadDirectoryIssue? {
        val trimmedPath = path.trim()
        if (trimmedPath.isEmpty()) {
            return DownloadDirectoryIssue.NotCreatable
        }
        return platformCheckDownloadDirectory(trimmedPath)
    }

    /**
     * 解析实际生效的下载目录。
     *
     * @param customPath 设置里保存的自定义目录，空字符串表示使用平台默认目录。
     * @param defaultPath 平台默认下载目录。
     * @return 自定义目录可用时返回自定义目录，否则返回平台默认目录。
     */
    fun resolve(customPath: String, defaultPath: String): String {
        val trimmedPath = customPath.trim()
        if (trimmedPath.isEmpty()) {
            return defaultPath
        }
        return if (checkUsable(trimmedPath) == null) trimmedPath else defaultPath
    }
}

// 各平台自行判断目录类型、写权限与可用空间；不支持自定义目录的平台直接返回 Unsupported。
internal expect fun platformCheckDownloadDirectory(path: String): DownloadDirectoryIssue?

// 各平台声明是否具备自定义下载目录能力；Android 下载落盘走 MediaStore，暂不支持。
internal expect fun platformSupportsCustomDownloadDirectory(): Boolean
