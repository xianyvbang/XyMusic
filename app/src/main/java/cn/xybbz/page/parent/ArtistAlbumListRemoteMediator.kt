package cn.xybbz.page.parent

import androidx.paging.ExperimentalPagingApi
import cn.xybbz.api.client.IDataSourceParentServer
import cn.xybbz.api.client.data.AllResponse
import cn.xybbz.common.constants.RemoteIdConstants
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.album.XyAlbum
import cn.xybbz.localdata.enums.MusicDataTypeEnum

@OptIn(ExperimentalPagingApi::class)
class ArtistAlbumListRemoteMediator(
    private val artistId: String,
    private val datasourceServer: IDataSourceParentServer,
    private val db: DatabaseClient,
    private val connectionId: Long
) : DefaultRemoteMediator<XyAlbum,XyAlbum>(
    db,
    RemoteIdConstants.ARTIST + RemoteIdConstants.ALBUM + artistId + connectionId,
    connectionId
){

    /**
     * 获得远程服务对象列表
     * @param [loadKey] 页码 从0开始
     * @param [pageSize] 页面大小
     */
    override suspend fun getRemoteServerObjectList(
        loadKey: Int,
        pageSize: Int
    ): AllResponse<XyAlbum> {
        return datasourceServer.getRemoteServerAlbumList(
            startIndex = loadKey * pageSize,
            pageSize = pageSize,
            artistId = artistId,
            genreId = null
        )
    }

    /**
     * 删除本地数据库对象列表
     */
    override suspend fun removeLocalObjectList() {
        db.albumDao.removeByType(MusicDataTypeEnum.ARTIST, artistId)
    }

    /**
     * 存储对象列表到本地数据库
     */
    override suspend fun saveBatchLocalObjectList(items: List<XyAlbum>) {
        datasourceServer.saveBatchAlbum(
            baseItemList = items,
            dataType = MusicDataTypeEnum.ARTIST,
            artistId = artistId
        )
    }
}