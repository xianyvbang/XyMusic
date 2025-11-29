package cn.xybbz.config.download

import cn.xybbz.common.utils.CoroutineScopeUtils
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.enums.DownloadStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadRepository @Inject constructor(
    val db: DatabaseClient
) {
    val scope = CoroutineScopeUtils.getIo("DownloadRepository")
    val musicIdsFlow: StateFlow<List<String>> =
        db.downloadDao.getAllMusicTaskUidsFlow(status = DownloadStatus.COMPLETED)
            .stateIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
}
