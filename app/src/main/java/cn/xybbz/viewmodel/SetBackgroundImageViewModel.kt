package cn.xybbz.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.xybbz.config.BackgroundConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SetBackgroundImageViewModel @Inject constructor(
    val backgroundConfig: BackgroundConfig
): ViewModel() {

    fun updateBackgroundImageUri(backgroundImageUri: Uri?) {
        viewModelScope.launch {
            backgroundConfig.updateBackgroundImageUri(backgroundImageUri)
        }
    }
}