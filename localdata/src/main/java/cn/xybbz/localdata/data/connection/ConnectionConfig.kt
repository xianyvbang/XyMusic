package cn.xybbz.localdata.data.connection

import androidx.room.Entity
import androidx.room.PrimaryKey
import cn.xybbz.localdata.enums.DataSourceType
import java.time.Instant

@Entity(tableName = "xy_connection_config")
data class ConnectionConfig(
    /**
     * 链接id
     */
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /**
     * 链接服务id
     */
    val serverId: String = "",
    /**
     * 服务端名称
     */
    val serverName:String = "",
    /**
     * 服务端版本
     */
    val serverVersion: String,

    /**
     * 连接设备标识
     */
    val deviceId:String = "",

    /**
     * 链接名称
     */
    val name: String,
    /**
     * 链接地址 http/https
     */
    val address: String,
    /**
     * 链接类型
     */
    val type: DataSourceType,
    /**
     * 用户id
     */
    val userId: String,
    /**
     * 用户名
     */
    val username: String = "",
    /**
     * 用户token
     */
    val accessToken: String? = "",
    /**
     * 当前密码 加密存储
     */
    val currentPassword: String = "",
    /**
     * AES iv
     */
    val iv: String = "",
    /**
     * AES key
     */
    val key: String = "",

    /**
     * 媒体库Id
     */
    val libraryId: String? = null,

    /**
     * 扩展信息
     */
    val extendInfo: String? = null,
    /**
     * 最后登陆时间
     */
    val lastLoginTime: Long = Instant.now().toEpochMilli(),
    /**
     * 更新数据时间
     */
    val updateTime: Long = System.currentTimeMillis(),
    /**
     * 第一次链接时间
     */
    val createTime: Long = System.currentTimeMillis()
)
