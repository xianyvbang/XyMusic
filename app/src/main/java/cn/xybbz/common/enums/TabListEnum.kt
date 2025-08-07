package cn.xybbz.common.enums

/**
 * 收藏Tab
 */
enum class TabListEnum(val code: Int, val message: String) {
    Music(1, "音乐"),
    Album(3, "专辑"),
}

/**
 * 搜索Tab
 */
enum class SearchTabListEnum(val code: Int, val message: String) {
    Music(1, "音乐"),
    Album(3, "专辑"),
    ARTIST(4, "艺术家")
}