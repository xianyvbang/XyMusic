package cn.xybbz.localdata.data.proxy

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "xy_proxy_config")
data class XyProxyConfig(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val enabled: Boolean = false,
    val address: String = ""
)