package cn.xybbz.router

data class PlatformNavigationConfig(
    val startRoute: RouterConstants,
    val topLevelRoutes: Set<RouterConstants>,
    val enableAnimations: Boolean = true,
    val enableTopLevelRoutes: Boolean = true
)

internal val mobilePlatformNavigationConfig = PlatformNavigationConfig(
    startRoute = Home,
    topLevelRoutes = setOf(Home),
    enableAnimations = true
)

expect val platformNavigationConfig: PlatformNavigationConfig
