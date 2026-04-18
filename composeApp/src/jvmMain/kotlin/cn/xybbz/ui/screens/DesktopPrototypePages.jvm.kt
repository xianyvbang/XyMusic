package cn.xybbz.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.xybbz.ui.components.SongTable
import cn.xybbz.ui.components.SongTableColumns
import org.jetbrains.compose.resources.painterResource
import xymusic_kmp.composeapp.generated.resources.*

/**
 * 首页内容，包含推荐歌曲、最新专辑和最近播放区域。
 */
@Composable
internal fun HomeDesktopContent() {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(40.dp),
    ) {
        SongSection("推荐音乐", recommendedSongs)
        AlbumSection("最新专辑", latestAlbums, onOpenAlbum = {})
        SongSection("最近播放音乐", recentSongs)
        AlbumSection("最近播放专辑", recentAlbums, onOpenAlbum = {})
        AlbumSection("热门专辑", hotAlbums, onOpenAlbum = {})
        Spacer(modifier = Modifier.height(8.dp))
    }
}

/**
 * 搜索页内容，包含搜索框和热门搜索卡片。
 */
@Composable
internal fun SearchDesktopContent() {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        TitleText("搜索")
        Box(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Color.White).padding(horizontal = 16.dp, vertical = 14.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text("想听点什么？", color = Color(0xFF666666), fontSize = 15.sp)
        }
        Text("大家都在搜", color = desktopColors.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        SearchCardGrid()
    }
}

/**
 * 音乐库页内容。
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun LibraryDesktopContent(onOpenPlaylist: () -> Unit) {
    GridPage("我的音乐库", libraryCards, onOpenPlaylist)
}

/**
 * 专辑列表页内容。
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun AlbumsDesktopContent(onOpenAlbum: () -> Unit) {
    GridPage("所有专辑", allAlbums, onOpenAlbum)
}

/**
 * 艺术家列表页内容。
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ArtistsDesktopContent(onOpenArtist: () -> Unit) {
    GridPage("艺术家", allArtists, onOpenArtist)
}

/**
 * 通用网格页面骨架，用于音乐库/专辑/艺术家列表等结构相似的页面。
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GridPage(title: String, albums: List<AlbumCardData>, onOpen: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        TitleText(title)
        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(albums) { card -> AlbumCard(card = card, onClick = onOpen) }
        }
    }
}

/**
 * 歌单详情页内容。
 */
@Composable
internal fun PlaylistDetailDesktopContent(
    onOpenAlbum: () -> Unit,
    onOpenArtist: () -> Unit,
) {
    DetailPageScaffold(playlistHeader, 64.sp, true) {
        SongSection(title = null, songs = recommendedSongs, onOpenAlbum = onOpenAlbum, onOpenArtist = onOpenArtist)
    }
}

/**
 * 专辑详情页内容。
 */
@Composable
internal fun AlbumDetailDesktopContent(title: String, onOpenArtist: () -> Unit) {
    DetailPageScaffold(sampleAlbumDetail.copy(title = title), 64.sp, false) {
        AlbumTrackSection(onOpenArtist = onOpenArtist)
    }
}

/**
 * 艺术家详情页内容。
 */
@Composable
internal fun ArtistDetailDesktopContent(name: String, onOpenAlbum: () -> Unit) {
    val detail = sampleArtistDetail.copy(name = name)
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(40.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
            CoverSwatch(Modifier.size(232.dp), detail.accent, circular = true, glyph = painterResource(Res.drawable.person_24px))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("认证艺术家", color = Color(0xFF8AB4FF), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(detail.name, color = desktopColors.textPrimary, fontSize = 72.sp, lineHeight = 72.sp, fontWeight = FontWeight.Bold)
                Text(detail.monthlyListeners, color = desktopColors.textSecondary, fontSize = 16.sp)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
            GreenPlayButton(56.dp)
            Text(
                "关注",
                color = desktopColors.textPrimary,
                modifier = Modifier.clip(RoundedCornerShape(16.dp)).border(1.dp, desktopColors.textSecondary, RoundedCornerShape(16.dp)).padding(horizontal = 16.dp, vertical = 6.dp),
                fontWeight = FontWeight.SemiBold,
            )
            ResourceIcon(Res.drawable.more_horiz_24px, null, desktopColors.textSecondary)
        }
        SongSection("热门", artistHotSongs, showAlbumColumn = false, showMetaColumn = false)
        AlbumSection("发行", listOf(sampleAlbumDetail.toAlbumCardData()), onOpenAlbum = onOpenAlbum)
    }
}

/**
 * 歌单/专辑详情共用头部布局。
 */
@Composable
private fun DetailPageScaffold(
    header: DetailHeaderData,
    titleFontSize: TextUnit,
    showFilledFavorite: Boolean,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.Bottom) {
            CoverSwatch(
                modifier = Modifier.size(232.dp),
                accent = header.accent,
                circular = header.circular,
                glyph = painterResource(Res.drawable.album_24px),
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(header.eyebrow, color = desktopColors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(header.title, color = desktopColors.textPrimary, fontSize = titleFontSize, lineHeight = titleFontSize, fontWeight = FontWeight.Bold)
                Text(header.subtitle, color = desktopColors.textSecondary, fontSize = 14.sp)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
            GreenPlayButton(56.dp)
            ResourceIcon(
                if (showFilledFavorite) Res.drawable.favorite_24px else Res.drawable.favorite_border_24px,
                null,
                if (showFilledFavorite) desktopColors.theme else desktopColors.textSecondary,
                modifier = Modifier.size(32.dp),
            )
            ResourceIcon(Res.drawable.more_horiz_24px, null, desktopColors.textSecondary)
        }
        content()
    }
}

/**
 * 专辑详情中的曲目列表。
 */
@Composable
private fun AlbumTrackSection(onOpenArtist: () -> Unit) {
    SongTable(
        songs = albumTrackSongs,
        columns = SongTableColumns(
            showAlbumColumn = false,
            showMetaColumn = false,
        ),
        accentColor = { _, music -> prototypeSongAccent(music) },
        onOpenArtist = { onOpenArtist() },
    )
}
