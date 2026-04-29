package cn.xybbz.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import cn.xybbz.common.utils.DateUtil
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.ui.screens.desktopColors
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyRow
import androidx.compose.ui.text.style.TextAlign

/**
 * 非懒加载场景下的歌曲表格容器。
 * 供桌面原型页等直接使用整块表格的地方复用。
 */
@Composable
internal fun SongTable(
    songs: List<XyMusic>,
    columns: SongTableColumns = SongTableColumns(),
    albumText: (XyMusic) -> String = { it.albumName.orEmpty() },
    metaText: (XyMusic) -> String = { "" },
    durationText: (XyMusic) -> String = { DateUtil.millisecondsToTime(it.runTimeTicks) },
    accentColor: (Int, XyMusic) -> androidx.compose.ui.graphics.Color = ::defaultSongAccentColor,
    ifFavorite: (XyMusic) -> Boolean = { it.ifFavoriteStatus },
    ifPlay: (XyMusic) -> Boolean = { false },
    onSongClick: (XyMusic) -> Unit = {},
    onOpenAlbum: (XyMusic) -> Unit = {},
    onOpenArtist: (XyMusic) -> Unit = {},
    onFavoriteClick: (XyMusic) -> Unit = {},
) {
    Column(modifier = androidx.compose.ui.Modifier.fillMaxWidth()) {
        SongTableHeader(columns = columns)
        songs.forEachIndexed { index, music ->
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
                onClick = { onSongClick(music) },
                onOpenAlbum = { onOpenAlbum(music) },
                onOpenArtist = { onOpenArtist(music) },
                onFavoriteClick = { onFavoriteClick(music) },
            )
        }
    }
}

/**
 * LazyListScope 版本的歌曲表格构建器。
 * 通过一个 header item 加一组 row items 组合，便于首页继续使用 LazyColumn。
 */
internal fun LazyListScope.songTableItems(
    tableKey: String,
    songs: List<XyMusic>,
    columns: SongTableColumns = SongTableColumns(),
    albumText: (XyMusic) -> String = { it.albumName.orEmpty() },
    metaText: (XyMusic) -> String = { "" },
    durationText: (XyMusic) -> String = { DateUtil.millisecondsToTime(it.runTimeTicks) },
    accentColor: (Int, XyMusic) -> androidx.compose.ui.graphics.Color = ::defaultSongAccentColor,
    ifFavorite: (XyMusic) -> Boolean = { it.ifFavoriteStatus },
    ifPlay: (XyMusic) -> Boolean = { false },
    onSongClick: (XyMusic) -> Unit = {},
    onOpenAlbum: (XyMusic) -> Unit = {},
    onOpenArtist: (XyMusic) -> Unit = {},
    onFavoriteClick: (XyMusic) -> Unit = {},
) {
    item(key = "${tableKey}_table_header") {
        SongTableHeader(columns = columns)
    }
    itemsIndexed(
        items = songs,
        key = { _, music -> music.itemId }
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
            onClick = { onSongClick(music) },
            onOpenAlbum = { onOpenAlbum(music) },
            onOpenArtist = { onOpenArtist(music) },
            onFavoriteClick = { onFavoriteClick(music) },
        )
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
        }
        HorizontalDivider(color = desktopColors.divider)
    }
}
