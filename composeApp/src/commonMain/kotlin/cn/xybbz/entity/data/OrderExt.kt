package cn.xybbz.entity.data

import cn.xybbz.api.enums.jellyfin.ItemSortBy
import cn.xybbz.api.enums.jellyfin.SortOrder
import cn.xybbz.api.enums.navidrome.OrderType
import cn.xybbz.api.enums.navidrome.SortType
import cn.xybbz.api.enums.plex.PlexSortOrder
import cn.xybbz.api.enums.plex.PlexSortType
import cn.xybbz.common.enums.SortTypeEnum

/**
 * 将SortTypeEnum转换成SearchAndOrder
 */
fun SortTypeEnum?.toSearchAndOrder(): SearchAndOrder {
    return when (this) {
        SortTypeEnum.CREATE_TIME_ASC -> {
            SearchAndOrder(ItemSortBy.DATE_CREATED)
        }

        SortTypeEnum.CREATE_TIME_DESC -> {
            SearchAndOrder(ItemSortBy.DATE_CREATED, SortOrder.DESCENDING)
        }

        SortTypeEnum.MUSIC_NAME_ASC -> {
            SearchAndOrder()
        }

        SortTypeEnum.MUSIC_NAME_DESC -> {
            SearchAndOrder(order = SortOrder.DESCENDING)
        }

        SortTypeEnum.ALBUM_NAME_ASC -> {
            SearchAndOrder(ItemSortBy.ALBUM)
        }

        SortTypeEnum.ALBUM_NAME_DESC -> {
            SearchAndOrder(ItemSortBy.ALBUM, SortOrder.DESCENDING)
        }

        SortTypeEnum.ARTIST_NAME_ASC -> {
            SearchAndOrder(ItemSortBy.ARTIST)
        }

        SortTypeEnum.ARTIST_NAME_DESC -> {
            SearchAndOrder(ItemSortBy.ARTIST, SortOrder.DESCENDING)
        }

        null -> {
            SearchAndOrder()
        }

        SortTypeEnum.PREMIERE_DATE_ASC -> SearchAndOrder(ItemSortBy.PREMIERE_DATE)
        SortTypeEnum.PREMIERE_DATE_DESC -> SearchAndOrder(
            ItemSortBy.PREMIERE_DATE,
            SortOrder.DESCENDING
        )
    }
}

/**
 * 将SortTypeEnum转换成NavidromeOrder
 */
fun SortTypeEnum?.toNavidromeOrder(): NavidromeOrder{
    return when (this) {
        SortTypeEnum.CREATE_TIME_ASC -> {
            NavidromeOrder(SortType.CREATED_AT)
        }

        SortTypeEnum.CREATE_TIME_DESC -> {
            NavidromeOrder(SortType.CREATED_AT, OrderType.DESC)
        }

        SortTypeEnum.MUSIC_NAME_ASC -> {
            NavidromeOrder()
        }

        SortTypeEnum.MUSIC_NAME_DESC -> {
            NavidromeOrder()
        }

        SortTypeEnum.ALBUM_NAME_ASC -> {
            NavidromeOrder(SortType.NAME)
        }

        SortTypeEnum.ALBUM_NAME_DESC -> {
            NavidromeOrder(SortType.NAME, OrderType.DESC)
        }

        SortTypeEnum.ARTIST_NAME_ASC -> {
            NavidromeOrder(SortType.ALBUM_ARTIST)
        }

        SortTypeEnum.ARTIST_NAME_DESC -> {
            NavidromeOrder(SortType.ALBUM_ARTIST, OrderType.DESC)
        }

        null -> {
            NavidromeOrder()
        }

        SortTypeEnum.PREMIERE_DATE_ASC -> NavidromeOrder(SortType.MAX_YEAR)
        SortTypeEnum.PREMIERE_DATE_DESC -> NavidromeOrder(SortType.MAX_YEAR, OrderType.DESC)
    }
}


fun SortTypeEnum?.toNavidromeOrder2(): NavidromeOrder{
    return when (this) {
        SortTypeEnum.CREATE_TIME_ASC -> {
            NavidromeOrder(SortType.CREATED_AT)
        }

        SortTypeEnum.CREATE_TIME_DESC -> {
            NavidromeOrder(SortType.CREATED_AT, OrderType.DESC)
        }

        SortTypeEnum.MUSIC_NAME_ASC -> {
            NavidromeOrder(sortType = SortType.TITLE)
        }

        SortTypeEnum.MUSIC_NAME_DESC -> {
            NavidromeOrder(sortType = SortType.TITLE, order = OrderType.DESC)
        }

        SortTypeEnum.ALBUM_NAME_ASC -> {
            NavidromeOrder(SortType.ALBUM)
        }

        SortTypeEnum.ALBUM_NAME_DESC -> {
            NavidromeOrder(SortType.ALBUM, OrderType.DESC)
        }

        SortTypeEnum.ARTIST_NAME_ASC -> {
            NavidromeOrder(SortType.ARTIST)
        }

        SortTypeEnum.ARTIST_NAME_DESC -> {
            NavidromeOrder(SortType.ARTIST, OrderType.DESC)
        }

        null -> {
            NavidromeOrder(sortType = SortType.TITLE)
        }

        SortTypeEnum.PREMIERE_DATE_ASC -> NavidromeOrder(SortType.YEAR)
        SortTypeEnum.PREMIERE_DATE_DESC -> NavidromeOrder(SortType.YEAR, OrderType.DESC)
    }
}

/**
 * 将SortTypeEnum转换成PlexOrder
 */
fun SortTypeEnum?.toPlexOrder(): PlexOrder{
    return when (this) {
        SortTypeEnum.CREATE_TIME_ASC -> {
            PlexOrder(PlexSortType.ADDED_AT)
        }

        SortTypeEnum.CREATE_TIME_DESC -> {
            PlexOrder(PlexSortType.ADDED_AT, PlexSortOrder.DESCENDING)
        }

        SortTypeEnum.MUSIC_NAME_ASC -> {
            PlexOrder()
        }

        SortTypeEnum.MUSIC_NAME_DESC -> {
            PlexOrder(order = PlexSortOrder.DESCENDING)
        }

        SortTypeEnum.ALBUM_NAME_ASC -> {
            PlexOrder(PlexSortType.ALBUM_TITLE_SORT)
        }

        SortTypeEnum.ALBUM_NAME_DESC -> {
            PlexOrder(PlexSortType.ALBUM_TITLE_SORT, PlexSortOrder.DESCENDING)
        }

        SortTypeEnum.ARTIST_NAME_ASC -> {
            PlexOrder(PlexSortType.ARTIST_TITLE_SORT)
        }

        SortTypeEnum.ARTIST_NAME_DESC -> {
            PlexOrder(PlexSortType.ARTIST_TITLE_SORT, PlexSortOrder.DESCENDING)
        }

        null -> {
            PlexOrder()
        }

        SortTypeEnum.PREMIERE_DATE_ASC -> PlexOrder(PlexSortType.YEAR)
        SortTypeEnum.PREMIERE_DATE_DESC -> PlexOrder(
            PlexSortType.YEAR,
            PlexSortOrder.DESCENDING
        )
    }
}