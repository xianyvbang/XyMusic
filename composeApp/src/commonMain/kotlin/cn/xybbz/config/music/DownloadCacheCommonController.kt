package cn.xybbz.config.music

import cn.xybbz.config.scope.IoScoped
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.localdata.data.music.XyPlayMusic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

/**
 * 缓存目录切换时旧缓存的处理策略。
 */
enum class CacheMigrationPolicy {
    /**
     * 把旧目录缓存迁移到新目录。
     */
    MIGRATE,

    /**
     * 不迁移，保留旧目录缓存。
     */
    KEEP_OLD,

    /**
     * 不迁移，并删除旧目录缓存。
     */
    DELETE_OLD,
}

/**
 * 缓存目录切换失败原因。
 */
enum class CacheDirectoryChangeFailure {
    /**
     * 路径为空或无法创建。
     */
    INVALID_PATH,

    /**
     * 目标目录不可写。
     */
    NOT_WRITABLE,

    /**
     * 目标目录与当前目录相同。
     */
    SAME_DIRECTORY,

    /**
     * 目标目录与当前目录存在父子嵌套关系。
     */
    NESTED_DIRECTORY,

    /**
     * 目标目录非空，无法安全迁移。
     */
    TARGET_NOT_EMPTY,

    /**
     * 目标磁盘剩余空间不足。
     */
    INSUFFICIENT_SPACE,

    /**
     * 迁移过程中复制或移动失败。
     */
    MIGRATION_FAILED,

    /**
     * 新路径持久化失败。
     */
    PERSIST_FAILED,
}

/**
 * 缓存目录切换结果。
 */
sealed interface CacheDirectoryChangeResult {
    /**
     * 切换成功。
     */
    data object Success : CacheDirectoryChangeResult

    /**
     * 切换失败，携带失败原因。
     */
    data class Failed(val reason: CacheDirectoryChangeFailure) : CacheDirectoryChangeResult

    /**
     * 用户取消了迁移。
     */
    data object Cancelled : CacheDirectoryChangeResult
}

/**
 * 缓存迁移阶段。
 */
enum class CacheMigrationPhase {
    /**
     * 正在复制旧缓存。
     */
    COPYING,

    /**
     * 正在删除旧缓存。
     */
    DELETING_OLD,

    /**
     * 迁移完成。
     */
    COMPLETED,
}

/**
 * 缓存迁移进度。
 *
 * @property processedBytes 已处理字节数。
 * @property totalBytes 需要处理的总字节数。
 * @property phase 当前迁移阶段。
 */
data class CacheMigrationProgress(
    val processedBytes: Long,
    val totalBytes: Long,
    val phase: CacheMigrationPhase,
) {
    /**
     * 迁移进度比例，范围 0f..1f。
     */
    val fraction: Float
        get() = if (totalBytes <= 0L) {
            0f
        } else {
            (processedBytes.toFloat() / totalBytes).coerceIn(0f, 1f)
        }
}

abstract class DownloadCacheCommonController : IoScoped(), KoinComponent {


    val settingsManager = get<SettingsManager>()

    /**
     * 所有缓存大小
     */
    private val _allCacheSizeFlow = MutableStateFlow(0L)
    val allCacheSizeFlow = _allCacheSizeFlow.asStateFlow()

    /**
     * 当前缓存进度
     */
    private val _cacheSchedule = MutableStateFlow(0f)
    val cacheSchedule = _cacheSchedule.asStateFlow()

    fun updateCacheSchedule(schedule: Float) {
        _cacheSchedule.value = schedule.coerceIn(0f, 1f)
    }

    /**
     * 缓存迁移进度。
     *
     * 为 null 表示当前没有进行中的迁移任务。
     */
    private val _cacheMigrationProgress = MutableStateFlow<CacheMigrationProgress?>(null)
    val cacheMigrationProgress = _cacheMigrationProgress.asStateFlow()

    /**
     * 更新缓存迁移进度。
     *
     * @param progress 最新进度；传 null 表示迁移结束。
     */
    protected fun updateCacheMigrationProgress(progress: CacheMigrationProgress?) {
        _cacheMigrationProgress.value = progress
    }

    /**
     * 清空缓存
     */
    abstract fun clearCache()

    /**
     * 获得所有缓存大小
     */
    abstract fun getCacheSize()

    /**
     * 缓存音乐
     */
    abstract fun cacheMedia(
        music: XyPlayMusic,
        ifStatic: Boolean
    )

    /**
     * 取消所有缓存
     */
    abstract fun cancelAllCache()

    /**
     * 根据缓存key获得mediaItem
     */
    abstract fun getMediaItem(cacheKey: String): Any?

    /**
     * 当前缓存目录路径。
     */
    open val cacheDirectoryPath: String
        get() = settingsManager.cacheFilePath.value

    /**
     * 当前缓存目录是否为平台默认目录。
     */
    open val isDefaultCacheDirectory: Boolean
        get() = true

    /**
     * 切换缓存目录。默认平台无自定义目录能力。
     */
    open suspend fun changeCacheDirectory(path: String): Boolean {
        return false
    }

    /**
     * 按指定策略切换缓存目录。默认平台无自定义目录能力。
     *
     * @param path 用户选择的新目录路径。
     * @param policy 旧缓存处理策略。
     * @return 切换结果。
     */
    open suspend fun changeCacheDirectory(
        path: String,
        policy: CacheMigrationPolicy,
    ): CacheDirectoryChangeResult {
        return if (changeCacheDirectory(path)) {
            CacheDirectoryChangeResult.Success
        } else {
            CacheDirectoryChangeResult.Failed(CacheDirectoryChangeFailure.INVALID_PATH)
        }
    }

    /**
     * 恢复默认缓存目录。默认平台无自定义目录能力。
     */
    open suspend fun restoreDefaultCacheDirectory(): Boolean {
        return false
    }

    /**
     * 按指定策略恢复默认缓存目录。默认平台无自定义目录能力。
     *
     * @param policy 旧缓存处理策略。
     * @return 切换结果。
     */
    open suspend fun restoreDefaultCacheDirectory(
        policy: CacheMigrationPolicy,
    ): CacheDirectoryChangeResult {
        return if (restoreDefaultCacheDirectory()) {
            CacheDirectoryChangeResult.Success
        } else {
            CacheDirectoryChangeResult.Failed(CacheDirectoryChangeFailure.INVALID_PATH)
        }
    }

    /**
     * 清理历史切换目录时删除失败的旧缓存目录。
     */
    open suspend fun cleanupPendingCacheDirectories() = Unit

    /**
     * 获得缓存key
     */
    fun getCacheKey(musicId: String): String {
        val settings = settingsManager.get()
        val audioBitRate = settingsManager.audioBitRate.value
        val ifStatic = settings.ifTranscoding || audioBitRate == 0
        return musicId + ifStatic + settings.transcodeFormat + audioBitRate
    }

    /**
     * 更新当前缓存大小
     */
    fun updateCacheSizeFlow(size: Long) {
        _allCacheSizeFlow.value = size
    }
}
