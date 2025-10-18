package cn.xybbz.viewmodel

import androidx.lifecycle.ViewModel
import cn.xybbz.common.music.MusicController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class LrcViewModel @Inject constructor(
    private val _musicController: MusicController
): ViewModel() {

    val musicFinalNewController = _musicController

    fun getProgressStateFlow():Flow<Long>{
        return _musicController.progressStateFlow
    }
    fun seekTo(millSeconds: Long){
        _musicController.seekTo(millSeconds)
    }
}