package cn.xybbz.page.parent

import androidx.paging.ExperimentalPagingApi
import cn.xybbz.api.client.IDataSourceParentServer
import cn.xybbz.api.client.data.AllResponse
import cn.xybbz.common.constants.RemoteIdConstants
import cn.xybbz.entity.data.Sort
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import kotlinx.coroutines.flow.StateFlow


@OptIn(ExperimentalPagingApi::class)
class MusicRemoteMediator(
    private val db: DatabaseClient,
    private val datasourceServer: IDataSourceParentServer,
    private val connectionId: Long,
    private val sortByFlow: StateFlow<Sort>
) : DefaultRemoteMediator<XyMusic>(
    db,
    RemoteIdConstants.MUSIC + connectionId,
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
    ): AllResponse<XyMusic> {
        val sort = sortByFlow.value
        return datasourceServer.getRemoteServerMusicList(
            startIndex = loadKey * pageSize,
            pageSize = pageSize,
            isFavorite = sort.isFavorite,
            sortType = sort.sortType,
            years = sort.yearList,
        )
    }

    /**
     * 删除本地数据库对象列表
     */
    override suspend fun removeLocalObjectList() {
        db.musicDao.removeByType(MusicDataTypeEnum.HOME)
    }

    /**
     * 存储对象列表到本地数据库
     */
    override suspend fun saveBatchLocalObjectList(items: List<XyMusic>) {
        datasourceServer.saveBatchMusic(
            items,
            MusicDataTypeEnum.HOME
        )
    }
}