package cn.xybbz.router

import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.album_24px
import xymusic_kmp.composeapp.generated.resources.album
import xymusic_kmp.composeapp.generated.resources.all_music
import xymusic_kmp.composeapp.generated.resources.favorite_border_24px
import xymusic_kmp.composeapp.generated.resources.favorite
import xymusic_kmp.composeapp.generated.resources.genres
import xymusic_kmp.composeapp.generated.resources.home
import xymusic_kmp.composeapp.generated.resources.label_24px
import xymusic_kmp.composeapp.generated.resources.person_24px
import xymusic_kmp.composeapp.generated.resources.artist
import xymusic_kmp.composeapp.generated.resources.music_note_24px

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
    )
)
