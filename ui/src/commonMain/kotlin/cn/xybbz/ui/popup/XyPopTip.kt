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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cn.xybbz.ui.xy.XyTextSub
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
        currentTipFlow.value = data
        if (data.durationMillis > 0) {
            dismissJob = scope.launch {
                if (data.durationMillis != -1L){
                    delay(data.durationMillis)
                    dismiss(data.id)
                }
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

@Composable
fun XyPopTipHost(
    modifier: Modifier = Modifier
) {
    val currentTip by XyPopTipManager.currentTip.collectAsState()
    if (currentTip == null) return

    val tip = currentTip ?: return
    val text = tip.text ?: tip.textRes?.let { stringResource(it) } ?: return

    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            shadowElevation = 18.dp,
            tonalElevation = 6.dp,
            color = backgroundColor(tip.style)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(color = iconContainerColor(tip.style), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = iconText(tip.style),
                        style = MaterialTheme.typography.bodyMedium,
                        color = iconTint(tip.style),
                        fontWeight = FontWeight.Bold
                    )
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

@Composable
private fun backgroundColor(style: XyPopTipStyle): Color {
    return when (style) {
        XyPopTipStyle.Default -> MaterialTheme.colorScheme.surface
        XyPopTipStyle.Success -> MaterialTheme.colorScheme.surface
        XyPopTipStyle.Error -> MaterialTheme.colorScheme.surface
        XyPopTipStyle.Hint -> Color(0xFFFFF3E0)
    }
}

@Composable
private fun contentColor(style: XyPopTipStyle): Color {
    return when (style) {
        XyPopTipStyle.Hint -> Color(0xFF7A4D00)
        else -> MaterialTheme.colorScheme.onSurface
    }
}

@Composable
private fun iconContainerColor(style: XyPopTipStyle): Color {
    return when (style) {
        XyPopTipStyle.Default -> MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
        XyPopTipStyle.Success -> Color(0xFFDCFCE7)
        XyPopTipStyle.Error -> Color(0xFFFEE2E2)
        XyPopTipStyle.Hint -> Color(0xFFFFE0B2)
    }
}

@Composable
private fun iconTint(style: XyPopTipStyle): Color {
    return when (style) {
        XyPopTipStyle.Default -> MaterialTheme.colorScheme.primary
        XyPopTipStyle.Success -> Color(0xFF15803D)
        XyPopTipStyle.Error -> Color(0xFFB91C1C)
        XyPopTipStyle.Hint -> Color(0xFFB45309)
    }
}

private fun iconText(style: XyPopTipStyle): String {
    return when (style) {
        XyPopTipStyle.Default -> "i"
        XyPopTipStyle.Success -> "✓"
        XyPopTipStyle.Error -> "!"
        XyPopTipStyle.Hint -> "·"
    }
}
