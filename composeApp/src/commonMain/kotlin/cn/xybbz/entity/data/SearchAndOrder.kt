package cn.xybbz.entity.data

import cn.xybbz.api.enums.jellyfin.ItemSortBy
import cn.xybbz.api.enums.jellyfin.SortOrder
import cn.xybbz.api.enums.navidrome.OrderType
import cn.xybbz.api.enums.navidrome.SortType
import cn.xybbz.api.enums.plex.PlexSortOrder
import cn.xybbz.api.enums.plex.PlexSortType

/**
 * 排序对象
 */
data class SearchAndOrder(
    val sortType: ItemSortBy? = ItemSortBy.SORT_NAME,
    val order: SortOrder? = SortOrder.ASCENDING
)

data class NavidromeOrder(
    val sortType: SortType = SortType.NAME,
    val order: OrderType = OrderType.ASC
)

data class PlexOrder(
    val sortType: PlexSortType = PlexSortType.TITLE_SORT,
    val order: PlexSortOrder = PlexSortOrder.ASCENDING
)