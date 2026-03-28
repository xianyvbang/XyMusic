package cn.xybbz.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class SetBackgroundImageViewModel (
    val backgroundConfig: BackgroundConfig,
): ViewModel() {

    fun updateBackgroundImageUri(backgroundImageUri: Uri?) {
        viewModelScope.launch {
            backgroundConfig.updateBackgroundImageUri(backgroundImageUri)
        }
    }
}
