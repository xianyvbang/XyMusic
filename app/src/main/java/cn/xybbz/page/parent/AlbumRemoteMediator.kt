package cn.xybbz.page.parent

import androidx.paging.ExperimentalPagingApi
import cn.xybbz.api.client.IDataSourceParentServer
import cn.xybbz.api.client.data.XyResponse
import cn.xybbz.common.constants.RemoteIdConstants
import cn.xybbz.entity.data.Sort
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.album.XyAlbum
import cn.xybbz.localdata.enums.DataSourceType
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import kotlinx.coroutines.flow.StateFlow

/**
 * 专辑的网络数据加载
 * @author xybbz
 * @date 2024/06/14
 * @constructor 创建[AlbumRemoteMediator]
 * @param [userId] 用户id
 * @param [database] 本地缓存管理类
 * @param [dataSource] 数据源来兴
 */
@OptIn(ExperimentalPagingApi::class)
class AlbumRemoteMediator(
    private val dataSource: DataSourceType,
    private val db: DatabaseClient,
    private val datasourceServer: IDataSourceParentServer,
    private val connectionId: Long,
    private val sort: StateFlow<Sort>
) : DefaultRemoteMediator<XyAlbum,XyAlbum>(
    db,
    RemoteIdConstants.ALBUM + dataSource + connectionId,
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
    ): XyResponse<XyAlbum> {
        val sort = sort.value
        return datasourceServer.getRemoteServerAlbumList(
            startIndex = loadKey * pageSize,
            pageSize = pageSize,
            sortType = sort.sortType,
            isFavorite = sort.isFavorite,
            years = sort.yearList,
            genreId = null
        )
    }

    /**
     * 删除本地数据库对象列表
     */
    override suspend fun removeLocalObjectList() {
        db.albumDao.removeByType(MusicDataTypeEnum.HOME)
    }

    /**
     * 存储对象列表到本地数据库
     */
    override suspend fun saveBatchLocalObjectList(items: List<XyAlbum>) {
        //存储专辑
        datasourceServer.saveBatchAlbum(
            items,
            MusicDataTypeEnum.HOME
        )
    }
}