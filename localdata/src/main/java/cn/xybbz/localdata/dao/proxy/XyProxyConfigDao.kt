package cn.xybbz.localdata.dao.proxy

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cn.xybbz.localdata.data.proxy.XyProxyConfig
import kotlinx.coroutines.flow.Flow

@Dao
interface XyProxyConfigDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(data: XyProxyConfig): Long

    @Query("SELECT * FROM xy_proxy_config limit 1")
    suspend fun getConfig(): XyProxyConfig?

    @Query("SELECT * FROM xy_proxy_config limit 1")
     fun getConfigFlow(): Flow<XyProxyConfig?>
}