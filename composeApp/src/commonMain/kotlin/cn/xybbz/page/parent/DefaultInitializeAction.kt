package cn.xybbz.page.parent

import androidx.paging.ExperimentalPagingApi
import androidx.paging.RemoteMediator.InitializeAction
import cn.xybbz.localdata.dao.remote.RemoteCurrentDao
import kotlin.time.Clock

@OptIn(ExperimentalPagingApi::class)
suspend fun getInitializeAction(remoteKeyDao: RemoteCurrentDao, remoteId: String, cacheTimeout: Long):InitializeAction {
    val remoteCurrent = remoteKeyDao.remoteKeyById(remoteId)
    if (remoteCurrent?.refresh == true){
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }
    return if (Clock.System.now().toEpochMilliseconds() - (remoteCurrent?.createTime
            ?: 0) <= cacheTimeout
    ) {
        InitializeAction.SKIP_INITIAL_REFRESH
    } else {
        InitializeAction.LAUNCH_INITIAL_REFRESH
    }
}