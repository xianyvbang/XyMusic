package cn.xybbz.viewmodel

import androidx.lifecycle.ViewModel
import cn.xybbz.config.lrc.LrcServer
import cn.xybbz.config.music.MusicCommonController
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class LrcViewModel (
    val musicController: MusicCommonController,
    val lrcServer: LrcServer
): ViewModel() {


    fun getProgressStateFlow():Flow<Long>{
        return musicController.progressStateFlow
    }
    fun seekTo(millSeconds: Long){
        musicController.seekTo(millSeconds)
    }
}