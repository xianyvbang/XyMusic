package cn.xybbz.common.utils

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * 获得CoroutineScope
 */
object CoroutineScopeUtils {

    /**
     * 获得Main携程
     */
    fun getMain(name: String = "xy"): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Main + CoroutineName(name))
    }

    /**
     * 获得IO携程
     */
    fun getIo(name: String = "xy"): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.IO + CoroutineName(name))
    }
}