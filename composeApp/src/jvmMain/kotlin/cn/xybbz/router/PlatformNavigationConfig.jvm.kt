package cn.xybbz.router

actual val platformNavigationConfig: PlatformNavigationConfig = PlatformNavigationConfig(
    startRoute = Home,
    topLevelRoutes = LinkedHashSet(jvmTopRouterDataList.map { it.route }),
    enableAnimations = false
)
