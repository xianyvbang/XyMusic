package cn.xybbz.common.utils


object GitHubVersionVersionUtils {

    /**
     * 比较当前版本和最新版本
     */
    fun isLatestVersion(currentVersion: String, latestVersion: String): Boolean {
        val curParts = currentVersion.removePrefix("v").split(".").map { it.toIntOrNull() ?: 0 }
        val latParts = latestVersion.removePrefix("v").split(".").map { it.toIntOrNull() ?: 0 }

        val maxLength = maxOf(curParts.size, latParts.size)
        for (i in 0 until maxLength) {
            val c = curParts.getOrElse(i) { 0 }
            val l = latParts.getOrElse(i) { 0 }
            if (c > l) return true
            if (c < l) return false
        }
        return true
    }
}
