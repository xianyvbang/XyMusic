package cn.xybbz.music

import android.content.Context
import android.os.Environment
import cn.xybbz.config.setting.SettingsManager
import java.io.File

internal fun defaultAndroidPlaybackCacheDirectory(context: Context): File {
    val cacheParentDirectory =
        if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
            File(
//                    context.externalCacheDir,
                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                ANDROID_CACHE_CHILD_PATH
            )
        } else {
            File(context.filesDir, ANDROID_CACHE_CHILD_PATH)
        }
    return File(cacheParentDirectory, ANDROID_CACHE_DIRECTORY_NAME).normalizedDirectory()
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

private const val ANDROID_CACHE_CHILD_PATH = "cache"
private const val ANDROID_CACHE_DIRECTORY_NAME = "example_media_cache"
