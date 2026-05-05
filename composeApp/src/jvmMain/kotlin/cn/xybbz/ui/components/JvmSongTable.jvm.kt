package cn.xybbz.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextAlign
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import cn.xybbz.common.utils.DateUtil
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.ui.screens.desktopColors
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyRow


/**
 * LazyListScope 版本的歌曲表格构建器。
 * 通过一个 header item 加一组 row items 组合，便于首页继续使用 LazyColumn。
 */
internal fun LazyListScope.songTableItems(
    tableKey: String,
    songs: List<XyMusic>,
    columns: SongTableColumns = SongTableColumns(),
    albumText: (XyMusic) -> String = ::defaultSongAlbumText,
    metaText: (XyMusic) -> String = { "" },
    durationText: (XyMusic) -> String = { DateUtil.millisecondsToTime(it.runTimeTicks) },
    accentColor: (Int, XyMusic) -> androidx.compose.ui.graphics.Color = ::defaultSongAccentColor,
    ifFavorite: (XyMusic) -> Boolean = { it.ifFavoriteStatus },
    ifPlay: (XyMusic) -> Boolean = { false },
    isSelected: (XyMusic) -> Boolean = { false },
    onSongClick: (Int, XyMusic) -> Unit = { _, _ -> },
    onOpenAlbum: (XyMusic) -> Unit = {},
    onOpenArtist: (XyMusic) -> Unit = {},
    onFavoriteClick: (XyMusic) -> Unit = {},
    onDownloadClick: (XyMusic) -> Unit = {},
    onAddToPlaylistClick: (XyMusic) -> Unit = { music ->
        AddPlaylistBottomData(
            ifShow = true,
            musicInfoList = listOf(music.itemId),
        ).show()
    },
    onMoreClick: (XyMusic) -> Unit = { music -> music.show() },
    onSelectionClick: (String) -> Unit = {},
    showViewArtistMenuItem: Boolean = true,
    showViewAlbumMenuItem: Boolean = true,
) {
    item(key = "${tableKey}_table_header") {
        SongTableHeader(columns = columns)
    }
    itemsIndexed(
        items = songs,
        key = { index, music -> "${tableKey}_${music.itemId}_$index" }
    ) { index, music ->
        SongRow(
            music = music,
            index = index,
            columns = columns,
            ifFavorite = ifFavorite(music),
            ifPlay = ifPlay(music),
            albumText = albumText(music),
            metaText = metaText(music),
            durationText = durationText(music),
            accentColor = accentColor(index, music),
            isSelected = isSelected(music),
            onClick = { onSongClick(index, music) },
            onOpenAlbum = { onOpenAlbum(music) },
            onOpenArtist = { onOpenArtist(music) },
            onFavoriteClick = { onFavoriteClick(music) },
            onDownloadClick = { onDownloadClick(music) },
            onAddToPlaylistClick = { onAddToPlaylistClick(music) },
            onMoreClick = { onMoreClick(music) },
            onSelectionClick = onSelectionClick,
            showViewArtistMenuItem = showViewArtistMenuItem,
            showViewAlbumMenuItem = showViewAlbumMenuItem,
        )
    }
}

/**
 * Paging 版本的歌曲表格构建器。
 * 使用 LazyPagingItems[index] 渲染行，保留滚动接近底部时自动加载下一页的行为。
 */
internal fun LazyListScope.songTableItems(
    tableKey: String,
    pagingItems: LazyPagingItems<XyMusic>,
    columns: SongTableColumns = SongTableColumns(),
    albumText: (XyMusic) -> String = ::defaultSongAlbumText,
    metaText: (XyMusic) -> String = { "" },
    durationText: (XyMusic) -> String = { DateUtil.millisecondsToTime(it.runTimeTicks) },
    accentColor: (Int, XyMusic) -> androidx.compose.ui.graphics.Color = ::defaultSongAccentColor,
    ifFavorite: (XyMusic) -> Boolean = { it.ifFavoriteStatus },
    ifPlay: (XyMusic) -> Boolean = { false },
    isSelected: (XyMusic) -> Boolean = { false },
    onSongClick: (Int, XyMusic) -> Unit = { _, _ -> },
    onOpenAlbum: (XyMusic) -> Unit = {},
    onOpenArtist: (XyMusic) -> Unit = {},
    onFavoriteClick: (XyMusic) -> Unit = {},
    onDownloadClick: (XyMusic) -> Unit = {},
    onAddToPlaylistClick: (XyMusic) -> Unit = { music ->
        AddPlaylistBottomData(
            ifShow = true,
            musicInfoList = listOf(music.itemId),
        ).show()
    },
    onMoreClick: (XyMusic) -> Unit = { music -> music.show() },
    onSelectionClick: (String) -> Unit = {},
    showViewArtistMenuItem: Boolean = true,
    showViewAlbumMenuItem: Boolean = true,
) {
    item(key = "${tableKey}_table_header") {
        SongTableHeader(columns = columns)
    }
    items(
        count = pagingItems.itemCount,
        key = pagingItems.itemKey { music -> "${tableKey}_${music.itemId}" },
        contentType = pagingItems.itemContentType { "${tableKey}_song_table_row" },
    ) { index ->
        pagingItems[index]?.let { music ->
            SongRow(
                music = music,
                index = index,
                columns = columns,
                ifFavorite = ifFavorite(music),
                ifPlay = ifPlay(music),
                albumText = albumText(music),
                metaText = metaText(music),
                durationText = durationText(music),
                accentColor = accentColor(index, music),
                isSelected = isSelected(music),
                onClick = { onSongClick(index, music) },
                onOpenAlbum = { onOpenAlbum(music) },
                onOpenArtist = { onOpenArtist(music) },
                onFavoriteClick = { onFavoriteClick(music) },
                onDownloadClick = { onDownloadClick(music) },
                onAddToPlaylistClick = { onAddToPlaylistClick(music) },
                onMoreClick = { onMoreClick(music) },
                onSelectionClick = onSelectionClick,
                showViewArtistMenuItem = showViewArtistMenuItem,
                showViewAlbumMenuItem = showViewAlbumMenuItem,
            )
        }
    }
}

/**
 * 歌曲表格头部。
 * 根据列配置决定显示哪些标题，以及哪些列只保留占位。
 */
@Composable
internal fun SongTableHeader(
    columns: SongTableColumns = SongTableColumns(),
) {
    Column(modifier = androidx.compose.ui.Modifier.fillMaxWidth()) {
        XyRow(
            paddingValues = PaddingValues(
                horizontal = XyTheme.dimens.innerHorizontalPadding,
                vertical = XyTheme.dimens.outerVerticalPadding
            )
        ) {
            SongTableCell("标题", SongTableDefaults.titleWidth, desktopColors.textSecondary)
            if (columns.showFavoriteColumn) {
                SongTableSpacer(SongTableDefaults.favoriteWidth)
            }
            if (columns.showInlineActions) {
                SongTableSpacer(SongTableDefaults.actionsWidth)
            }
            if (columns.showAlbumColumn) {
                SongTableCell("专辑", SongTableDefaults.albumWidth, desktopColors.textSecondary)
            }
            if (columns.showMetaColumn) {
                SongTableCell("添加时间", SongTableDefaults.metaWidth, desktopColors.textSecondary)
            }
            if (columns.showDurationColumn) {
                SongTableCell(
                    "时长",
                    SongTableDefaults.durationWidth,
                    desktopColors.textSecondary,
                    textAlign = TextAlign.End
                )
            }
            if (columns.showSelectionColumn) {
                SongTableSpacer(SongTableDefaults.selectionWidth)
            }
        }
        HorizontalDivider(color = desktopColors.divider)
    }
}
