package cn.xybbz.router

import androidx.navigation3.runtime.NavKey

interface OnDestinationChangedListener {

    fun onDestinationChanged(
        navigator: Navigator,
        destination: NavKey
    )
}