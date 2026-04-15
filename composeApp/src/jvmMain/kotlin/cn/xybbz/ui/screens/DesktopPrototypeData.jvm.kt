package cn.xybbz.ui.screens

import androidx.compose.ui.graphics.Color
import xymusic_kmp.composeapp.generated.resources.*

/**
 * 左侧导航菜单样例数据。
 */
internal val sidebarItems = listOf(
    MenuItem("主页", SidebarDestination.Home, Res.drawable.album_24px),
    MenuItem("搜索", SidebarDestination.Search, Res.drawable.search_24px),
    MenuItem("音乐库", SidebarDestination.Library, Res.drawable.menu_open_24px),
    MenuItem("专辑", SidebarDestination.Albums, Res.drawable.album_24px),
    MenuItem("艺术家", SidebarDestination.Artists, Res.drawable.person_24px),
)

/**
 * 左侧“我的歌单”样例数据。
 */
internal val sidebarPlaylists = listOf(
    PlaylistCard("我喜欢的音乐", "歌单 • 128首", Color(0xFF3E5AE8)),
    PlaylistCard("工作轻音乐", "歌单 • Xianyvbang", Color(0xFFB25524)),
    PlaylistCard("2023 年度总结", "歌单 • 官方", Color(0xFF257A74)),
)

/**
 * 首页推荐歌曲样例数据。
 */
internal val recommendedSongs = listOf(
    SongRowData(1, "Neon Lights", "The Synth Band", "Midnight Sounds", "2天前", "3:42", Color(0xFF3C4CE0)),
    SongRowData(2, "Bass Drop", "DJ Alex", "Electronic Vibes", "4天前", "4:15", Color(0xFFE14C40)),
    SongRowData(3, "Sunrise Stroll", "Sarah Guitars", "Acoustic Morning", "1周前", "3:18", Color(0xFFB98B29)),
)

/**
 * 首页最新专辑样例数据。
 */
internal val latestAlbums = listOf(
    AlbumCardData("Midnight Sounds", "The Synth Band", Color(0xFF3C4CE0)),
    AlbumCardData("Electronic Vibes", "DJ Alex", Color(0xFFE14C40)),
    AlbumCardData("Acoustic Morning", "Sarah Guitars", Color(0xFFB98B29)),
    AlbumCardData("Jazz Sessions", "The Trio", Color(0xFF267A6A)),
)

/**
 * 首页最近播放歌曲样例数据。
 */
internal val recentSongs = listOf(
    SongRowData(1, "Shape of My Heart", "Sting", "Ten Summoner's Tales", "昨天", "4:38", Color(0xFF467B52)),
    SongRowData(2, "Lose Yourself", "Eminem", "8 Mile", "昨天", "5:20", Color(0xFF7E3C3C)),
)

/**
 * 首页最近播放专辑样例数据。
 */
internal val recentAlbums = listOf(
    AlbumCardData("Folklore Tales", "Sarah Guitars", Color(0xFF4F7A46)),
    AlbumCardData("Night Drive", "The Synth Band", Color(0xFF293A7A)),
)

/**
 * 首页热门专辑样例数据。
 */
internal val hotAlbums = listOf(
    AlbumCardData("Vinyl Dreams", "The Trio", Color(0xFF945E1B)),
    AlbumCardData("Club Echo", "DJ Alex", Color(0xFF99415D)),
    AlbumCardData("Blue Hour", "North Harbor", Color(0xFF345B90)),
)

/**
 * 搜索页热门关键词卡片样例数据。
 */
internal val trendingCards = listOf(
    AlbumCardData("Synth Wave", "热门艺人", Color(0xFF27856A), highlight = Color(0xFF27856A)),
    AlbumCardData("Lo-Fi Beats", "舒缓精选", Color(0xFF1E3264), highlight = Color(0xFF1E3264)),
    AlbumCardData("City Pop", "夜间收藏", Color(0xFF8D67AB), highlight = Color(0xFF8D67AB)),
    AlbumCardData("Daily Mix", "随机播放", Color(0xFFE8115B), highlight = Color(0xFFE8115B)),
)

/**
 * 音乐库页歌单样例数据。
 */
internal val libraryCards = listOf(
    AlbumCardData("我喜欢的音乐", "歌单 • 128首", Color(0xFF3E5AE8)),
    AlbumCardData("工作轻音乐", "歌单 • Xianyvbang", Color(0xFFB25524)),
)

/**
 * 专辑列表页样例数据。
 */
internal val allAlbums = listOf(
    AlbumCardData("Midnight Sounds", "The Synth Band", Color(0xFF3C4CE0)),
    AlbumCardData("Electronic Vibes", "DJ Alex", Color(0xFFE14C40)),
    AlbumCardData("Acoustic Morning", "Sarah Guitars", Color(0xFFB98B29)),
)

/**
 * 艺术家列表页样例数据。
 */
internal val allArtists = listOf(
    AlbumCardData("The Synth Band", "关注者: 1.4M", Color(0xFF293A7A), circular = true),
    AlbumCardData("DJ Alex", "关注者: 928K", Color(0xFF7B2D57), circular = true),
    AlbumCardData("Sarah Guitars", "关注者: 2.1M", Color(0xFF926B1A), circular = true),
)

/**
 * 歌单详情头部样例数据。
 */
internal val playlistHeader = DetailHeaderData(
    "公开歌单",
    "我喜欢的音乐",
    "XyMusic 官方 • 128 首歌曲, 约 8 小时",
    Color(0xFF3E5AE8),
)

/**
 * 专辑详情头部样例数据。
 */
internal val sampleAlbumDetail = DetailHeaderData(
    "专辑",
    "Midnight Sounds",
    "The Synth Band • 2024 • 12 首歌曲, 42 分钟",
    Color(0xFF3C4CE0),
)

/**
 * 艺术家详情头部样例数据。
 */
internal val sampleArtistDetail = ArtistDetailData(
    "The Synth Band",
    "1,245,678 每月听众",
    Color(0xFF263A7B),
)

/**
 * 艺术家热门歌曲样例数据。
 */
internal val artistHotSongs = listOf(
    SongRowData(1, "Neon Lights", "The Synth Band", "1,000,432 次播放", "", "3:42", Color(0xFF3C4CE0)),
    SongRowData(2, "City Cruising", "The Synth Band", "845,123 次播放", "", "4:10", Color(0xFF6B419B)),
)

/**
 * 播放队列样例数据。
 */
internal val queueSongs = listOf(
    SongRowData(1, "Bass Drop", "DJ Alex", "", "", "4:15", Color(0xFFE14C40)),
    SongRowData(2, "Sunrise Stroll", "Sarah Guitars", "", "", "3:18", Color(0xFFB98B29)),
)
