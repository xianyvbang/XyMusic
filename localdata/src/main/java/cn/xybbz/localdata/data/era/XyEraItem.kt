package cn.xybbz.localdata.data.era

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 年代数据
 */
@Entity(
    tableName = "xy_era_item"
)
data class XyEraItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    /**
     * 标题
     */
    val title: String,
    /**
     * 年代
     */
    val era: Int,
    /**
     * 年份,使用逗号分割 例: 2021,2022,2023
     */
    val years: String
)