package cn.xybbz.config.network

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Network.nw_interface_type_wifi
import platform.Network.nw_interface_type_wired
import platform.Network.nw_path_get_status
import platform.Network.nw_path_is_expensive
import platform.Network.nw_path_monitor_cancel
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_monitor_t
import platform.Network.nw_path_status_satisfied
import platform.Network.nw_path_t
import platform.Network.nw_path_uses_interface_type
import platform.darwin.dispatch_queue_create
import platform.darwin.dispatch_queue_t

@OptIn(ExperimentalForeignApi::class)
class IosNetWorkMonitor : NetWorkMonitor() {

    private var monitor: nw_path_monitor_t? = null
    private var queue: dispatch_queue_t? = null
    private var hasDeliveredInitialState = false

    override fun onStart() {
        val pathMonitor = nw_path_monitor_create()
        val dispatchQueue = dispatch_queue_create("cn.xybbz.network.monitor", null)

        hasDeliveredInitialState = false
        nw_path_monitor_set_update_handler(pathMonitor) { path ->
            val forceDispatch = !hasDeliveredInitialState
            hasDeliveredInitialState = true
            publishNetworkState(resolveState(path), force = forceDispatch)
        }
        nw_path_monitor_set_queue(pathMonitor, dispatchQueue)
        nw_path_monitor_start(pathMonitor)

        monitor = pathMonitor
        queue = dispatchQueue
    }

    override fun onStop() {
        monitor?.let { nw_path_monitor_cancel(it) }
        monitor = null
        queue = null
        hasDeliveredInitialState = false
    }

    private fun resolveState(path: nw_path_t?): Boolean {
        if (path == null) {
            return false
        }

        val isSatisfied = nw_path_get_status(path) == nw_path_status_satisfied
        if (!isSatisfied || nw_path_is_expensive(path)) {
            return false
        }

        return nw_path_uses_interface_type(path, nw_interface_type_wifi) ||
            nw_path_uses_interface_type(path, nw_interface_type_wired)
    }
}
