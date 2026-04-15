package cn.xybbz.router

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey

expect val platformEntryProvider: (NavKey) -> NavEntry<NavKey>
