package cn.xybbz.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import cn.xybbz.ui.theme.XyTheme

@Composable
fun ItemSwitcher(
    state: Boolean,
    onChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    iconPainter: Painter? = null,
    iconPaddingValues: PaddingValues = PaddingValues(0.dp),
    iconColor: Color? = null,
    text: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .alpha(if (enabled) 1f else 0.5f)
            .clickable(enabled = enabled) {
                onChange(!state)
            }
            .padding(
                horizontal = XyTheme.dimens.outerHorizontalPadding,
                vertical = XyTheme.dimens.outerVerticalPadding
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        iconPainter?.let {
            Image(
                modifier = Modifier
                    .size(24.dp)
                    .padding(iconPaddingValues),
                painter = iconPainter,
                contentDescription = null,
                colorFilter = iconColor?.let { ColorFilter.tint(iconColor) }
            )
            Spacer(modifier = Modifier.width(XyTheme.dimens.contentPadding))
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Text(
                text = text,
                color = if (enabled) Color(0xFF0470E6) else Color(0xFF8C8C8C)
            )
        }
        Spacer(modifier = Modifier.width(XyTheme.dimens.contentPadding))
        val backgroundColor by animateColorAsState(
            targetValue = if (state) Color(0xFF0470E6) else Color(0xFF8C8C8C).copy(alpha = 0.1f),
            animationSpec = spring(),
            label = "backgroundColor"
        )
        Box(
            modifier = Modifier
                .size(46.dp, 26.dp)
                .clip(CircleShape)
                .drawBehind {
                    drawRect(color = backgroundColor)
                }
                .padding(5.dp)
        ) {
            val layoutDirection = LocalLayoutDirection.current
            val translationX by animateDpAsState(
                targetValue = if (state) {
                    when (layoutDirection) {
                        LayoutDirection.Ltr -> 20.dp
                        LayoutDirection.Rtl -> (-20).dp
                    }
                } else {
                    0.dp
                },
                animationSpec = spring(),
                label = "startPadding"
            )
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        this.translationX = translationX.toPx()
                    }
                    .size(16.dp)
                    .border(width = 4.dp, color = Color.White, shape = CircleShape)
            )
        }
    }
}