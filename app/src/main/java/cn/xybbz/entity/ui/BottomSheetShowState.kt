package cn.xybbz.entity.ui

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@Stable
 class BottomSheetShowState{
    var state by mutableStateOf(false)
        private set

    /**
     * 显示
     */
     fun show(){
         state = true
     }

    /**
     * 隐藏
     */
    fun hide(){
        state = false
    }
 }
