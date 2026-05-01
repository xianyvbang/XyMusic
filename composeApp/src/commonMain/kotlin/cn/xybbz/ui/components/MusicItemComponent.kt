/*
 *   XyMusic
 *   Copyright (C) 2023 xianyvbang
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package cn.xybbz.ui.components


import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cn.xybbz.config.image.rememberMusicCoverUrls
import cn.xybbz.entity.data.ext.joinToString
import cn.xybbz.entity.data.music.OnMusicPlayParameter
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.ui.xy.ItemTrailingContent
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.more_vert_24px
import xymusic_kmp.composeapp.generated.resources.other_operations_button_suffix
import cn.xybbz.ui.xy.XyIconButton as IconButton

/**
 * 音乐列表item
 * @param [modifier] 修饰符
 * @param [onMusicPlay] 播放方法
 */

@Composable
fun MusicItemComponent(
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

    MusicItemComponent(
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
private fun MusicItemComponent(
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
    ItemTrailingContent(
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

/**
 * 音乐列表item
 * @param [modifier] 修饰符
 */
@Composable
fun MusicItemNotClickComponent(
    modifier: Modifier = Modifier,
    music: XyMusic,
    ifDownload: Boolean,
    ifPlay: Boolean = false,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    picSize: Dp = 62.dp,
    brush: Brush? = null,
    favoriteState: Boolean,
    trailingOnClick: () -> Unit,
) {
    val coverUrls = rememberMusicCoverUrls(music)

    ItemTrailingContent(
        name = music.name,
        subordination = music.artists?.joinToString(),
        imgUrl = coverUrls.primaryUrl,
        backImgUrl = coverUrls.fallbackUrl,
        media = getMusicMedia(music.codec, music.bitRate),
        backgroundColor = backgroundColor,
        picSize = picSize,
        brush = brush,
        modifier = modifier,
        favoriteState = false,
        ifDownload = ifDownload,
        ifPlay = ifPlay,
        trailingContent = {
            FavoriteIconButton(
                isFavorite = favoriteState,
                modifier = Modifier.offset(x = (10).dp),
                onClick = {
                    trailingOnClick.invoke()
                },
                normalTint = MaterialTheme.colorScheme.onSurface,
            )
        }
    )
}

/**
 * 获得音乐的media信息字符串
 */
fun getMusicMedia(codec: String? = "", bitRate: Int? = 0): String {
    return "$codec ${bitRate?.div(1000)}k"
}

