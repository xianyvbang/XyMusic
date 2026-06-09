package cn.xybbz.config.volume

class VolumeIosServer : VolumeServer {

    override fun createVolumeManager() {
    }

    override suspend fun updateVolume(volume: Int) {
    }

    override fun getMaxVolume(): Int {
        return 100
    }

    override fun getStreamVolume(): Int {
        return 100
    }
}
