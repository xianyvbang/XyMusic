package cn.xybbz.ui.xy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import cn.xybbz.ui.ext.debounceClickable
import cn.xybbz.ui.theme.XyTheme

@Composable
fun XyColumn(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    paddingValues: PaddingValues = PaddingValues(
        horizontal = XyTheme.dimens.outerHorizontalPadding,
        vertical = XyTheme.dimens.outerVerticalPadding
    ),
    verticalArrangement: Arrangement.Vertical = Arrangement.Center,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    clipSize: Dp = XyTheme.dimens.corner,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .then(modifier)
            .fillMaxWidth()
            .padding(
                paddingValues
            )
            .background(backgroundColor,RoundedCornerShape(clipSize)),
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
        content = content
    )
}

@Composable
fun XyColumnButton(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Transparent,
    enabled: Boolean = true,
    onClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    XyColumn (
        modifier = Modifier
            .then(modifier)
            .fillMaxWidth()
            .debounceClickable (enabled = enabled){
                onClick()
            },
        backgroundColor = backgroundColor,
        paddingValues = PaddingValues(
            horizontal = XyTheme.dimens.outerHorizontalPadding,
            vertical = XyTheme.dimens.outerVerticalPadding
        ),
        horizontalAlignment= Alignment.Start,
        content = content
    )
}

@Composable
fun XyColumnNotHorizontalPadding(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    content: @Composable ColumnScope.() -> Unit
) {
    XyColumn(
        modifier = modifier,
        backgroundColor = backgroundColor,
        paddingValues = PaddingValues(
            vertical = XyTheme.dimens.outerVerticalPadding
        ),
        content = content
    )
}


@Composable
fun XyRow(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(
        horizontal = XyTheme.dimens.outerHorizontalPadding,
        vertical = XyTheme.dimens.outerVerticalPadding
    ),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceBetween,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        verticalAlignment = verticalAlignment,
        horizontalArrangement = horizontalArrangement,
        modifier = Modifier
            .then(modifier)
            .fillMaxWidth()
            .padding(
                paddingValues
            ),
        content = content
    )
}

@Composable
fun XyRowButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    XyRow(
        modifier = Modifier
            .then(modifier)
            .fillMaxWidth()
            .debounceClickable (enabled = enabled){
                onClick()
            },
        paddingValues = PaddingValues(
            horizontal = XyTheme.dimens.outerHorizontalPadding,
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
fun XyColumnScreen(
    modifier: Modifier = Modifier,
    background: Color = Color.Transparent,
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