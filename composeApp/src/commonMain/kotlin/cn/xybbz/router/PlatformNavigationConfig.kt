package cn.xybbz.router

data class PlatformNavigationConfig(
    val startRoute: RouterConstants,
    val topLevelRoutes: Set<RouterConstants>
)

internal val mobilePlatformNavigationConfig = PlatformNavigationConfig(
    startRoute = Home,
    topLevelRoutes = setOf(Home, Album)
)

expect val platformNavigationConfig: PlatformNavigationConfig
