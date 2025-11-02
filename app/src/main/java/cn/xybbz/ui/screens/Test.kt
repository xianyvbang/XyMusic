package cn.xybbz.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlin.math.min

@Preview
@Composable
private fun TestP() {
    StickyHeaderExactlyBelowTransparentAppBar()
}
@Composable
fun StickyHeaderExactlyBelowTransparentAppBar() {
    val appBarHeight = 56.dp
    val listState = rememberLazyListState()

    Box(Modifier.fillMaxSize()) {
        // 背景（渐变可透过）
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF222222), Color(0xFF555555))
                    )
                )
        )

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            // 🔥 顶部内边距刚好等于 AppBar 高度，这样 stickyHeader 的吸附基准点就下移
            contentPadding = PaddingValues(top = appBarHeight)
        ) {
            // 顶部内容
            items(10) { index ->
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(Color.Gray.copy(alpha = 0.15f * (index + 1)))
                ) {
                    Text(
                        "Item $index",
                        Modifier.align(Alignment.Center),
                        color = Color.White
                    )
                }
            }

            // StickyHeader：不需要 offset，也不会闪
            stickyHeader {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(Color(0xAA000000))
                ) {
                    Text(
                        "🎵 Sticky Header",
                        Modifier.align(Alignment.Center),
                        color = Color.White
                    )
                }
            }

            // 下方内容
            items(20) { index ->
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(Color.DarkGray.copy(alpha = 0.1f * ((index % 5) + 1)))
                ) {
                    Text(
                        "Content Item $index",
                        Modifier.align(Alignment.Center),
                        color = Color.White
                    )
                }
            }
        }

        // 透明 AppBar 覆盖在上方（可透出 header）
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(appBarHeight)
                .background(Color.Black.copy(alpha = 0.3f))
                .zIndex(2f)
        ) {
            Text(
                "Transparent TopAppBar",
                Modifier.align(Alignment.Center),
                color = Color.White
            )
        }
    }
}

