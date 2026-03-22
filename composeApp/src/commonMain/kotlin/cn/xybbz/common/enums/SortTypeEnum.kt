package cn.xybbz.common.enums

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.MoreTime
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Person
import androidx.compose.ui.graphics.vector.ImageVector
import cn.xybbz.R

enum class SortTypeEnum(@param:StringRes val title: Int, val imageVector: ImageVector?) {
    /**
     * 创建时间排序
     */
    CREATE_TIME_ASC(R.string.sort_by_create_time_asc, Icons.Rounded.MoreTime),

    /**
     * 创建时间倒序
     */
    CREATE_TIME_DESC(R.string.sort_by_create_time_desc, null),

    /**
     * 按歌曲名称排序
     */
    MUSIC_NAME_ASC(R.string.sort_by_music_name_asc, Icons.Rounded.MusicNote),

    /**
     * 按歌曲名称倒序
     */
    MUSIC_NAME_DESC(R.string.sort_by_music_name_desc, null),

    /**
     * 按专辑名称排序
     */
    ALBUM_NAME_ASC(R.string.sort_by_album_name_asc, Icons.Rounded.Album),

    /**
     * 按专辑名称倒序
     */
    ALBUM_NAME_DESC(R.string.sort_by_album_name_desc, null),

    /**
     * 按艺术家排序
     */
    ARTIST_NAME_ASC(R.string.sort_by_artist_name_asc, Icons.Rounded.Person),

    /**
     * 按艺术家倒序
     */
    ARTIST_NAME_DESC(R.string.sort_by_artist_name_desc, null),

    /**
     * 按发行时间正序
     */
    PREMIERE_DATE_ASC(R.string.sort_by_premiere_date_asc, Icons.Rounded.DateRange),

    /**
     * 按发行时间倒叙
     */
    PREMIERE_DATE_DESC(R.string.sort_by_premiere_date_desc, null)
}