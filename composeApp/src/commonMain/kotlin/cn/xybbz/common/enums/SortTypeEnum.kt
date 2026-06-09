package cn.xybbz.common.enums

import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.album_24px
import xymusic_kmp.composeapp.generated.resources.date_range_24px
import xymusic_kmp.composeapp.generated.resources.more_time_24px
import xymusic_kmp.composeapp.generated.resources.music_note_24px
import xymusic_kmp.composeapp.generated.resources.person_24px
import xymusic_kmp.composeapp.generated.resources.sort_by_album_name_asc
import xymusic_kmp.composeapp.generated.resources.sort_by_album_name_desc
import xymusic_kmp.composeapp.generated.resources.sort_by_artist_name_asc
import xymusic_kmp.composeapp.generated.resources.sort_by_artist_name_desc
import xymusic_kmp.composeapp.generated.resources.sort_by_create_time_asc
import xymusic_kmp.composeapp.generated.resources.sort_by_create_time_desc
import xymusic_kmp.composeapp.generated.resources.sort_by_music_name_asc
import xymusic_kmp.composeapp.generated.resources.sort_by_music_name_desc
import xymusic_kmp.composeapp.generated.resources.sort_by_premiere_date_asc
import xymusic_kmp.composeapp.generated.resources.sort_by_premiere_date_desc

enum class SortTypeEnum(val title: StringResource, val painter: DrawableResource?) {
    /**
     * 创建时间排序
     */
    CREATE_TIME_ASC(Res.string.sort_by_create_time_asc, Res.drawable.more_time_24px),

    /**
     * 创建时间倒序
     */
    CREATE_TIME_DESC(Res.string.sort_by_create_time_desc, null),

    /**
     * 按歌曲名称排序
     */
    MUSIC_NAME_ASC(Res.string.sort_by_music_name_asc, Res.drawable.music_note_24px),

    /**
     * 按歌曲名称倒序
     */
    MUSIC_NAME_DESC(Res.string.sort_by_music_name_desc, null),

    /**
     * 按专辑名称排序
     */
    ALBUM_NAME_ASC(Res.string.sort_by_album_name_asc, Res.drawable.album_24px),

    /**
     * 按专辑名称倒序
     */
    ALBUM_NAME_DESC(Res.string.sort_by_album_name_desc, null),

    /**
     * 按艺术家排序
     */
    ARTIST_NAME_ASC(Res.string.sort_by_artist_name_asc, Res.drawable.person_24px),

    /**
     * 按艺术家倒序
     */
    ARTIST_NAME_DESC(Res.string.sort_by_artist_name_desc, null),

    /**
     * 按发行时间正序
     */
    PREMIERE_DATE_ASC(Res.string.sort_by_premiere_date_asc, Res.drawable.date_range_24px),

    /**
     * 按发行时间倒叙
     */
    PREMIERE_DATE_DESC(Res.string.sort_by_premiere_date_desc, null)
}
