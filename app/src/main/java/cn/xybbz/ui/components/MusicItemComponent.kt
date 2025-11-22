package cn.xybbz.ui.components


import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
    onMusicData: () -> XyMusic,
    index: Int? = null,
    enabledPic:Boolean = true,
    onIfFavorite: () -> Boolean,
    subordination: String? = null,
    backgroundColor: Color = Color.Transparent,
    brush: Brush? = null,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    onMusicPlay: (OnMusicPlayParameter) -> Unit,
    ifShowTrailingContent: Boolean = true,
    ifSelect: Boolean = false,
    trailingOnClick: ((Boolean) -> Unit)? = null,
    ifSelectCheckBox: (() -> Boolean)? = null
) {

    val xyMusicData by remember {
        mutableStateOf(onMusicData())
    }

    ItemTrailingContent(
        name = xyMusicData.name,
        subordination = subordination ?: (xyMusicData.artists ?: ""),
        favoriteState = onIfFavorite(),
        imgUrl = xyMusicData.pic,
        index = index,
        media = getMusicMedia(xyMusicData),
        enabledPic= enabledPic,
        backgroundColor = backgroundColor,
        brush = brush,
        textColor = textColor,
        modifier = modifier,
        onClick = {
            if (!ifSelect)
                onMusicPlay.invoke(
                    OnMusicPlayParameter(
                        musicId = xyMusicData.itemId,
                        albumId = xyMusicData.album
                    )
                )
            else {
                trailingOnClick?.invoke(ifSelectCheckBox?.invoke() == true)
            }
        },
        trailingContent = {
            if (ifShowTrailingContent)
                if (!ifSelect)
                    IconButton(
                        modifier = Modifier.offset(x = (10).dp),
                        onClick = {
                            xyMusicData.show()
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "${xyMusicData.name}${stringResource(R.string.other_operations_button_suffix)}"
                        )
                    }
                else {
                    IconButton(
                        modifier = Modifier.offset(x = (10).dp),
                        onClick = {
                            trailingOnClick?.invoke(ifSelectCheckBox?.invoke() == true)
                        },
                    ) {
                        RadioButton(selected = ifSelectCheckBox?.invoke() == true, onClick = {
                            trailingOnClick?.invoke(ifSelectCheckBox?.invoke() == true)
                        })
                    }

                }

        }
    )

}

/**
 * 音乐列表item
 * @param [modifier] 修饰符
 * @param [onMusicData] 关于音乐信息数据
 * @param [index] 索引
 * @param [onMusicPlay] 播放方法
 */
@Composable
fun MusicItemNotClickComponent(
    modifier: Modifier = Modifier,
    onMusicData: () -> XyMusic,
    subordination: String? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    brush: Brush? = null
) {

    MusicItemComponent(
        onMusicData = onMusicData,
        subordination = subordination,
        backgroundColor = backgroundColor,
        brush = brush,
        modifier = modifier,
        onIfFavorite = { false },
        ifShowTrailingContent = false,
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
    onMusicData: () -> XyMusic,
    onIfFavorite: () -> Boolean,
    subordination: String? = null,
    index: Int,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    onMusicPlay: (OnMusicPlayParameter) -> Unit,
    ifSelect: Boolean = false,
    trailingOnClick: ((Boolean) -> Unit)? = null,
    ifSelectCheckBox: (() -> Boolean)? = null
) {

    MusicItemComponent(
        onMusicData = onMusicData,
        index = index,
        subordination = subordination,
        onIfFavorite = onIfFavorite,
        textColor = textColor,
        modifier = modifier,
        backgroundColor = backgroundColor,
        ifSelect = ifSelect,
        trailingOnClick = trailingOnClick,
        ifSelectCheckBox = ifSelectCheckBox,
        onMusicPlay = onMusicPlay,
    )
}

/**
 * 获得音乐的media信息字符串
 */
fun getMusicMedia(music: XyMusic): String {
    return "${music.codec} ${music.bitRate?.div(1000)}k"
}