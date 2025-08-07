package cn.xybbz.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cn.xybbz.ui.theme.XyTheme

/**
 * 通用LazyColumn
 * @param [modifier] 样式
 * @param [items] Lazy的item
 */
@Composable
fun LazyColumnComponent(
    modifier: Modifier = Modifier,
    items: LazyListScope.() -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(),
        contentPadding = PaddingValues(
            vertical = XyTheme.dimens.outerVerticalPadding,
            horizontal = XyTheme.dimens.outerHorizontalPadding
        ),
        verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding / 2),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = {
            items()
            item {
                Spacer(
                    modifier = Modifier.height(
                        XyTheme.dimens.snackBarPlayerHeight + WindowInsets.navigationBars.asPaddingValues()
                            .calculateBottomPadding()
                    )
                )
            }
        }
    )
}

@Composable
fun LazyColumnNotComponent(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    items: LazyListScope.() -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth(),
        state = state,
        content = {
            items()
            item {
                Spacer(
                    modifier = Modifier.height(
                        XyTheme.dimens.snackBarPlayerHeight + WindowInsets.navigationBars.asPaddingValues()
                            .calculateBottomPadding()
                    )
                )
            }
        }
    )
}

@Composable
fun LazyColumnNotHorizontalComponent(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    items: LazyListScope.() -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
        state = state,
        contentPadding = PaddingValues(
            vertical = XyTheme.dimens.outerVerticalPadding
        ),
        verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding / 2),
        horizontalAlignment = horizontalAlignment,
        content = {
            items()
            item {
                Spacer(
                    modifier = Modifier.height(
                        XyTheme.dimens.snackBarPlayerHeight + WindowInsets.navigationBars.asPaddingValues()
                            .calculateBottomPadding()
                    )
                )
            }
        }
    )
}

@Composable
fun LazyColumnNotVerticalComponent(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    items: LazyListScope.() -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
        state = state,
        contentPadding = PaddingValues(
            horizontal = XyTheme.dimens.outerHorizontalPadding
        ),
        verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding / 2),
        horizontalAlignment = horizontalAlignment,
        content = {
            items()
            item {
                Spacer(
                    modifier = Modifier.height(
                        XyTheme.dimens.snackBarPlayerHeight + WindowInsets.navigationBars.asPaddingValues()
                            .calculateBottomPadding()
                    )
                )
            }
        }
    )
}

/**
 * 通用LazyColumn
 * @param [modifier] 样式
 * @param [items] Lazy的item
 */
@Composable
fun LazyColumnNotBottomComponent(
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
    items: LazyListScope.() -> Unit
) {
    LazyColumn(
        state = lazyListState,
        modifier = modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(
            vertical = XyTheme.dimens.outerVerticalPadding,
            horizontal = XyTheme.dimens.outerHorizontalPadding
        ),
        verticalArrangement = Arrangement.spacedBy(XyTheme.dimens.outerVerticalPadding / 2),
        content = {
            items()
            item {
                Spacer(
                    modifier = Modifier.height(
                        XyTheme.dimens.snackBarPlayerHeight + WindowInsets.navigationBars.asPaddingValues()
                            .calculateBottomPadding()
                    )
                )
            }
        }
    )
}