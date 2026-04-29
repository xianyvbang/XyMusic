package cn.xybbz.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cn.xybbz.ui.common.UiConstants.MusicCardImageSize
import cn.xybbz.ui.theme.XyTheme
import com.github.panpf.sketch.ability.bindPauseLoadWhenScrolling

@Composable
fun LazyHorizontalGridComponent(
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
    bindPauseLoadWhenScrolling(lazyGridState)
    LazyHorizontalGrid(
        state = lazyGridState,
        rows = rows,
        modifier = modifier.fillMaxWidth(),
        contentPadding = contentPadding,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
    ) {
        content()
    }
}
