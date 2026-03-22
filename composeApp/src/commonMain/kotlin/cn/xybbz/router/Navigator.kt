/*
 *   XyMusic
 *   Copyright (C) 2023 xianyvbang
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package cn.xybbz.router

import androidx.navigation3.runtime.NavKey

/**
 * Handles navigation events (forward and back) by updating the navigation state.
 */
class Navigator(
    val state: NavigationState,
) {

    val onDestinationChangedListeners = mutableListOf<OnDestinationChangedListener>()
    fun navigate(route: RouterConstants) {
        if (route in state.backStacks.keys) {
            // This is a top level route, just switch to it.
            state.topLevelRoute = route
        } else {
            state.backStacks[state.topLevelRoute]?.add(route)
        }
        for (listener in onDestinationChangedListeners.toList()) {
            listener.onDestinationChanged(
                this,
                route
            )
        }
    }

    fun goBack() {
        val currentStack = state.backStacks[state.topLevelRoute]
            ?: error("Stack for ${state.topLevelRoute} not found")
        val currentRoute = currentStack.last()
        var route: NavKey?
        // If we're at the base of the current route, go back to the start route stack.
        if (currentRoute == state.topLevelRoute) {
            val startRoute = state.startRoute
            state.topLevelRoute = startRoute
            route = startRoute
        } else {
            currentStack.removeLastOrNull()
            route = currentStack.last()
        }

        route.let {
            for (listener in onDestinationChangedListeners.toList()) {
                listener.onDestinationChanged(
                    this,
                    route
                )
            }
        }
    }


    fun addOnDestinationChangedListener(listener: OnDestinationChangedListener) {
        onDestinationChangedListeners.add(listener)
    }

    fun removeOnDestinationChangedListener(listener: OnDestinationChangedListener) {
        onDestinationChangedListeners.remove(listener)
    }
}