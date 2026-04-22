package cn.xybbz.entity.data

import androidx.compose.runtime.Immutable
import cn.xybbz.common.enums.SortTypeEnum

/**
 * 列表页排序与筛选条件。
 */
@Immutable
data class Sort(
    // 排序类型
    val sortType: SortTypeEnum? = null,
    // 过滤条件年列表
    val yearList: List<Int>? = null,
    // 收藏条件, 收藏, 全部
    val isFavorite: Boolean? = null
)

/**
 * 艺术家页面筛选条件。
 */
@Immutable
data class ArtistFilter(
    // 收藏条件, 收藏, 全部
    val isFavorite: Boolean? = null
)
