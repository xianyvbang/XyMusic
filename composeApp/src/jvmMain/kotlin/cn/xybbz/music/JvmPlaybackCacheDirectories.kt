package cn.xybbz.music

import cn.xybbz.platform.ContextWrapper
import cn.xybbz.platform.PlatformCacheDirectoryNames
import java.io.File

/**
 * JVM 播放缓存目录解析工具。
 */
internal object JvmPlaybackCacheDirectories {
    /**
     * 解析默认播放缓存目录。
     *
     * @param contextWrapper JVM 平台上下文。
     * @return 位于可重建缓存根目录下的播放缓存目录。
     */
    fun defaultCacheDirectory(contextWrapper: ContextWrapper): File {
        return File(contextWrapper.cacheDirectory, PlatformCacheDirectoryNames.SONG).normalizedDirectory()
    }

    /**
     * 判断两个目录是否相同或存在父子嵌套关系。
     *
     * 迁移缓存时如果目标目录与源目录互相包含，移动会递归进自身，必须提前拒绝。
     *
     * @param first 第一个目录。
     * @param second 第二个目录。
     * @return true 表示两个目录相同，或其中一个位于另一个内部。
     */
    fun isSameOrNested(first: File, second: File): Boolean {
        val firstPath = first.normalizedDirectory().absolutePath
        val secondPath = second.normalizedDirectory().absolutePath
        return firstPath == secondPath ||
                firstPath.startsWith(secondPath + File.separator) ||
                secondPath.startsWith(firstPath + File.separator)
    }

    /**
     * 读取目录所在磁盘的可用空间。
     *
     * 目录尚未创建时向上查找已存在的父目录，避免迁移前无法预估空间。
     *
     * @param directory 目标目录。
     * @return 可用字节数；无法读取时返回 0。
     */
    fun freeSpaceBytes(directory: File): Long {
        return runCatching {
            var current: File? = directory.normalizedDirectory()
            while (current != null && !current.exists()) {
                current = current.parentFile
            }
            current?.usableSpace ?: 0L
        }.getOrDefault(0L)
    }

    /**
     * 统计目录占用空间。
     *
     * 迁移前需要独立统计源目录大小，不能复用绑定当前目录的缓存存储层。
     *
     * @param directory 要统计的目录。
     * @return 目录下所有文件大小，单位是 byte。
     */
    fun directorySizeBytes(directory: File): Long {
        return if (directory.exists()) {
            directory.walkTopDown().filter { file -> file.isFile }.sumOf { file -> file.length() }
        } else {
            0L
        }
    }
}

/**
 * 规范化 JVM 播放缓存目录路径。
 */
private fun File.normalizedDirectory(): File {
    return runCatching {
        canonicalFile
    }.getOrDefault(absoluteFile)
}
