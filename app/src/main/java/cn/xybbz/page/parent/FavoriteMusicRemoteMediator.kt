package cn.xybbz.page.parent

import androidx.paging.ExperimentalPagingApi
import cn.xybbz.api.client.IDataSourceParentServer
import cn.xybbz.api.client.data.XyResponse
import cn.xybbz.common.constants.RemoteIdConstants
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.enums.MusicDataTypeEnum

@OptIn(ExperimentalPagingApi::class)
class FavoriteMusicRemoteMediator(
    private val datasourceServer: IDataSourceParentServer,
    private val db: DatabaseClient,
    private val connectionId: Long
) : DefaultRemoteMediator<XyMusic,XyMusic>(
    db,
    RemoteIdConstants.FAVORITE + connectionId,
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
    ): XyResponse<XyMusic> {
        return datasourceServer.getRemoteServerFavoriteMusicList(
            startIndex = loadKey * pageSize,
            pageSize = pageSize,
            isFavorite = true
        )
    }

    /**
     * 删除本地数据库对象列表
     */
    override suspend fun removeLocalObjectList() {
        db.musicDao.removeByType(MusicDataTypeEnum.FAVORITE)
    }

    /**
     * 存储对象列表到本地数据库
     */
    override suspend fun saveBatchLocalObjectList(items: List<XyMusic>) {
        datasourceServer.saveBatchMusic(
            items,
            MusicDataTypeEnum.FAVORITE
        )
    }
}