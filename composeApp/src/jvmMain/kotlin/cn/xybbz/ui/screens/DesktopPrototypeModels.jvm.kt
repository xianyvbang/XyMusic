@file:OptIn(ExperimentalFoundationApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package cn.xybbz.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.DrawableResource

/**
 * JVM 桌面端首页入口。
 */
@Composable
fun HomeDesktopScreen() = DesktopPrototypeScreen(DesktopDestination.Home)

/**
 * JVM 桌面端搜索页入口。
 */
@Composable
fun SearchDesktopScreen() = DesktopPrototypeScreen(DesktopDestination.Search)

/**
 * JVM 桌面端音乐库页入口。
 */
@Composable
fun MusicDesktopScreen() = DesktopPrototypeScreen(DesktopDestination.Library)

/**
 * JVM 桌面端专辑列表页入口。
 */
@Composable
fun AlbumDesktopScreen() = DesktopPrototypeScreen(DesktopDestination.Albums)

/**
 * JVM 桌面端艺术家列表页入口。
 */
@Composable
fun ArtistDesktopScreen() = DesktopPrototypeScreen(DesktopDestination.Artists)

/**
 * JVM 桌面端歌单详情页入口。
 */
@Composable
fun FavoriteDesktopScreen() = DesktopPrototypeScreen(DesktopDestination.PlaylistDetail)

/**
 * JVM 桌面端专辑详情入口。
 * 目前使用原型数据承载视觉结构，后续可替换为真实详情数据。
 */
@Composable
fun AlbumInfoDesktopScreen(albumInfo: cn.xybbz.router.AlbumInfo) {
    DesktopPrototypeScreen(
        DesktopDestination.AlbumDetail(
            title = if (albumInfo.itemId.isBlank()) sampleAlbumDetail.title else sampleAlbumDetail.title
        )
    )
}

/**
 * JVM 桌面端艺术家详情入口。
 * 优先使用路由里的名称，没有时回落到原型样例名称。
 */
@Composable
fun ArtistInfoDesktopScreen(artistInfo: cn.xybbz.router.ArtistInfo) {
    DesktopPrototypeScreen(
        DesktopDestination.ArtistDetail(
            name = artistInfo.artistName.ifBlank { sampleArtistDetail.name }
        )
    )
}

/**
 * 左侧边栏的一级导航目标。
 */
internal enum class SidebarDestination { Home, Search, Library, Albums, Artists }

/**
 * 桌面原型内部使用的页面枚举。
 */
internal sealed interface DesktopDestination {
    data object Home : DesktopDestination
    data object Search : DesktopDestination
    data object Library : DesktopDestination
    data object Albums : DesktopDestination
    data object Artists : DesktopDestination
    data object PlaylistDetail : DesktopDestination
    data class AlbumDetail(val title: String) : DesktopDestination
    data class ArtistDetail(val name: String) : DesktopDestination
}

/**
 * 桌面原型使用的颜色集合，尽量和 HTML 原型色板对应。
 */
internal data class DesktopColors(
    val bgBase: Color = Color(0xFF000000),
    val bgElevated: Color = Color(0xFF121212),
    val bgHighlight: Color = Color(0xFF1A1A1A),
    val bgHover: Color = Color(0xFF282828),
    val textPrimary: Color = Color.White,
    val textSecondary: Color = Color(0xFFA7A7A7),
    val theme: Color = Color(0xFF1DB954),
    val divider: Color = Color(0x1AFFFFFF),
)

/**
 * 侧边栏菜单项模型。
 */
internal data class MenuItem(
    val label: String,
    val destination: SidebarDestination,
    val icon: DrawableResource,
)

/**
 * 左侧歌单列表项模型。
 */
internal data class PlaylistCard(
    val title: String,
    val description: String,
    val accent: Color,
)

/**
 * 歌曲行展示模型。
 */
internal data class SongRowData(
    val index: Int,
    val title: String,
    val artist: String,
    val album: String,
    val meta: String,
    val duration: String,
    val accent: Color,
)

/**
 * 专辑/歌单/艺人卡片模型。
 */
internal data class AlbumCardData(
    val title: String,
    val subtitle: String,
    val accent: Color,
    val circular: Boolean = false,
    val highlight: Color? = null,
)

/**
 * 歌单/专辑详情头部展示模型。
 */
internal data class DetailHeaderData(
    val eyebrow: String,
    val title: String,
    val subtitle: String,
    val accent: Color,
    val circular: Boolean = false,
)

/**
 * 艺术家详情头部展示模型。
 */
internal data class ArtistDetailData(
    val name: String,
    val monthlyListeners: String,
    val accent: Color,
)

/**
 * 桌面原型全局色板实例。
 */
internal val desktopColors = DesktopColors()
