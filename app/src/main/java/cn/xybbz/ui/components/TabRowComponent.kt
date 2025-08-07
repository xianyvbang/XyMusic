package cn.xybbz.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cn.xybbz.ui.ext.debounceClickable
import cn.xybbz.ui.theme.primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabRowComponent(
    modifier: Modifier = Modifier,
    onSelectIndex: () -> Int,
    onClick: (Int) -> Unit,
    tabList: List<String>
) {
    PrimaryTabRow(
        modifier = modifier.width(250.dp),
        selectedTabIndex = onSelectIndex(),
        divider = {}
    ) {
        tabList.forEachIndexed { index, it ->
            Box(
                modifier = Modifier
                    .padding(bottom = 3.dp)
                    .debounceClickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onClick(index)
                    }, contentAlignment = Alignment.Center
            ) {
                Text(
                    text = it,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (onSelectIndex() == index) primary else Color.Unspecified
                )
            }
        }
    }
}