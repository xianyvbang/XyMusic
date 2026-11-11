package cn.xybbz.router

import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import xymusic.composeapp.generated.resources.Res
import xymusic.composeapp.generated.resources.add_link_24px
import xymusic.composeapp.generated.resources.album_24px
import xymusic.composeapp.generated.resources.album
import xymusic.composeapp.generated.resources.all_music
import xymusic.composeapp.generated.resources.connection_management
import xymusic.composeapp.generated.resources.favorite_border_24px
import xymusic.composeapp.generated.resources.favorite
import xymusic.composeapp.generated.resources.genres
import xymusic.composeapp.generated.resources.home
import xymusic.composeapp.generated.resources.label_24px
import xymusic.composeapp.generated.resources.local_and_download
import xymusic.composeapp.generated.resources.person_24px
import xymusic.composeapp.generated.resources.artist
import xymusic.composeapp.generated.resources.music_note_24px
import xymusic.composeapp.generated.resources.queue_music_24px
import xymusic.composeapp.generated.resources.settings
import xymusic.composeapp.generated.resources.settings_24px

data class JvmTopRouterData(
    val title: StringResource,
    val icon: DrawableResource,
    val route: RouterConstants
)

val jvmTopRouterDataList = listOf(
    JvmTopRouterData(
        title = Res.string.home,
        icon = Res.drawable.album_24px,
        route = Home
    ),
    JvmTopRouterData(
        title = Res.string.all_music,
        icon = Res.drawable.music_note_24px,
        route = Music
    ),
    JvmTopRouterData(
        title = Res.string.local_and_download,
        icon = Res.drawable.queue_music_24px,
        route = Local
    ),
    JvmTopRouterData(
        title = Res.string.album,
        icon = Res.drawable.album_24px,
        route = Album
    ),
    JvmTopRouterData(
        title = Res.string.artist,
        icon = Res.drawable.person_24px,
        route = Artist
    ),
    JvmTopRouterData(
        title = Res.string.favorite,
        icon = Res.drawable.favorite_border_24px,
        route = FavoriteList
    ),
    JvmTopRouterData(
        title = Res.string.genres,
        icon = Res.drawable.label_24px,
        route = Genres
    ),
    // 链接管理与设置属于全局入口，与上面的一级导航同列排列
    JvmTopRouterData(
        title = Res.string.connection_management,
        icon = Res.drawable.add_link_24px,
        route = ConnectionManagement
    ),
    JvmTopRouterData(
        title = Res.string.settings,
        icon = Res.drawable.settings_24px,
        route = Setting
    )
)
