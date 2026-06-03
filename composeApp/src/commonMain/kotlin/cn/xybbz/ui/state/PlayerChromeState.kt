package cn.xybbz.ui.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * 播放器外壳状态。
 *
 * 只保存播放器页显隐、标题跑马灯次数这类页面表现状态，避免把纯 UI 状态放进 MainViewModel。
 */
class PlayerChromeState {
    /**
     * 迷你播放条标题的跑马灯滚动次数。
     */
    var marqueeIterations by mutableIntStateOf(1)
        private set

    /**
     * 完整播放器页面是否显示。
     */
    var isPlayerSheetVisible by mutableStateOf(false)
        private set

    /**
     * 更新迷你播放条标题的跑马灯滚动次数。
     */
    fun putMarqueeIterations(iterations: Int) {
        marqueeIterations = iterations
    }

    /**
     * 显示完整播放器页面。
     */
    fun showPlayerSheet() {
        isPlayerSheetVisible = true
    }

    /**
     * 隐藏完整播放器页面。
     */
    fun hidePlayerSheet() {
        isPlayerSheetVisible = false
    }
}

/**
 * 创建并记住播放器外壳状态。
 */
@Composable
fun rememberPlayerChromeState(): PlayerChromeState =
    remember { PlayerChromeState() }
