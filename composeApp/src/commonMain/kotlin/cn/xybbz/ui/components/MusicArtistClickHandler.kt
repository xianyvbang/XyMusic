package cn.xybbz.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import cn.xybbz.common.constants.Constants.ARTIST_DELIMITER_SEMICOLON
import cn.xybbz.common.constants.Constants.SLASH_DELIMITER
import cn.xybbz.compositionLocal.LocalNavigator
import cn.xybbz.localdata.common.LocalConstants
import cn.xybbz.localdata.data.album.XyAlbum
import cn.xybbz.localdata.data.music.XyMusic
import cn.xybbz.localdata.data.music.XyPlayMusic
import cn.xybbz.router.ArtistInfo
import cn.xybbz.viewmodel.MusicBottomMenuViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

/**
 * 统一处理“打开艺术家”的点击行为。
 *
 * 该类只暴露不同数据来源的便捷入口，实际的单艺术家跳转、多艺术家弹窗逻辑由
 * [rememberMusicArtistClickHandler] 在 Compose 作用域内完成，避免各页面重复判断。
 *
 * @property onOpenArtists 统一入口，参数分别为艺术家 id 列表和同索引的艺术家名称列表。
 */
class MusicArtistClickHandler internal constructor(
    private val onOpenArtists: (List<String>?, List<String>?) -> Unit,
) {
    /**
     * 根据艺术家 id 列表打开艺术家。
     *
     * 多个艺术家会先弹出艺术家选择列表，单个艺术家会直接进入详情页。
     */
    fun openArtists(
        artistIds: List<String>?,
        artists: List<String>?,
    ) {
        onOpenArtists(artistIds, artists)
    }

    /**
     * 打开单个艺术家，主要用于已经拿到 [artistId] 的艺术家卡片。
     */
    fun openArtist(
        artistId: String?,
        artistName: String?,
    ) {
        onOpenArtists(
            artistId?.takeIf { it.isNotBlank() }?.let { listOf(it) },
            artistName?.let { listOf(it) },
        )
    }

    /**
     * 打开普通音乐数据中的艺术家。
     */
    fun openMusicArtists(music: XyMusic) {
        onOpenArtists(music.artistIds, music.artists)
    }

    /**
     * 打开正在播放音乐数据中的艺术家。
     */
    fun openPlayMusicArtists(music: XyPlayMusic) {
        onOpenArtists(music.artistIds, music.artists)
    }

    /**
     * 打开专辑数据中的艺术家。
     *
     * 专辑里的艺术家 id 和名称是字符串字段，这里会先拆成列表再走统一逻辑。
     */
    fun openAlbumArtists(album: XyAlbum?) {
        onOpenArtists(
            album?.artistIds.toArtistValueList(),
            album?.artists.toArtistValueList(),
        )
    }
}

/**
 * 记住一个通用的艺术家点击处理器，并在需要时托管多艺术家选择弹窗。
 *
 * @param musicBottomMenuViewModel 复用底部菜单 ViewModel 中的艺术家查询能力。
 * @param onBeforeOpen 打开艺术家前执行的动作，通常用于先隐藏当前菜单。
 * @param onBeforeSingleArtistNavigate 单艺术家直接跳转前执行的动作，通常用于关闭播放器弹层。
 * @param onAfterOpen 打开流程结束后的动作，通常用于收起当前 UI 状态。
 */
@Composable
fun rememberMusicArtistClickHandler(
    musicBottomMenuViewModel: MusicBottomMenuViewModel = koinViewModel<MusicBottomMenuViewModel>(),
    onBeforeOpen: suspend () -> Unit = {},
    onBeforeSingleArtistNavigate: suspend () -> Unit = {},
    onAfterOpen: () -> Unit = {},
): MusicArtistClickHandler {
    val navigator = LocalNavigator.current
    val coroutineScope = rememberCoroutineScope()

    // 使用 rememberUpdatedState 保持回调为最新值，同时避免重建 openArtists 闭包。
    val currentOnBeforeOpen = rememberUpdatedState(onBeforeOpen)
    val currentOnBeforeSingleArtistNavigate = rememberUpdatedState(onBeforeSingleArtistNavigate)
    val currentOnAfterOpen = rememberUpdatedState(onAfterOpen)

    /**
     * 是否展示多艺术家选择弹窗。
     *
     * 多艺术家场景会先查询艺术家信息，再由 [ArtistItemListBottomSheet] 展示可点击列表。
     */
    var ifShowArtistList by remember {
        mutableStateOf(false)
    }

    // 多艺术家弹窗在处理器内部托管，调用方只需要调用 openArtists 系列方法。
    ArtistItemListBottomSheet(
        artistList = musicBottomMenuViewModel.xyArtists,
        onIfShowArtistList = { ifShowArtistList },
        onSetShowArtistList = { ifShowArtistList = it },
    )

    val openArtists = remember(
        navigator,
        coroutineScope,
        musicBottomMenuViewModel,
    ) {
        openArtists@{ artistIds: List<String>?, artists: List<String>? ->
            // 先把 id 和名称对齐成路由数据，过滤掉空 id，避免无效跳转。
            val artistRoutes = artistIds.toArtistRoutes(artists)
            if (artistRoutes.isEmpty()) {
                return@openArtists
            }

            coroutineScope.launch {
                currentOnBeforeOpen.value()
                if (artistRoutes.size > 1) {
                    // 多艺术家：展示选择弹窗，并查询每个艺术家的完整信息用于卡片展示。
                    ifShowArtistList = true
                    musicBottomMenuViewModel.getArtistInfos(artistRoutes.map { it.artistId })
                } else {
                    // 单艺术家：无需弹窗，直接关闭相关 UI 后进入艺术家详情页。
                    currentOnBeforeSingleArtistNavigate.value()
                    val artistRoute = artistRoutes.first()
                    navigator.navigate(
                        ArtistInfo(
                            artistRoute.artistId,
                            artistRoute.artistName,
                        )
                    )
                }
            }.invokeOnCompletion {
                // 不论直接跳转还是弹出选择列表，都让调用方有机会清理当前菜单状态。
                currentOnAfterOpen.value()
            }
        }
    }

    return remember(openArtists) {
        MusicArtistClickHandler(openArtists)
    }
}

/**
 * 可直接进入艺术家详情页的路由数据。
 *
 * @property artistId 艺术家 id，是跳转详情页的必需参数。
 * @property artistName 艺术家名称，用于详情页初始化标题。
 */
private data class ArtistRoute(
    val artistId: String,
    val artistName: String,
)

/**
 * 将艺术家 id 列表和名称列表合并为路由数据。
 *
 * id 是跳转的关键字段，名称列表只按相同索引补充标题，缺失时使用空字符串。
 */
private fun List<String>?.toArtistRoutes(artists: List<String>?): List<ArtistRoute> {
    return orEmpty().mapIndexedNotNull { index, artistId ->
        val routeArtistId =
            artistId.trim().takeIf { it.isNotBlank() } ?: return@mapIndexedNotNull null
        ArtistRoute(
            artistId = routeArtistId,
            artistName = artists?.getOrNull(index).orEmpty(),
        )
    }
}

/**
 * 将专辑表中保存的艺术家字符串拆成列表。
 *
 * 兼容本地逗号、斜杠和旧的分号分隔格式，避免不同数据源写入格式不一致导致无法点击。
 */
private fun String?.toArtistValueList(): List<String> {
    return orEmpty()
        .split(LocalConstants.ARTIST_DELIMITER, SLASH_DELIMITER, ARTIST_DELIMITER_SEMICOLON)
        .map { it.trim() }
        .filter { it.isNotBlank() }
}
