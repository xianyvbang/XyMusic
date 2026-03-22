package cn.xybbz.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cn.xybbz.ui.theme.XyTheme

@Composable
fun LazyRowComponent(modifier: Modifier = Modifier, content: LazyListScope.() -> Unit) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = XyTheme.dimens.outerHorizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerHorizontalPadding / 2),
    ){
        content()
    }
}