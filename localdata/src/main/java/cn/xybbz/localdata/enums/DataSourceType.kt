package cn.xybbz.localdata.enums

/**
 * 数据加载类型
 * 数据源类型枚举
 * @author 刘梦龙
 * @date 2024/11/04
 * @constructor 创建[DataSourceType]
 * @param [describe] 描述
 * @param [title] 标题
 * @param [img] 图片网址
 * @param [port] 端口号
 * @param [ifScan] 是否需要扫描
 * @param [ifUpdatePassword] 是否可以更改密码
 * @param [ifSelectProtocolType] 是否可以选择协议类型
 * @param [options] 协议列表
 */
//todo 将配置抽取成对象,变成一个模板对象,然后可以写入链接配置中
enum class DataSourceType(
    val describe: String,
    val title: String,
    val port: Int? = null,
    val ifSelectProtocolType: Boolean = false,
    val ifShow: Boolean = true,
    val options: List<String> = emptyList(),
    val code: String,
    val version: String,
    //是否只能选单年
    val ifSelectOneYear: Boolean,
    //是否显示筛选和排序功能 todo 需要增肌更详细的设置
    val ifShowSelect: Boolean,
    //是否显示数量
    val ifShowCount: Boolean,
    //是否支持删除功能
    val ifDelete: Boolean,
    //是否艺术家可以进行收藏筛选
    val ifArtistFavorite: Boolean
) {

    JELLYFIN(
        describe = "jellyfin媒体服务器",
        title = "Jellyfin",
        port = 8096,
        ifSelectProtocolType = true,
        options = listOf("http://", "https://"),
        code = "1",
        version = "10.10.7",
        ifShowSelect = true,
        ifShowCount = true,
        ifDelete = true,
        ifArtistFavorite = true,
        ifSelectOneYear = false
    ),

    SUBSONIC(
        describe = "Subsonic媒体服务器",
        title = "Subsonic",
        port = 4040,
        ifSelectProtocolType = true,
        options = listOf("http://", "https://"),
        code = "2",
        version = "1.16.0",
        ifShowSelect = false,
        ifShowCount = false,
        ifDelete = false,
        ifArtistFavorite = false,
        ifSelectOneYear = false
    ),

    NAVIDROME(
        describe = "navidrome媒体服务器",
        title = "Navidrome",
        port = 4533,
        ifSelectProtocolType = true,
        options = listOf("http://", "https://"),
        code = "3",
        version = "0.56.1",
        ifShowSelect = true,
        ifShowCount = true,
        ifDelete = true,
        ifArtistFavorite = true,
        ifSelectOneYear = true
    ),
    EMBY(
        describe = "emby媒体服务器",
        title = "Emby",
        port = 8096,
        ifSelectProtocolType = true,
        options = listOf("http://", "https://"),
        code = "4",
        version = "4.1.1.0",
        ifShowSelect = true,
        ifShowCount = true,
        ifDelete = true,
        ifArtistFavorite = true,
        ifSelectOneYear = true
    ),


    PLEX(
        describe = "PLEX媒体服务器",
        title = "Plex",
        port = 32400,
        ifSelectProtocolType = true,
        options = listOf("http://", "https://"),
        code = "4",
        version = "4.1.1.0",
        ifShowSelect = true,
        ifShowCount = true,
        ifDelete = true,
        ifArtistFavorite = false,
        ifSelectOneYear = false
    )
}