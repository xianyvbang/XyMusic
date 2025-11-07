package cn.xybbz.viewmodel

import androidx.lifecycle.ViewModel
import cn.xybbz.config.BackgroundConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AboutViewModel @Inject constructor(
    val backgroundConfig: BackgroundConfig
) : ViewModel() {

}