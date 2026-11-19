package cn.xybbz.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import cn.xybbz.common.utils.DateUtil
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyRow
import cn.xybbz.ui.xy.XyText
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.stringResource
import xymusic.composeapp.generated.resources.*


/**
 * LazyListScope 版本的歌曲表格构建器。
 * 通过一个 header item 加一组 row items 组合，便于首页继续使用 LazyColumn。
 *
 * @param stickyTableHeader 表头是否吸附在列表顶部（音乐页等需要固定表头的场景传 true）。
 */
@OptIn(ExperimentalFoundationApi::class)
internal fun LazyListScope.songTableItems(
    tableKey: String,
    songs: List<XyMusic>,
    columns: SongTableColumns = SongTableColumns(),
    albumText: (XyMusic) -> String = ::defaultSongAlbumText,
    metaText: (XyMusic) -> String = { "" },
    durationText: (XyMusic) -> String = { DateUtil.millisecondsToTime(it.runTimeTicks) },
    accentColor: (Int, XyMusic) -> androidx.compose.ui.graphics.Color = ::defaultSongAccentColor,
    ifFavorite: (XyMusic) -> Boolean = { it.ifFavoriteStatus },
    currentPlayingMusicIdFlow: Flow<String?>,
    isSelected: (XyMusic) -> Boolean = { false },
    onSongClick: (Int, XyMusic) -> Unit,
    onOpenAlbum: (XyMusic) -> Unit,
    onOpenArtist: (XyMusic) -> Unit,
    onFavoriteClick: (XyMusic) -> Unit,
    onDownloadClick: (XyMusic) -> Unit,
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
    stickyTableHeader: Boolean = false,
) {
    songTableHeaderItem(tableKey = tableKey, columns = columns, sticky = stickyTableHeader)
    itemsIndexed(
        items = songs,
        key = { index, music -> "${tableKey}_${music.itemId}_$index" }
    ) { index, music ->
        SongRow(
            music = music,
            index = index,
            columns = columns,
            ifFavorite = ifFavorite(music),
            currentPlayingMusicIdFlow = currentPlayingMusicIdFlow,
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
 *
 * @param stickyTableHeader 表头是否吸附在列表顶部（音乐页等需要固定表头的场景传 true）。
 */
@OptIn(ExperimentalFoundationApi::class)
internal fun LazyListScope.songTableItems(
    tableKey: String,
    pagingItems: LazyPagingItems<XyMusic>,
    columns: SongTableColumns = SongTableColumns(),
    albumText: (XyMusic) -> String = ::defaultSongAlbumText,
    metaText: (XyMusic) -> String = { "" },
    durationText: (XyMusic) -> String = { DateUtil.millisecondsToTime(it.runTimeTicks) },
    accentColor: (Int, XyMusic) -> androidx.compose.ui.graphics.Color = ::defaultSongAccentColor,
    ifFavorite: (XyMusic) -> Boolean = { it.ifFavoriteStatus },
    currentPlayingMusicIdFlow: Flow<String?>,
    isSelected: (XyMusic) -> Boolean = { false },
    onSongClick: (Int, XyMusic) -> Unit,
    onOpenAlbum: (XyMusic) -> Unit,
    onOpenArtist: (XyMusic) -> Unit,
    onFavoriteClick: (XyMusic) -> Unit,
    onDownloadClick: (XyMusic) -> Unit,
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
    stickyTableHeader: Boolean = false,
) {
    songTableHeaderItem(tableKey = tableKey, columns = columns, sticky = stickyTableHeader)
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
                currentPlayingMusicIdFlow = currentPlayingMusicIdFlow,
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
 * 歌曲表格表头 item。
 *
 * 普通模式作为跟随滚动的首个 item，吸附模式则作为 stickyHeader 固定在列表顶部。
 * 吸附表头自带页面背景，避免滚动时下方歌曲行透出。
 *
 * @param tableKey 表格标识，用于生成稳定的 key。
 * @param columns 列开关配置。
 * @param sticky 是否吸附在列表顶部。
 */
@OptIn(ExperimentalFoundationApi::class)
internal fun LazyListScope.songTableHeaderItem(
    tableKey: String,
    columns: SongTableColumns,
    sticky: Boolean,
) {
    if (sticky) {
        stickyHeader(key = "${tableKey}_table_header") {
            SongTableHeader(
                columns = columns,
                modifier = Modifier.background(MaterialTheme.colorScheme.background),
            )
        }
    } else {
        item(key = "${tableKey}_table_header") {
            SongTableHeader(columns = columns)
        }
    }
}

/**
 * 歌曲表格头部。
 * 根据列配置决定显示哪些标题，以及哪些列只保留占位。
 *
 * @param columns 列开关配置。
 * @param modifier 外层修饰符，吸附场景下用于补充不透明背景。
 */
@Composable
internal fun SongTableHeader(
    columns: SongTableColumns = SongTableColumns(),
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        XyRow(
            paddingValues = PaddingValues(
                horizontal = XyTheme.dimens.innerHorizontalPadding,
                vertical = XyTheme.dimens.outerVerticalPadding
            ),
            horizontalArrangement = Arrangement.spacedBy(SongTableDefaults.columnSpacing),
        ) {
            // 标题列表头与行内标题一致，按剩余空间自适应，保证歌名尽量完整显示
            Box(
                modifier = Modifier.weight(SongTableDefaults.titleWeight),
                contentAlignment = Alignment.CenterStart,
            ) {
                XyText(
                    text = stringResource(Res.string.title),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Normal,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                )
            }
            if (columns.showFavoriteColumn) {
                SongTableSpacer(SongTableDefaults.favoriteWidth)
            }
            if (columns.showInlineActions) {
                SongTableSpacer(SongTableDefaults.actionsWidth)
            }
            if (columns.showAlbumColumn) {
                SongTableCell(stringResource(Res.string.album), SongTableDefaults.albumWidth, MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (columns.showMetaColumn) {
                SongTableCell(stringResource(Res.string.add_time), SongTableDefaults.metaWidth, MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (columns.showDurationColumn) {
                SongTableCell(
                    stringResource(Res.string.duration),
                    SongTableDefaults.durationWidth,
                    MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End
                )
            }
            if (columns.showSelectionColumn) {
                SongTableSpacer(SongTableDefaults.selectionWidth)
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}
