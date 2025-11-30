package cn.xybbz.ui.components


import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cn.xybbz.R
import cn.xybbz.entity.data.music.OnMusicPlayParameter
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.ui.xy.ItemTrailingContent
import com.google.common.collect.Multimaps.index

/**
 * 音乐列表item
 * @param [modifier] 修饰符
 * @param [onMusicData] 关于音乐信息数据
 * @param [index] 索引
 * @param [onMusicPlay] 播放方法
 */

@Composable
fun MusicItemComponent(
    modifier: Modifier = Modifier,
    music: XyMusic,
    index: Int? = null,
    enabledPic: Boolean = true,
    onIfFavorite: () -> Boolean,
    ifDownload: Boolean,
    subordination: String? = null,
    backgroundColor: Color = Color.Transparent,
    brush: Brush? = null,
    ifPlay: Boolean,
    onMusicPlay: (OnMusicPlayParameter) -> Unit,
    ifShowTrailingContent: Boolean = true,
    ifSelect: Boolean = false,
    trailingOnSelectClick: ((Boolean) -> Unit)? = null,
    trailingOnClick: () -> Unit,
    ifSelectCheckBox: (() -> Boolean)? = null
) {
    MusicItemComponent(
        modifier = modifier,
        index = index,
        enabledPic = enabledPic,
        itemId = music.itemId,
        name = music.name,
        album = music.album,
        artists = music.artists,
        pic = music.pic,
        codec = music.codec,
        bitRate = music.bitRate,
        subordination = subordination,
        onIfFavorite = onIfFavorite,
        ifDownload = ifDownload,
        ifPlay = ifPlay,
        backgroundColor = backgroundColor,
        brush = brush,
        ifSelect = ifSelect,
        trailingOnSelectClick = trailingOnSelectClick,
        trailingOnClick = trailingOnClick,
        ifSelectCheckBox = ifSelectCheckBox,
        onMusicPlay = onMusicPlay,
        ifShowTrailingContent = ifShowTrailingContent
    )
}

@Composable
fun MusicItemComponent(
    modifier: Modifier = Modifier,
    itemId: String,
    name: String,
    album: String = "",
    artists: String? = "",
    pic: String? = "",
    codec: String? = "",
    bitRate: Int? = 0,
    index: Int? = null,
    enabledPic: Boolean = true,
    onIfFavorite: () -> Boolean,
    ifDownload: Boolean,
    subordination: String? = null,
    backgroundColor: Color = Color.Transparent,
    brush: Brush? = null,
    ifPlay:Boolean,
    onMusicPlay: (OnMusicPlayParameter) -> Unit,
    ifShowTrailingContent: Boolean = true,
    ifSelect: Boolean = false,
    trailingOnSelectClick: ((Boolean) -> Unit)? = null,
    trailingOnClick: () -> Unit,
    ifSelectCheckBox: (() -> Boolean)? = null
) {

    ItemTrailingContent(
        name = name,
        subordination = subordination ?: (artists ?: ""),
        favoriteState = onIfFavorite(),
        imgUrl = pic,
        index = index,
        media = getMusicMedia(codec, bitRate),
        enabledPic = enabledPic,
        ifDownload = ifDownload,
        backgroundColor = backgroundColor,
        brush = brush,
        ifPlay = ifPlay,
        modifier = modifier,
        onClick = {
            if (!ifSelect)
                onMusicPlay.invoke(
                    OnMusicPlayParameter(
                        musicId = itemId,
                        albumId = album
                    )
                )
            else {
                trailingOnSelectClick?.invoke(ifSelectCheckBox?.invoke() == true)
            }
        },
        trailingContent = {
            if (ifShowTrailingContent)
                if (!ifSelect)
                    IconButton(
                        modifier = Modifier.offset(x = (10).dp),
                        onClick = {
                            trailingOnClick.invoke()
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "${name}${stringResource(R.string.other_operations_button_suffix)}"
                        )
                    }
                else {
                    IconButton(
                        modifier = Modifier.offset(x = (10).dp),
                        onClick = {
                            trailingOnSelectClick?.invoke(ifSelectCheckBox?.invoke() == true)
                        },
                    ) {
                        RadioButton(selected = ifSelectCheckBox?.invoke() == true, onClick = {
                            trailingOnSelectClick?.invoke(ifSelectCheckBox?.invoke() == true)
                        })
                    }

                }

        }
    )

}

/**
 * 音乐列表item
 * @param [modifier] 修饰符
 * @param [index] 索引
 */
@Composable
fun MusicItemNotClickComponent(
    modifier: Modifier = Modifier,
    music: XyMusic,
    ifDownload: Boolean,
    ifPlay: Boolean = false,
    subordination: String? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    brush: Brush? = null
) {

    MusicItemComponent(
        itemId = music.itemId,
        name = music.name,
        album = music.album,
        artists = music.artists,
        pic = music.pic,
        codec = music.codec,
        bitRate = music.bitRate,
        subordination = subordination,
        backgroundColor = backgroundColor,
        brush = brush,
        modifier = modifier,
        onIfFavorite = { false },
        ifDownload = ifDownload,
        ifPlay = ifPlay,
        ifShowTrailingContent = false,
        trailingOnClick = {},
        onMusicPlay = {},
    )

}


/**
 * 音乐列表的index item
 * @param [modifier] 修饰符
 * @param [onMusicData] 关于音乐信息数据
 * @param [index] 索引
 * @param [onMusicPlay] 播放方法
 */
@Composable
fun MusicItemIndexComponent(
    modifier: Modifier = Modifier,
    music: XyMusic,
    onIfFavorite: () -> Boolean,
    ifDownload: Boolean,
    subordination: String? = null,
    index: Int,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    onMusicPlay: (OnMusicPlayParameter) -> Unit,
    ifSelect: Boolean = false,
    ifPlay: Boolean,
    trailingOnSelectClick: (Boolean) -> Unit,
    trailingOnClick: () -> Unit,
    ifSelectCheckBox: (() -> Boolean)? = null
) {

    MusicItemComponent(
        itemId = music.itemId,
        name = music.name,
        album = music.album,
        artists = music.artists,
        pic = music.pic,
        codec = music.codec,
        bitRate = music.bitRate,
        index = index,
        subordination = subordination,
        onIfFavorite = onIfFavorite,
        ifDownload = ifDownload,
        ifPlay = ifPlay,
        modifier = modifier,
        backgroundColor = backgroundColor,
        ifSelect = ifSelect,
        trailingOnSelectClick = trailingOnSelectClick,
        trailingOnClick = trailingOnClick,
        ifSelectCheckBox = ifSelectCheckBox,
        onMusicPlay = onMusicPlay,
    )
}

/**
 * 获得音乐的media信息字符串
 */
fun getMusicMedia(codec: String? = "", bitRate: Int? = 0): String {
    return "$codec ${bitRate?.div(1000)}k"
}