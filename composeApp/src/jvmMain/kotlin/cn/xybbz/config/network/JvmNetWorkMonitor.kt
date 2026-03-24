package cn.xybbz.config.network

import java.net.NetworkInterface
import java.util.Collections
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class JvmNetWorkMonitor : NetWorkMonitor() {

    private var executor: ScheduledExecutorService? = null

    override fun onStart() {
        publishNetworkState(detectNetworkState(), force = true)
        executor = Executors.newSingleThreadScheduledExecutor { runnable ->
            Thread(runnable, "xy-network-monitor").apply {
                isDaemon = true
            }
        }.also { scheduler ->
            scheduler.scheduleWithFixedDelay(
                {
                    runCatching {
                        publishNetworkState(detectNetworkState())
                    }
                },
                POLL_INTERVAL_SECONDS,
                POLL_INTERVAL_SECONDS,
                TimeUnit.SECONDS
            )
        }
    }

    override fun onStop() {
        executor?.shutdownNow()
        executor = null
    }

    private fun detectNetworkState(): Boolean {
        val interfaces = NetworkInterface.getNetworkInterfaces() ?: return false
        return Collections.list(interfaces).any { networkInterface ->
            isUsableUnmeteredInterface(networkInterface)
        }
    }

    private fun isUsableUnmeteredInterface(networkInterface: NetworkInterface): Boolean {
        return runCatching {
            val interfaceNames =
                listOfNotNull(networkInterface.name, networkInterface.displayName)
                    .joinToString(" ")
                    .lowercase()

            if (!networkInterface.isUp || networkInterface.isLoopback || networkInterface.isVirtual) {
                false
            } else if (VIRTUAL_INTERFACE_MARKERS.any(interfaceNames::contains)) {
                false
            } else {
                val hasUsableAddress =
                    Collections.list(networkInterface.inetAddresses).any { address ->
                        !address.isLoopbackAddress &&
                            !address.isLinkLocalAddress &&
                            address.hostAddress?.isNotBlank() == true
                    }
                hasUsableAddress && EXPENSIVE_INTERFACE_MARKERS.none(interfaceNames::contains)
            }
        }.getOrDefault(false)
    }

    private companion object {
        private const val POLL_INTERVAL_SECONDS = 3L

        private val VIRTUAL_INTERFACE_MARKERS = listOf(
            "virtual",
            "vmware",
            "vbox",
            "hyper-v",
            "docker",
            "bridge",
            "loopback",
            "awdl",
            "tap",
            "tun"
        )

        private val EXPENSIVE_INTERFACE_MARKERS = listOf(
            "cell",
            "mobile",
            "wwan"
        )
    }
}
