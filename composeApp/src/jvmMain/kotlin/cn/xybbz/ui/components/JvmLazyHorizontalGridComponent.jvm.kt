package cn.xybbz.ui.components

import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cn.xybbz.ui.common.UiConstants.MusicCardImageSize
import cn.xybbz.ui.theme.XyTheme
import com.github.panpf.sketch.ability.bindPauseLoadWhenScrolling

@Composable
fun JvmLazyHorizontalGridComponent(
    modifier: Modifier = Modifier,
    lazyGridState: LazyGridState = rememberLazyGridState(),
    rows: GridCells = GridCells.Adaptive(MusicCardImageSize),
    contentPadding: PaddingValues = PaddingValues(
        horizontal = XyTheme.dimens.outerHorizontalPadding,
        vertical = XyTheme.dimens.outerVerticalPadding,
    ),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(
        XyTheme.dimens.innerVerticalPadding,
    ),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(
        XyTheme.dimens.outerVerticalPadding,
        Alignment.CenterVertically,
    ),
    content: LazyGridScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()

    bindPauseLoadWhenScrolling(lazyGridState)
    Box(
        modifier = modifier.hoverable(interactionSource = interactionSource),
    ) {
        LazyHorizontalGrid(
            state = lazyGridState,
            rows = rows,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = contentPadding,
            horizontalArrangement = horizontalArrangement,
            verticalArrangement = verticalArrangement,
        ) {
            content()
        }

        JvmHorizontalScrollbar(
            visible = hovered || lazyGridState.isScrollInProgress,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(horizontal = XyTheme.dimens.outerHorizontalPadding),
            adapter = rememberScrollbarAdapter(scrollState = lazyGridState),
        )
    }
}
