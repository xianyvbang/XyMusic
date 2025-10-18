package cn.xybbz.page.parent

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import cn.xybbz.api.client.data.AllResponse
import cn.xybbz.common.constants.Constants
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.remote.RemoteCurrent
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalPagingApi::class)
abstract class DefaultRemoteMediator<T : Any>(
    private val db: DatabaseClient,
    protected val remoteId:String,
    private val connectionId: Long
): RemoteMediator<Int, T>() {

    protected val remoteKeyDao = db.remoteCurrentDao

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, T>
    ): MediatorResult {
        return try {

            val loadKey: Int = when (loadType) {
                LoadType.REFRESH -> 0
                LoadType.PREPEND -> return MediatorResult.Success(
                    endOfPaginationReached = true
                )

                LoadType.APPEND -> {
                    val remoteKey = db.withTransaction {
                        remoteKeyDao.remoteKeyById(remoteId)
                    }
                    if (remoteKey == null || (remoteKey.nextKey * state.config.pageSize) >= remoteKey.total
                    ) {
                        return MediatorResult.Success(
                            endOfPaginationReached = true
                        )
                    }
                    remoteKey.nextKey.plus(1)
                }
            }

            val response = getRemoteServerObjectList(loadKey,state.config.pageSize)

            db.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    remoteKeyDao.deleteById(remoteId)
                    removeLocalObjectList()
                }

                remoteKeyDao.insertOrReplace(
                    RemoteCurrent(
                        id = remoteId,
                        nextKey = loadKey,
                        total = response.totalRecordCount,
                        connectionId = connectionId
                    )
                )
                response.items?.let {
                    saveBatchLocalObjectList(it)
                }

            }

            MediatorResult.Success(
                endOfPaginationReached = response.items.isNullOrEmpty()
                        || (loadKey * state.config.pageSize) >= response.totalRecordCount
            )
        } catch (e: Exception) {
            e.printStackTrace()
            MediatorResult.Error(e)
        }
    }

    override suspend fun initialize(): InitializeAction {
        return getInitializeAction()
    }

    open suspend fun getInitializeAction():InitializeAction{
        val cacheTimeout =
            TimeUnit.MILLISECONDS.convert(Constants.PAGE_TIME_FAILURE, TimeUnit.MINUTES)
        return if (System.currentTimeMillis() - (remoteKeyDao.remoteKeyById(remoteId)?.createTime
                ?: 0) <= cacheTimeout
        ) {
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }

    /**
     * 获得远程服务对象列表
     * @param [loadKey] 页码 从0开始
     * @param [pageSize] 页面大小
     */
    abstract suspend fun getRemoteServerObjectList(loadKey:Int,pageSize:Int): AllResponse<T>

    /**
     * 删除本地数据库对象列表
     */
    abstract suspend fun removeLocalObjectList()

    /**
     * 存储对象列表到本地数据库
     */
    abstract suspend fun saveBatchLocalObjectList(items:List<T>)
}