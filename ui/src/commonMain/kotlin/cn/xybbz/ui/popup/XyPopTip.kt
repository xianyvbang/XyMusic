package cn.xybbz.ui.popup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyTextSub
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import xymusic_kmp.ui.generated.resources.Res
import xymusic_kmp.ui.generated.resources.check_24px
import xymusic_kmp.ui.generated.resources.info_24px
import xymusic_kmp.ui.generated.resources.warning_24px

enum class XyPopTipStyle {
    Default,
    Success,
    Error,
    Hint
}

data class XyPopTipData(
    val id: Long,
    val text: String? = null,
    val textRes: StringResource? = null,
    val style: XyPopTipStyle = XyPopTipStyle.Default,
    val durationMillis: Long = 1_500L
)

private const val NEVER_DISMISS_DURATION = -1L

class XyPopTipHandle internal constructor(
    private val id: Long,
    private val onDismiss: (Long) -> Unit
) {
    fun dismiss() {
        onDismiss(id)
    }
}

object XyPopTipManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val currentTipFlow = MutableStateFlow<XyPopTipData?>(null)
    private var dismissJob: Job? = null
    private var nextId = 0L

    val currentTip: StateFlow<XyPopTipData?> = currentTipFlow.asStateFlow()

    fun show(
        text: String,
        style: XyPopTipStyle = XyPopTipStyle.Default,
        durationMillis: Long = 1_500L
    ): XyPopTipHandle {
        return showInternal(
            XyPopTipData(
                id = newId(),
                text = text,
                style = style,
                durationMillis = durationMillis
            )
        )
    }

    fun show(
        textRes: StringResource,
        style: XyPopTipStyle = XyPopTipStyle.Default,
        durationMillis: Long = 1_500L
    ): XyPopTipHandle {
        return showInternal(
            XyPopTipData(
                id = newId(),
                textRes = textRes,
                style = style,
                durationMillis = durationMillis
            )
        )
    }

    fun dismissCurrent() {
        dismiss()
    }

    private fun showInternal(data: XyPopTipData): XyPopTipHandle {
        dismissJob?.cancel()
        dismissJob = null
        currentTipFlow.value = data
        if (data.durationMillis == NEVER_DISMISS_DURATION) {
            return XyPopTipHandle(data.id, ::dismiss)
        }
        if (data.durationMillis > 0) {
            dismissJob = scope.launch {
                delay(data.durationMillis)
                dismiss(data.id)
            }
        }
        return XyPopTipHandle(data.id, ::dismiss)
    }

    private fun dismiss(id: Long? = null) {
        if (id != null && currentTipFlow.value?.id != id) {
            return
        }
        dismissJob?.cancel()
        dismissJob = null
        currentTipFlow.value = null
    }

    private fun newId(): Long {
        nextId += 1
        return nextId
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun XyPopTipHost(
    modifier: Modifier = Modifier
) {
    val currentTip by XyPopTipManager.currentTip.collectAsState()
    if (currentTip == null) return

    val tip = currentTip ?: return
    val text = tip.text ?: tip.textRes?.let { stringResource(it) } ?: return
    val dimens = XyTheme.dimens
    val iconContainerSize = dimens.innerHorizontalPadding * 2
    val iconSize = dimens.contentPadding * 2

    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = dimens.outerHorizontalPadding, vertical = dimens.innerVerticalPadding),
        contentAlignment = Alignment.TopCenter
    ) {
        Surface(
            shape = RoundedCornerShape(dimens.corner),
            shadowElevation = dimens.dialogCorner,
            tonalElevation = dimens.outerVerticalPadding,
            color = backgroundColor(tip.style)
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = dimens.innerHorizontalPadding,
                    vertical = dimens.outerVerticalPadding
                ),
                horizontalArrangement = Arrangement.spacedBy(dimens.contentPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (tip.style == XyPopTipStyle.Hint) {
                    LoadingIndicator(
                        modifier = Modifier.size(iconContainerSize)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(iconContainerSize)
                            .background(color = iconContainerColor(tip.style), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(iconResource(tip.style)),
                            contentDescription = null,
                            modifier = Modifier.size(iconSize),
                            tint = iconTint(tip.style)
                        )
                    }
                }
                Column {
                    XyTextSub(
                        text = text,
                        color = contentColor(tip.style),
                        maxLines = 3
                    )
                }
            }
        }
    }
}

private fun iconResource(style: XyPopTipStyle): DrawableResource {
    return when (style) {
        XyPopTipStyle.Default -> Res.drawable.info_24px
        XyPopTipStyle.Success -> Res.drawable.check_24px
        XyPopTipStyle.Error -> Res.drawable.warning_24px
        XyPopTipStyle.Hint -> Res.drawable.info_24px
    }
}

@Composable
private fun backgroundColor(style: XyPopTipStyle): Color {
    return when (style) {
        XyPopTipStyle.Default -> MaterialTheme.colorScheme.surface
        XyPopTipStyle.Success -> Color(0xFFD1FAE5)
        XyPopTipStyle.Error -> MaterialTheme.colorScheme.surface
        XyPopTipStyle.Hint -> Color(0xFFFFF3E0)
    }
}

@Composable
private fun contentColor(style: XyPopTipStyle): Color {
    return when (style) {
        XyPopTipStyle.Success -> Color(0xFF065F46)
        XyPopTipStyle.Hint -> Color(0xFF7A4D00)
        else -> MaterialTheme.colorScheme.onSurface
    }
}

@Composable
private fun iconContainerColor(style: XyPopTipStyle): Color {
    return when (style) {
        XyPopTipStyle.Default -> MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
        XyPopTipStyle.Success -> Color(0xFFD1FAE5)
        XyPopTipStyle.Error -> Color(0xFFFEE2E2)
        XyPopTipStyle.Hint -> Color(0xFFFFE0B2)
    }
}

@Composable
private fun iconTint(style: XyPopTipStyle): Color {
    return when (style) {
        XyPopTipStyle.Default -> MaterialTheme.colorScheme.primary
        XyPopTipStyle.Success -> Color(0xFF10B981)
        XyPopTipStyle.Error -> Color(0xFFB91C1C)
        XyPopTipStyle.Hint -> Color(0xFFB45309)
    }
}

