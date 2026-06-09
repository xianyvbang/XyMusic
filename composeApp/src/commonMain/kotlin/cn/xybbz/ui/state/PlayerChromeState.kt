package cn.xybbz.ui.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.annotation.Single

/**
 * 播放器外壳状态。
 *
 * 通过 Koin 单例保存播放器页显隐、标题跑马灯次数这类页面表现状态，避免把纯 UI 状态放进 MainViewModel。
 */
@Single
class PlayerChromeState {
    /**
     * 迷你播放条标题跑马灯滚动次数的内部可变数据源。
     */
    private val _marqueeIterationsFlow = MutableStateFlow(1)

    /**
     * 迷你播放条标题的跑马灯滚动次数。
     */
    val marqueeIterationsFlow = _marqueeIterationsFlow.asStateFlow()

    /**
     * 完整播放器页面显隐状态的内部可变数据源。
     */
    private val _isPlayerSheetVisibleFlow = MutableStateFlow(false)

    /**
     * 完整播放器页面是否显示。
     */
    val isPlayerSheetVisibleFlow = _isPlayerSheetVisibleFlow.asStateFlow()

    /**
     * 更新迷你播放条标题的跑马灯滚动次数。
     */
    fun putMarqueeIterations(iterations: Int) {
        _marqueeIterationsFlow.value = iterations
    }

    /**
     * 显示完整播放器页面。
     */
    fun showPlayerSheet() {
        _isPlayerSheetVisibleFlow.value = true
    }

    /**
     * 隐藏完整播放器页面。
     */
    fun hidePlayerSheet() {
        _isPlayerSheetVisibleFlow.value = false
    }
}
