package cn.xybbz.ui.components

import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cn.xybbz.ui.theme.XyTheme
import com.github.panpf.sketch.ability.bindPauseLoadWhenScrolling

@Composable
fun JvmLazyRowComponent(
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(
        horizontal = XyTheme.dimens.outerHorizontalPadding,
    ),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(
        XyTheme.dimens.outerHorizontalPadding / 2,
    ),
    content: LazyListScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()

    bindPauseLoadWhenScrolling(lazyListState)
    Box(
        modifier = modifier.hoverable(interactionSource = interactionSource),
    ) {
        LazyRow(
            state = lazyListState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = JvmHorizontalScrollbarBottomPadding()),
            contentPadding = contentPadding,
            horizontalArrangement = horizontalArrangement,
        ) {
            content()
        }

        JvmHorizontalScrollbar(
            visible = hovered || lazyListState.isScrollInProgress,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(horizontal = XyTheme.dimens.outerHorizontalPadding),
            adapter = rememberScrollbarAdapter(scrollState = lazyListState),
        )
    }
}
