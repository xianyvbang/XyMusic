package cn.xybbz.router

actual val platformNavigationConfig: PlatformNavigationConfig = PlatformNavigationConfig(
    startRoute = Home,
    topLevelRoutes = setOf(Home, Search, Music, Album, Artist, FavoriteList)
)
