package cn.xybbz.localdata.data.proxy

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "xy_proxy_config")
data class XyProxyConfig(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val enabled: Boolean = false,
    val mode: String,
    val host: String? = null,
    val port: Int? = null,
    val username: String? = null,
    val password: String? = null
)