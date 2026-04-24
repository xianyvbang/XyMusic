package cn.xybbz.config.volume

import cn.xybbz.config.music.MusicCommonController
import cn.xybbz.config.setting.SettingsManager
import cn.xybbz.localdata.config.LocalDatabaseClient
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject

class VolumeJvmServer : VolumeServer, KoinComponent {

    private val musicController: MusicCommonController by inject()
    private val settingsManager: SettingsManager by inject()

    private var volume = 0

    override fun createVolumeManager() {
    }

    override suspend fun updateVolume(volume: Int) {
        this.volume = volume.coerceIn(0, getMaxVolume())
        musicController.setVolume(this.volume)
        settingsManager.setJvmVolume(volume)
    }

    override fun getMaxVolume(): Int {
        return 100
    }

    override fun getStreamVolume(): Int {
        return volume
    }
}
