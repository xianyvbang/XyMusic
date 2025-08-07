package cn.xybbz.router

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import cn.xybbz.R
import cn.xybbz.localdata.enums.MusicDataTypeEnum
import cn.xybbz.ui.screens.HomeScreen
import cn.xybbz.ui.screens.SettingScreen
import cn.xybbz.ui.xy.XyItemText
import kotlinx.serialization.Serializable

/**
 * 路由地址常量
 * @author 刘梦龙
 * @date 2024/03/06
 * @constructor 创建[RouterConstants]
 * @param [router] 路由地址
 * @param [type] 类型 0 NavigationBar打开展示 1普通页面
 */
@Serializable
sealed class RouterConstants() {

    @Serializable
    data object Home : RouterConstants()

    @Serializable
    data object Search : RouterConstants()

    @Serializable
    data object Music : RouterConstants()

    /**
     * 艺术家
     */
    @Serializable
    data object Artist : RouterConstants()

    /**
     * 设置
     */
    @Serializable
    data object Setting : RouterConstants()

    /**
     * 专辑详细
     */
//    data object AlbumInfo : RouterConstants("albumInfo")

    @Serializable
    data class AlbumInfo(
        /**
         * 专辑id/歌单id
         */
        val itemId: String,
        /**
         * 数据类型0专辑,1歌单
         */
        val dataType: MusicDataTypeEnum
    ) : RouterConstants()

    /**
     * 收藏列表
     */
    @Serializable
    data object FavoriteList : RouterConstants()

    /**
     * 专辑推荐页面
     */
    @Serializable
    data object Album : RouterConstants()

    /**
     * 页面
     */
    @Serializable
    data object Screen : RouterConstants()

    /**
     * 连接管理
     */
    @Serializable
    data object ConnectionManagement : RouterConstants()

    /**
     * 艺术家详细
     */
    @Serializable
    data class ArtistInfo(
        val artistId: String
    ) : RouterConstants()

    /**
     * 连接服务页面
     */
    @Serializable
    data class Connection(
        val connectionUiType: String? = null
    ) : RouterConstants()

    /**
     * 连接详情
     */
    @Serializable
    data class ConnectionInfo(
        val connectionId: Long
    ) : RouterConstants()

    /**
     * 存储管理
     */
    @Serializable
    data object MemoryManagement : RouterConstants()

    /**
     * 界面设置
     */
    @Serializable
    data object InterfaceSetting : RouterConstants()

    /**
     * 语言设置
     */
    @Serializable
    data object LanguageConfig : RouterConstants()

    /**
     * 流派列表
     */
    @Serializable
    data object Genres : RouterConstants()

    /**
     * 流派详情
     */
    @Serializable
    data class GenreInfo(
        val genreId: String
    ) : RouterConstants()

    @Serializable
    data object About : RouterConstants()

    @Serializable
    data object CacheLimit : RouterConstants()

}

sealed class MainScreen<T : RouterConstants>(
    val route: T,
    val tabLabel: @Composable (() -> Unit),
    val icon: @Composable ((Boolean) -> Unit),
    val content: @Composable (() -> Unit),
) {

    data object Home : MainScreen<RouterConstants.Home>(
        route = RouterConstants.Home,
        tabLabel = {
            XyItemText(text = stringResource(id = R.string.home))
        },
        icon = {
            Icon(
                if (it) Icons.Rounded.Home else Icons.Outlined.Home,
                contentDescription = stringResource(id = R.string.home)
            )
        }, content = {
            HomeScreen()
        })

    data object Setting : MainScreen<RouterConstants.Setting>(
        route = RouterConstants.Setting,
        tabLabel = {
            XyItemText(text = stringResource(id = R.string.setting))
        },
        icon = {
            Icon(
                if (it) Icons.Rounded.Settings else Icons.Outlined.Settings,
                contentDescription = stringResource(id = R.string.setting)
            )
        },
        content = {
            SettingScreen()
        })
}

val PagItems = listOf(
    MainScreen.Home,
    MainScreen.Setting
)