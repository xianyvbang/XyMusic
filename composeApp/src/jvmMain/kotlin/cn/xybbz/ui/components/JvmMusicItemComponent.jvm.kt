package cn.xybbz.ui.components

import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cn.xybbz.config.image.rememberMusicCoverUrls
import cn.xybbz.entity.data.ext.joinToString
import cn.xybbz.entity.data.music.OnMusicPlayParameter
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.ui.xy.JvmItemTrailingArrowRight
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.more_vert_24px
import xymusic_kmp.composeapp.generated.resources.other_operations_button_suffix

/**
 * JVM 桌面端列表使用的音乐/歌单列表项。
 * 视觉结构参考桌面原型里的 SongRow，底层使用 ListItem 实现。
 */
@Composable
fun JvmMusicItemComponent(
    modifier: Modifier = Modifier,
    music: XyMusic,
    enabledPic: Boolean = true,
    onIfFavorite: () -> Boolean,
    ifDownload: Boolean,
    subordination: String? = null,
    backgroundColor: Color = Color.Transparent,
    brush: Brush? = null,
    ifPlay: Boolean,
    onMusicPlay: (OnMusicPlayParameter) -> Unit,
    trailingIcon: DrawableResource = Res.drawable.more_vert_24px,
    ifShowTrailingContent: Boolean = true,
    ifSelect: Boolean = false,
    trailingOnSelectClick: ((Boolean) -> Unit)? = null,
    trailingOnClick: () -> Unit,
    ifSelectCheckBox: (() -> Boolean)? = null
) {

    val coverUrls = rememberMusicCoverUrls(music)

    JvmMusicItemComponent(
        modifier = modifier,
        enabledPic = enabledPic,
        itemId = music.itemId,
        name = music.name,
        album = music.album,
        artists = music.artists?.joinToString(),
        pic = coverUrls.primaryUrl,
        backPic = coverUrls.fallbackUrl,
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
        trailingIcon = trailingIcon,
        ifShowTrailingContent = ifShowTrailingContent
    )
}


@Composable
private fun JvmMusicItemComponent(
    modifier: Modifier = Modifier,
    itemId: String,
    name: String,
    album: String = "",
    artists: String? = "",
    pic: String? = "",
    backPic: String? = null,
    codec: String? = "",
    bitRate: Int? = 0,
    enabledPic: Boolean = true,
    onIfFavorite: () -> Boolean,
    ifDownload: Boolean,
    subordination: String? = null,
    backgroundColor: Color = Color.Transparent,
    brush: Brush? = null,
    ifPlay: Boolean,
    onMusicPlay: (OnMusicPlayParameter) -> Unit,
    trailingIcon: DrawableResource = Res.drawable.more_vert_24px,
    trailingContentDescription: String = "${name}${stringResource(Res.string.other_operations_button_suffix)}",
    ifShowTrailingContent: Boolean = true,
    ifSelect: Boolean = false,
    trailingOnSelectClick: ((Boolean) -> Unit)? = null,
    trailingOnClick: () -> Unit,
    ifSelectCheckBox: (() -> Boolean)? = null
) {
    JvmItemTrailingArrowRight(
        name = name,
        subordination = subordination ?: (artists ?: ""),
        favoriteState = onIfFavorite(),
        imgUrl = pic,
        backImgUrl = backPic,
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
                            painter = painterResource(trailingIcon),
                            contentDescription = trailingContentDescription
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
