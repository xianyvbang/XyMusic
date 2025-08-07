package cn.xybbz.ui.xy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import cn.xybbz.ui.ext.debounceClickable
import cn.xybbz.ui.theme.XyTheme

@Composable
fun XyColumn(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
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
            .background(backgroundColor),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
}

@Composable
fun XyColumnNotVerticalPadding(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .then(modifier)
            .fillMaxWidth()
            .padding(
                horizontal = XyTheme.dimens.outerHorizontalPadding
            )
            .background(backgroundColor),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = horizontalAlignment,
        content = content
    )
}

@Composable
fun XyColumnNotHorizontalPadding(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .then(modifier)
            .fillMaxWidth()
            .padding(
                vertical = XyTheme.dimens.outerVerticalPadding
            )
            .clip(RoundedCornerShape(XyTheme.dimens.corner))
            .background(backgroundColor),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
}

@Composable
fun XyColumnTopNotHorizontalPadding(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .then(modifier)
            .padding(
                vertical = XyTheme.dimens.outerVerticalPadding
            )
            .clip(RoundedCornerShape(XyTheme.dimens.corner))
            .background(backgroundColor),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
}


@Composable
fun XyColumnNotPadding(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    shape: Shape = RoundedCornerShape(XyTheme.dimens.corner),
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .then(modifier)
            .fillMaxWidth()
            .clip(shape)
            .background(backgroundColor),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = horizontalAlignment,
        content = content
    )
}

@Composable
fun XyRow(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .then(modifier)
            .fillMaxWidth()
            .padding(
                horizontal = XyTheme.dimens.outerHorizontalPadding,
                vertical = XyTheme.dimens.outerVerticalPadding
            ),
        content = content
    )
}

@Composable
fun XyRowNotHorizontalPadding(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .then(modifier)
            .fillMaxWidth()
            .padding(
                vertical = XyTheme.dimens.outerVerticalPadding
            ),
        content = content
    )
}

/**
 * 水平布局
 * 高度缩减为 ITEM_HEIGHT的二分之一
 */
@Composable
fun XyRowHeightSmall(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = Modifier
            .then(modifier)
            .fillMaxWidth()
            .heightIn(min = XyTheme.dimens.itemHeight / 2)
            .padding(
                vertical = XyTheme.dimens.outerVerticalPadding,
                horizontal = XyTheme.dimens.outerHorizontalPadding,
            ),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}


@Composable
fun XyRow(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .then(modifier)
            .fillMaxWidth()
            .debounceClickable {
                onClick()
            }
            .padding(
                horizontal = XyTheme.dimens.outerHorizontalPadding,
                vertical = XyTheme.dimens.outerVerticalPadding
            ),
        content = content
    )
}

@Composable
fun XyRowHorizontalPadding(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .then(modifier)
            .fillMaxWidth()
            .debounceClickable {
                onClick?.invoke()
            }
            .padding(
                horizontal = XyTheme.dimens.outerHorizontalPadding
            ),
        content = content
    )
}

@Composable
fun XyRowCenter(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .then(modifier)
            .fillMaxWidth()
            .padding(
                horizontal = XyTheme.dimens.outerHorizontalPadding,
                vertical = XyTheme.dimens.outerVerticalPadding
            ),
        content = content
    )
}

@Composable
fun XyRowNotPadding(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .then(modifier)
            .fillMaxWidth(),
        content = content
    )
}

@Composable
fun XyColumnScreen(
    modifier: Modifier = Modifier,
    background: Color = MaterialTheme.colorScheme.background,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .then(modifier)
            .fillMaxSize()
            .padding(bottom = XyTheme.dimens.snackBarPlayerHeight)
            .background(background),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
}

@Composable
fun XyColumnScreen(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .then(modifier)
            .fillMaxSize()
            .padding(bottom = XyTheme.dimens.snackBarPlayerHeight),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
}

@Composable
fun XyColumnNotPaddingScreen(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .then(modifier)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
}

@Composable
fun XyColumnScreen(
    modifier: Modifier = Modifier,
    background: Brush,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .then(modifier)
            .fillMaxSize()
            .padding(bottom = XyTheme.dimens.snackBarPlayerHeight)
            .background(background),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
}

@Composable
fun XyColumnMaxSizeScreen(
    modifier: Modifier = Modifier,
    background: Color = MaterialTheme.colorScheme.background,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .then(modifier)
            .fillMaxSize()
            .background(background),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
}

@Composable
fun XyBoxWithConstraintsScreen(
    modifier: Modifier = Modifier,
    content: @Composable @UiComposable BoxWithConstraintsScope.() -> Unit
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        content = content
    )
}

@Composable
fun XyBoxScreen(
    modifier: Modifier = Modifier,
    background: Color = MaterialTheme.colorScheme.background,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .then(modifier)
            .fillMaxSize()
            .semantics { isTraversalGroup = true }
            .padding(bottom = XyTheme.dimens.snackBarPlayerHeight)
            .background(background),
        content = content
    )
}