package cn.xybbz.ui.windows

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import cn.xybbz.ui.theme.XyTheme

@Composable
fun DesktopWindowTitleBar(
    modifier: Modifier = Modifier,
    frameState: DesktopWindowFrameState = LocalDesktopWindowFrameState.current,
    chromeController: DesktopWindowChromeController = LocalDesktopWindowChromeController.current,
    hitTestOwner: DesktopInteractiveHitTestOwner? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    isMaximized: Boolean = frameState.isMaximized,
    onMinimize: () -> Unit = frameState.onMinimize,
    onToggleMaximize: () -> Unit = frameState.onToggleMaximize,
    onClose: () -> Unit = frameState.onClose,
    showMinimizeButton: Boolean = true,
    showMaximizeButton: Boolean = true,
    showCloseButton: Boolean = true,
    minimizeIcon: WindowControlIcon = { tint, iconModifier ->
        WindowControlButtonIcons.Minimize(tint, iconModifier)
    },
    maximizeIcon: WindowControlIcon = { tint, iconModifier ->
        WindowControlButtonIcons.Maximize(tint, iconModifier)
    },
    restoreIcon: WindowControlIcon = { tint, iconModifier ->
        WindowControlButtonIcons.Restore(tint, iconModifier)
    },
    closeIcon: WindowControlIcon = { tint, iconModifier ->
        WindowControlButtonIcons.Close(tint, iconModifier)
    },
    beforeWindowControls: (@Composable RowScope.(DesktopInteractiveHitTestOwner) -> Unit)? = null,
    afterWindowControls: (@Composable RowScope.(DesktopInteractiveHitTestOwner) -> Unit)? = null,
    onControlButtonBoundsChanged: (WindowControlType, Rect) -> Unit = { _, _ -> },
    front: (@Composable RowScope.(DesktopInteractiveHitTestOwner) -> Unit)? = null,
    middle: (@Composable BoxScope.(DesktopInteractiveHitTestOwner) -> Unit)? = null,
    back: (@Composable RowScope.(DesktopInteractiveHitTestOwner) -> Unit)? = null,
) {
    val activeHitTestOwner = hitTestOwner ?: remember { DesktopInteractiveHitTestOwner() }

    DisposableEffect(chromeController, activeHitTestOwner) {
        val registration = DesktopTitleBarHitTestRegistry.register(
            chromeController = chromeController,
            owner = activeHitTestOwner
        )
        onDispose {
            activeHitTestOwner.clear()
            DesktopTitleBarHitTestRegistry.unregister(chromeController, registration)
        }
    }

    val controlButtonBoundsChanged: (WindowControlType, Rect) -> Unit = { type, bounds ->
        activeHitTestOwner.updateBounds("WindowControl$type", bounds)
        when (type) {
            WindowControlType.Minimize -> chromeController.updateMinimizeButtonBounds(bounds)
            WindowControlType.Maximize,
            WindowControlType.Restore -> chromeController.updateMaximizeButtonBounds(bounds)

            WindowControlType.Close -> chromeController.updateCloseButtonBounds(bounds)
        }
        onControlButtonBoundsChanged(type, bounds)
    }

    CompositionLocalProvider(LocalDesktopTitleBarHitTestOwner provides activeHitTestOwner) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(DesktopWindowTitleBarDefaults.Height)
                .onGloballyPositioned { coordinates ->
                    chromeController.updateTitleBarBounds(coordinates.boundsInWindow())
                }
                .background(backgroundColor),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            front?.invoke(this, activeHitTestOwner)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.CenterStart
            ) {
                middle?.invoke(this, activeHitTestOwner)
            }

            Row(
                modifier = Modifier.padding(end = XyTheme.dimens.outerHorizontalPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (back != null) {
                    back.invoke(this, activeHitTestOwner)
                } else {
                    DefaultDesktopWindowTitleBarBack(
                        isMaximized = isMaximized,
                        onMinimize = onMinimize,
                        onToggleMaximize = onToggleMaximize,
                        onClose = onClose,
                        showMinimizeButton = showMinimizeButton,
                        showMaximizeButton = showMaximizeButton,
                        showCloseButton = showCloseButton,
                        minimizeIcon = minimizeIcon,
                        maximizeIcon = maximizeIcon,
                        restoreIcon = restoreIcon,
                        closeIcon = closeIcon,
                        beforeWindowControls = beforeWindowControls?.let { content ->
                            { content(activeHitTestOwner) }
                        },
                        afterWindowControls = afterWindowControls?.let { content ->
                            { content(activeHitTestOwner) }
                        },
                        onControlButtonBoundsChanged = controlButtonBoundsChanged
                    )
                }
            }
        }
    }
}



@Composable
fun DesktopWindowTitleCenterBar(
    modifier: Modifier = Modifier,
    frameState: DesktopWindowFrameState = LocalDesktopWindowFrameState.current,
    chromeController: DesktopWindowChromeController = LocalDesktopWindowChromeController.current,
    hitTestOwner: DesktopInteractiveHitTestOwner? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    isMaximized: Boolean = frameState.isMaximized,
    onMinimize: () -> Unit = frameState.onMinimize,
    onToggleMaximize: () -> Unit = frameState.onToggleMaximize,
    onClose: () -> Unit = frameState.onClose,
    showMinimizeButton: Boolean = true,
    showMaximizeButton: Boolean = true,
    showCloseButton: Boolean = true,
    minimizeIcon: WindowControlIcon = { tint, iconModifier ->
        WindowControlButtonIcons.Minimize(tint, iconModifier)
    },
    maximizeIcon: WindowControlIcon = { tint, iconModifier ->
        WindowControlButtonIcons.Maximize(tint, iconModifier)
    },
    restoreIcon: WindowControlIcon = { tint, iconModifier ->
        WindowControlButtonIcons.Restore(tint, iconModifier)
    },
    closeIcon: WindowControlIcon = { tint, iconModifier ->
        WindowControlButtonIcons.Close(tint, iconModifier)
    },
    beforeWindowControls: (@Composable RowScope.(DesktopInteractiveHitTestOwner) -> Unit)? = null,
    afterWindowControls: (@Composable RowScope.(DesktopInteractiveHitTestOwner) -> Unit)? = null,
    onControlButtonBoundsChanged: (WindowControlType, Rect) -> Unit = { _, _ -> },
    front: (@Composable BoxScope.(DesktopInteractiveHitTestOwner) -> Unit)? = null,
    middle: (@Composable BoxScope.(DesktopInteractiveHitTestOwner) -> Unit)? = null,
    back: (@Composable RowScope.(DesktopInteractiveHitTestOwner) -> Unit)? = null,
) {
    val activeHitTestOwner = hitTestOwner ?: remember { DesktopInteractiveHitTestOwner() }

    DisposableEffect(chromeController, activeHitTestOwner) {
        val registration = DesktopTitleBarHitTestRegistry.register(
            chromeController = chromeController,
            owner = activeHitTestOwner
        )
        onDispose {
            activeHitTestOwner.clear()
            DesktopTitleBarHitTestRegistry.unregister(chromeController, registration)
        }
    }

    val controlButtonBoundsChanged: (WindowControlType, Rect) -> Unit = { type, bounds ->
        activeHitTestOwner.updateBounds("WindowControl$type", bounds)
        when (type) {
            WindowControlType.Minimize -> chromeController.updateMinimizeButtonBounds(bounds)
            WindowControlType.Maximize,
            WindowControlType.Restore -> chromeController.updateMaximizeButtonBounds(bounds)

            WindowControlType.Close -> chromeController.updateCloseButtonBounds(bounds)
        }
        onControlButtonBoundsChanged(type, bounds)
    }

    CompositionLocalProvider(LocalDesktopTitleBarHitTestOwner provides activeHitTestOwner) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(start = XyTheme.dimens.outerHorizontalPadding)
                .height(DesktopWindowTitleBarDefaults.Height)
                .onGloballyPositioned { coordinates ->
                    chromeController.updateTitleBarBounds(coordinates.boundsInWindow())
                }
                .background(backgroundColor)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxHeight(),
                contentAlignment = Alignment.CenterStart
            ) {
                front?.invoke(this, activeHitTestOwner)
            }

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                middle?.invoke(this, activeHitTestOwner)
            }

            Row(
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = XyTheme.dimens.outerHorizontalPadding),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (back != null) {
                    back.invoke(this, activeHitTestOwner)
                } else {
                    DefaultDesktopWindowTitleBarBack(
                        isMaximized = isMaximized,
                        onMinimize = onMinimize,
                        onToggleMaximize = onToggleMaximize,
                        onClose = onClose,
                        showMinimizeButton = showMinimizeButton,
                        showMaximizeButton = showMaximizeButton,
                        showCloseButton = showCloseButton,
                        minimizeIcon = minimizeIcon,
                        maximizeIcon = maximizeIcon,
                        restoreIcon = restoreIcon,
                        closeIcon = closeIcon,
                        beforeWindowControls = beforeWindowControls?.let { content ->
                            { content(activeHitTestOwner) }
                        },
                        afterWindowControls = afterWindowControls?.let { content ->
                            { content(activeHitTestOwner) }
                        },
                        onControlButtonBoundsChanged = controlButtonBoundsChanged
                    )
                }
            }
        }
    }
}

private object DesktopTitleBarHitTestRegistry {
    data class Registration(
        val owner: DesktopInteractiveHitTestOwner,
    )

    private val registrationsByController =
        mutableMapOf<DesktopWindowChromeController, MutableList<Registration>>()

    fun register(
        chromeController: DesktopWindowChromeController,
        owner: DesktopInteractiveHitTestOwner,
    ): Registration {
        val registration = Registration(owner)
        registrationsByController
            .getOrPut(chromeController) { mutableListOf() }
            .add(registration)
        applyTop(chromeController)
        return registration
    }

    fun unregister(
        chromeController: DesktopWindowChromeController,
        registration: Registration,
    ) {
        val registrations = registrationsByController[chromeController] ?: return
        registrations.remove(registration)
        if (registrations.isEmpty()) {
            registrationsByController.remove(chromeController)
        }
        applyTop(chromeController)
    }

    private fun applyTop(chromeController: DesktopWindowChromeController) {
        // AnimatedContent 切换时标题栏会短暂并存，用栈顶 owner 避免退出页覆盖新页面热区。
        val owner = registrationsByController[chromeController]?.lastOrNull()?.owner
        chromeController.setTitleBarHitTestOwner(owner)
        chromeController.setTitleBarHitTestEnabled(owner != null)
    }
}
