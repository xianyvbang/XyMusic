package cn.xybbz.router

data class PlatformNavigationConfig(
    val startRoute: RouterConstants,
    val topLevelRoutes: Set<RouterConstants>,
    val enableAnimations: Boolean = true
)

internal val mobilePlatformNavigationConfig = PlatformNavigationConfig(
    startRoute = Home,
    topLevelRoutes = setOf(Home, Album),
    enableAnimations = true
)

expect val platformNavigationConfig: PlatformNavigationConfig
