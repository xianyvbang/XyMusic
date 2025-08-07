package cn.xybbz.common.enums

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.MoreTime
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Person
import androidx.compose.ui.graphics.vector.ImageVector

enum class SortTypeEnum(val title: String, val imageVector: ImageVector?) {
    /**
     * 创建时间排序
     */
    CREATE_TIME_ASC("按创建时间排序", Icons.Rounded.MoreTime),

    /**
     * 创建时间倒序
     */
    CREATE_TIME_DESC("按创建时间倒序", null),

    /**
     * 按歌曲名称排序
     */
    MUSIC_NAME_ASC("按歌曲名称排序", Icons.Rounded.MusicNote),

    /**
     * 按歌曲名称倒序
     */
    MUSIC_NAME_DESC("按歌曲名称倒序", null),

    /**
     * 按专辑名称排序
     */
    ALBUM_NAME_ASC("按专辑名称排序", Icons.Rounded.Album),

    /**
     * 按专辑名称倒序
     */
    ALBUM_NAME_DESC("按专辑名倒序", null),

    /**
     * 按艺术家排序
     */
    ARTIST_NAME_ASC("按艺术家排序", Icons.Rounded.Person),

    /**
     * 按艺术家倒序
     */
    ARTIST_NAME_DESC("按艺术家倒序", null),
    /**
     * 按发行时间正序
     */
    PREMIERE_DATE_ASC("按发行时间正序", Icons.Rounded.DateRange),

    /**
     * 按发行时间倒叙
     */
    PREMIERE_DATE_DESC("按发行时间倒叙", null)
}