package cn.xybbz.config.update

import android.os.Build
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cn.xybbz.R
import cn.xybbz.api.client.version.VersionApiClient
import cn.xybbz.api.client.version.data.ReleasesData
import cn.xybbz.common.constants.Constants
import cn.xybbz.common.utils.GitHubVersionVersionUtils
import cn.xybbz.common.utils.MessageUtils
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.enums.DownloadStatus
import cn.xybbz.localdata.enums.DownloadTypes

class ApkUpdateManager(
    private val db: DatabaseClient,
    private val settingsManager: SettingsManager,
    private val versionApiClient: VersionApiClient,
) {

    //最新版本的版本号
    var latestVersion by mutableStateOf("")
        private set

    //当前版本
    var currentVersion by mutableStateOf("")
        private set

    //是否最新版本
    var ifMaxVersion by mutableStateOf(true)
        private set

    //最新版本信息
    var releasesInfo by mutableStateOf<ReleasesData?>(null)
        private set


    init {
        currentVersion = settingsManager.get().latestVersion
    }

    /**
     * 获得应用的最新版本号
     * @return true 获取最新版本号成功,false 获取最新版本号失败
     */
    suspend fun initLatestVersion(ifCheck: Boolean = false): Boolean {

        if (ifCheck)
            MessageUtils.sendPopTipSuccess(R.string.get_latest_version)

        val abis = Build.SUPPORTED_ABIS
        Log.d("ABI", abis.joinToString())

        val apkDownload = db.downloadDao.getOne(DownloadTypes.APK)
        if (apkDownload?.status == DownloadStatus.DOWNLOADING) return true
        val versionName = settingsManager.packageInfo.versionName
        var ifGetVersionSuccess = true
        val currentTimeMillis = System.currentTimeMillis()
        currentVersion = versionName ?: ""
        latestVersion = settingsManager.get().latestVersion
        try {
            val releasesInfo = versionApiClient.downloadApi().getLatestReleasesInfo()
            Log.i("======", "返回github信息: ${releasesInfo}")
            this.releasesInfo = releasesInfo
            if (releasesInfo != null) {
                latestVersion = releasesInfo.tagName
                settingsManager.setLatestVersion(releasesInfo.tagName)

                val assetItem = releasesInfo.assets.findLast {
                    it.name.contains("apk") && abis.any { abi ->
                        it.name.contains(abi)
                    }
                }
                if (assetItem != null) {
                    Log.i("======", "读取的APK下载信息: $assetItem")
                    settingsManager.setLastApkUrl(assetItem.browserDownloadUrl)
                }
                settingsManager.setLatestVersionTime(currentTimeMillis)
            } else {
                ifGetVersionSuccess = false
            }
        } catch (e: Exception) {
            Log.e(Constants.LOG_ERROR_PREFIX, "获取github版本号失败", e)
            ifGetVersionSuccess = false
        }
        if (!versionName.isNullOrBlank()) {
            ifMaxVersion =
                GitHubVersionVersionUtils.isLatestVersion(versionName, latestVersion)
            if (ifCheck)
                if (!ifMaxVersion && ifGetVersionSuccess) {
                    MessageUtils.sendPopTipSuccess(R.string.get_latest_version_success)
                } else if (!ifMaxVersion && !ifGetVersionSuccess) {
                    MessageUtils.sendPopTipError(R.string.get_latest_version_fail)
                } else {
                    MessageUtils.sendPopTipSuccess(R.string.now_new_version)
                }
        }
        return ifGetVersionSuccess
    }

    /**
     * 是否已经完成下载,或者下载时间不超过1小时
     */
    fun ifDownloadApk(ifCheck: Boolean): Boolean {
        val versionName = settingsManager.packageInfo.versionName
        currentVersion = if (versionName.isNullOrBlank()) "" else versionName
        val latestVersionTime = settingsManager.get().latestVersionTime
        val currentTimeMillis = System.currentTimeMillis()
        val ifGetVersion = (currentTimeMillis - latestVersionTime) >= (1 * 60 * 60 * 1000)
        return ifCheck && !ifGetVersion
    }

}