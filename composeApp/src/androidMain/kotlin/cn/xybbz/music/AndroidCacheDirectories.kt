package cn.xybbz.music

import android.content.Context
import android.os.Environment
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.platform.PlatformCacheDirectoryNames
import java.io.File

/**
 * 计算 Android 端默认播放缓存目录。
 *
 * 优先使用应用私有外部存储目录（/Android/data/<包名>/files/cache），
 * 该目录免存储权限、卸载时自动清理，且不会被系统媒体扫描器收录；
 * 外部存储不可用时回退到内部存储 filesDir。
 */
internal fun defaultAndroidPlaybackCacheDirectory(context: Context): File {
    val cacheParentDirectory =
        if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
            // getExternalFilesDir 在外部存储异常时可能返回 null，此时回退到内部存储
            File(
                context.getExternalFilesDir(null) ?: context.filesDir,
                PlatformCacheDirectoryNames.CACHE
            )
        } else {
            File(context.filesDir, PlatformCacheDirectoryNames.CACHE)
        }
    return File(cacheParentDirectory, PlatformCacheDirectoryNames.SONG).normalizedDirectory()
}

internal fun resolveAndroidPlaybackCacheDirectory(
    context: Context,
    settingsManager: SettingsManager,
): File {
    return settingsManager.get()
        .cacheFilePath
        .trim()
        .takeIf { it.isNotEmpty() }
        ?.let { File(it).normalizedDirectory() }
        ?: defaultAndroidPlaybackCacheDirectory(context)
}

internal fun File.normalizedDirectory(): File {
    return runCatching {
        canonicalFile
    }.getOrDefault(absoluteFile)
}

internal fun File.isSameDirectory(other: File): Boolean {
    return normalizedDirectory().absolutePath == other.normalizedDirectory().absolutePath
}
