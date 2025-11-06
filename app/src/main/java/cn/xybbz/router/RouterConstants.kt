package cn.xybbz.router

import cn.xybbz.localdata.enums.MusicDataTypeEnum
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

    /**
     * 首页
     */
    @Serializable
    data object Home : RouterConstants()

    /**
     * 搜索页面
     */
    @Serializable
    data object Search : RouterConstants()

    /**
     * 音乐列表页面
     */
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

    /**
     * 关于页面
     */
    @Serializable
    data object About : RouterConstants()

    /**
     * 缓存大小设置页面
     */
    @Serializable
    data object CacheLimit : RouterConstants()

    /**
     * 选择媒体库页面
     */
    @Serializable
    data class SelectLibrary(
        val connectionId: Long,
        val libraryId: String?
    ) : RouterConstants()

    /**
     * 每日推荐页面
     */
    @Serializable
    data object DailyRecommend : RouterConstants()

}

