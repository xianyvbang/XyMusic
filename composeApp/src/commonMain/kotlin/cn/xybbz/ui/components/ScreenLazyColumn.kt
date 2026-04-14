package cn.xybbz.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cn.xybbz.ui.xy.LazyColumnNotComponent
import org.jetbrains.compose.resources.stringResource
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.loading
import xymusic_kmp.composeapp.generated.resources.reached_bottom

@Composable
fun ScreenLazyColumn(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    ifLoading: Boolean = false,
    items: LazyListScope.() -> Unit
) {
    LazyColumnNotComponent(
        modifier = modifier,
        state = state,
        contentPadding = contentPadding,
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
        items = {
            items()
            item {
                LazyLoadingAndStatus(
                    if (ifLoading) stringResource(Res.string.loading) else stringResource(Res.string.reached_bottom),
                    ifLoading = ifLoading
                )
            }
        })
}
