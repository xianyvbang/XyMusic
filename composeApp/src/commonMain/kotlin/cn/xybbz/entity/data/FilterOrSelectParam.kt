package cn.xybbz.entity.data

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cn.xybbz.common.enums.SortTypeEnum

@Stable
class Sort {
    var sortType: SortTypeEnum? by mutableStateOf(null)

    //过滤条件年列表
    var yearList: List<Int>? by mutableStateOf(null)

    //收藏条件,收藏,全部
    var isFavorite: Boolean? by mutableStateOf(null)

    constructor(sortType: SortTypeEnum? = null) {
        this.sortType = sortType
    }

    constructor(
        sortType: SortTypeEnum? = null,
        yearList: List<Int>? = null,
        isFavorite: Boolean? = null
    ) {
        this.sortType = sortType
        this.yearList = yearList
        this.isFavorite = isFavorite
    }

    fun copy(): Sort {
        return Sort(this.sortType, this.yearList, this.isFavorite)
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