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

    @Query("UPDATE xy_proxy_config SET enabled = :enabled WHERE id = :id")
    suspend fun updateEnabled(enabled: Boolean, id: Long)

    @Query("UPDATE xy_proxy_config SET host = :host WHERE id = :id")
    suspend fun updateHost(host: String, id: Long)

    @Query("UPDATE xy_proxy_config SET port = :port WHERE id = :id")
    suspend fun updatePort(port: Int, id: Long)

    @Query("UPDATE xy_proxy_config SET username = :username WHERE id = :id")
    suspend fun updateUsername(username: String, id: Long)

    @Query("UPDATE xy_proxy_config SET password = :password WHERE id = :id")
    suspend fun updatePassword(password: String, id: Long)
}