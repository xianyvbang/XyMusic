package cn.xybbz.page.parent

import androidx.paging.ExperimentalPagingApi
import cn.xybbz.api.client.IDataSourceParentServer
import cn.xybbz.api.client.data.AllResponse
import cn.xybbz.common.constants.RemoteIdConstants
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.genre.XyGenre

@OptIn(ExperimentalPagingApi::class)
class GenresRemoteMediator(
    private val datasourceServer: IDataSourceParentServer,
    private val db: DatabaseClient,
    private val connectionId: Long
) : DefaultRemoteMediator<XyGenre>(
    db,
    RemoteIdConstants.GENRE + connectionId,
    connectionId
) {

    /**
     * 获得远程服务对象列表
     * @param [loadKey] 页码 从0开始
     * @param [pageSize] 页面大小
     */
    override suspend fun getRemoteServerObjectList(
        loadKey: Int,
        pageSize: Int
    ): AllResponse<XyGenre> {
        return datasourceServer.getRemoteServerGenreList(
            startIndex = loadKey * pageSize,
            pageSize = pageSize
        )
    }

    /**
     * 删除本地数据库对象列表
     */
    override suspend fun removeLocalObjectList() {
        db.genreDao.remove()
    }

    /**
     * 存储对象列表到本地数据库
     */
    override suspend fun saveBatchLocalObjectList(items: List<XyGenre>) {
        datasourceServer.saveBatchGenre(
            items = items
        )
    }
}