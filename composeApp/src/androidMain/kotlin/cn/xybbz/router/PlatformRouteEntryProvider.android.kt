package cn.xybbz.router

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey

actual val platformEntryProvider: (NavKey) -> NavEntry<NavKey> = buildDefaultRouteEntryProvider()
