package cn.xybbz.entity.data

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cn.xybbz.common.enums.SortTypeEnum
import cn.xybbz.localdata.data.era.XyEraItem

@Stable
class Sort {
    var sortType: SortTypeEnum? by mutableStateOf(null)

    //过滤条件,年代
    var eraItem: XyEraItem? by mutableStateOf(null)

    //收藏条件,收藏,全部
    var isFavorite: Boolean? by mutableStateOf(null)

    constructor(sortType: SortTypeEnum? = null) {
        this.sortType = sortType
    }

    constructor(
        sortType: SortTypeEnum? = null,
        eraItem: XyEraItem? = null,
        isFavorite: Boolean? = null
    ) {
        this.sortType = sortType
        this.eraItem = eraItem
        this.isFavorite = isFavorite
    }

    fun copy(): Sort {
        return Sort(this.sortType, this.eraItem, this.isFavorite)
    }
}

@Stable
class ArtistFilter {
    //收藏条件,收藏,全部
    var isFavorite: Boolean? by mutableStateOf(null)

    constructor()

    constructor(isFavorite: Boolean?) {
        this.isFavorite = isFavorite
    }

    fun copy(): ArtistFilter {
        return ArtistFilter(this.isFavorite)
    }

}