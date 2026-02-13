package cn.xybbz.localdata.data.connection

import androidx.room.Embedded

data class ConnectionConfigExt(
    @Embedded
    val connectionConfig: ConnectionConfig,
    val libraryName: String?
)
