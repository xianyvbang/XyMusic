package cn.xybbz.ui.xy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import cn.xybbz.ui.theme.XyTheme

@Composable
fun XyRoundedSurface(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    content: @Composable () -> Unit
) {

    Surface(
        modifier = modifier,
        color = color,
        shape = RoundedCornerShape(XyTheme.dimens.corner),
        content = content
    )
}

@Composable
fun XyRoundedBox(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .then(modifier)
            .padding(
                horizontal = XyTheme.dimens.outerHorizontalPadding,
                vertical = XyTheme.dimens.outerVerticalPadding
            )
            .background(backgroundColor, RoundedCornerShape(XyTheme.dimens.corner)),
        contentAlignment = Alignment.Center,
        content = content
    )
}

@Composable
fun RoundedSurfacePadding(modifier: Modifier = Modifier, content: @Composable () -> Unit) {

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = XyTheme.dimens.outerHorizontalPadding,
                vertical = XyTheme.dimens.outerVerticalPadding
            ),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = RoundedCornerShape(XyTheme.dimens.corner),
        content = content
    )
}

@Composable
fun RoundedSurfaceColumnPadding(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    content: @Composable ColumnScope.() -> Unit
) {

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = XyTheme.dimens.outerHorizontalPadding,
                vertical = XyTheme.dimens.outerVerticalPadding
            ),
        color = color,
        shape = RoundedCornerShape(XyTheme.dimens.corner),
        content = {
            Column(
                horizontalAlignment = horizontalAlignment,
                content = content
            )
        }
    )
}


@Composable
fun RoundedSurfaceColumnPadding(
    modifier: Modifier = Modifier,
    brush: Brush,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    content: @Composable ColumnScope.() -> Unit
) {

    Column(
        modifier = Modifier
            .then(modifier)
            .fillMaxWidth()
            .padding(
                horizontal = XyTheme.dimens.outerHorizontalPadding,
                vertical = XyTheme.dimens.outerVerticalPadding
            )
            .clip(RoundedCornerShape(XyTheme.dimens.corner))
            .background(brush),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = horizontalAlignment,
        content = content
    )
}

@Composable
fun RoundedSurfaceColumn(
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    content: @Composable ColumnScope.() -> Unit
) {

    Surface(
        modifier = modifier
            .fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = RoundedCornerShape(XyTheme.dimens.corner),
        content = {
            Column(
                horizontalAlignment = horizontalAlignment,
                content = content
            )
        }
    )
}

@Composable
fun RoundedSurfaceRowPadding(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceBetween,
    content: @Composable RowScope.() -> Unit
) {

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = XyTheme.dimens.outerHorizontalPadding,
                vertical = XyTheme.dimens.outerVerticalPadding
            ),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = RoundedCornerShape(XyTheme.dimens.corner),
        content = {
            Row(
                horizontalArrangement = horizontalArrangement,
                content = content
            )
        }
    )
}