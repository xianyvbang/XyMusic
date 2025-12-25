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
            val key = currentStack.removeLastOrNull()
            route = key
        }

        route?.let {
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