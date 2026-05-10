/*
 *   XyMusic
 *   Copyright (C) 2023 xianyvbang
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package cn.xybbz.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import cn.xybbz.ui.components.AlertDialogObject
import cn.xybbz.ui.components.TopAppBarComponent
import cn.xybbz.ui.components.TopAppBarTitle
import cn.xybbz.ui.components.show
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.LazyColumnNotComponent
import cn.xybbz.ui.xy.RoundedSurfaceColumn
import cn.xybbz.ui.xy.XyButton
import cn.xybbz.ui.xy.XyColumnScreen
import cn.xybbz.ui.xy.XyText
import cn.xybbz.ui.xy.XyTextSub
import cn.xybbz.ui.xy.XyTextSubSmall
import cn.xybbz.viewmodel.MemoryManagementViewModel
import ir.ehsannarmani.compose_charts.PieChart
import ir.ehsannarmani.compose_charts.models.Pie
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.audio_cache
import xymusic_kmp.composeapp.generated.resources.audio_cache_description
import xymusic_kmp.composeapp.generated.resources.clear
import xymusic_kmp.composeapp.generated.resources.confirm_delete_database
import xymusic_kmp.composeapp.generated.resources.database_data
import xymusic_kmp.composeapp.generated.resources.database_data_description
import xymusic_kmp.composeapp.generated.resources.essential_data
import xymusic_kmp.composeapp.generated.resources.essential_data_description
import xymusic_kmp.composeapp.generated.resources.storage_management
import xymusic_kmp.composeapp.generated.resources.warning
import kotlin.math.atan2
import kotlin.math.hypot

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JvmMemoryManagementScreen(
    memoryManagementViewModel: MemoryManagementViewModel = koinViewModel<MemoryManagementViewModel>()
) {

    val warning = stringResource(Res.string.warning)
    val audioCacheTitle = stringResource(Res.string.audio_cache)
    val databaseDataTitle = stringResource(Res.string.database_data)
    val essentialDataTitle = stringResource(Res.string.essential_data)
    val chartSegments = listOf(
        JvmStorageChartSegment(
            title = audioCacheTitle,
            sizeBytes = memoryManagementViewModel.musicCacheSize.toStorageBytes(),
            color = MaterialTheme.colorScheme.primary,
        ),
        JvmStorageChartSegment(
            title = databaseDataTitle,
            sizeBytes = memoryManagementViewModel.databaseSize.toStorageBytes(),
            color = MaterialTheme.colorScheme.error,
        ),
        JvmStorageChartSegment(
            title = essentialDataTitle,
            sizeBytes = memoryManagementViewModel.appDataSize.toStorageBytes(),
            color = MaterialTheme.colorScheme.tertiary,
        ),
    )


    LaunchedEffect(Unit) {
        memoryManagementViewModel.logStorageInfo()
    }
    XyColumnScreen {
        TopAppBarComponent(
            title = {
                TopAppBarTitle(
                    title = stringResource(Res.string.storage_management)
                )
            }
        )

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.Top,
        ) {
            JvmStorageDonutChart(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                segments = chartSegments,
            )

            LazyColumnNotComponent(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentPadding = PaddingValues(
                    vertical = XyTheme.dimens.outerVerticalPadding,
                ),
                horizontalAlignment = Alignment.End,
            ) {
                item {
                    JvmMemoryManagementItem(
                        modifier = Modifier.widthIn(max = 520.dp),
                        cacheSize = memoryManagementViewModel.musicCacheSize,
                        onClick = { memoryManagementViewModel.clearMusicCache() },
                        text = audioCacheTitle,
                        describe = stringResource(Res.string.audio_cache_description)
                    )
                }
                item {
                    JvmMemoryManagementItem(
                        modifier = Modifier.widthIn(max = 520.dp),
                        cacheSize = memoryManagementViewModel.databaseSize,
                        onClick = {
                            AlertDialogObject(
                                title = warning,
                                content = {
                                    XyTextSubSmall(
                                        text = stringResource(Res.string.confirm_delete_database)
                                    )
                                },
                                ifWarning = true,
                                onConfirmation = {
                                    memoryManagementViewModel.clearDatabaseData()
                                }
                            ).show()
                        },
                        text = databaseDataTitle,
                        describe = stringResource(Res.string.database_data_description)
                    )
                }
                item {
                    JvmMemoryManagementItem(
                        modifier = Modifier.widthIn(max = 520.dp),
                        cacheSize = memoryManagementViewModel.appDataSize,
                        text = essentialDataTitle,
                        describe = stringResource(Res.string.essential_data_description),
                        ifShowButton = false
                    )
                }
            }
        }
    }
}

/**
 * 存储管理项
 * @param [modifier] 修饰语
 * @param [cacheSize] 缓存大小
 * @param [onClick] 点击时
 * @param [text] 文本
 * @param [describe] 描述
 */
@Composable
fun JvmMemoryManagementItem(
    modifier: Modifier = Modifier,
    cacheSize: String,
    text: String,
    describe: String,
    ifShowButton: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    RoundedSurfaceColumn(
        modifier = modifier.heightIn(min = 132.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = XyTheme.dimens.innerHorizontalPadding,
                    top = XyTheme.dimens.innerVerticalPadding,
                    end = XyTheme.dimens.innerHorizontalPadding,
                    bottom = XyTheme.dimens.innerVerticalPadding / 2,
                ),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                XyText(
                    text = text
                )
                XyTextSubSmall(
                    modifier = Modifier,
                    text = cacheSize
                )
            }
            if (ifShowButton)
                XyButton(
                    modifier = Modifier,
                    enabled = cacheSize != "0B",
                    onClick = { onClick?.invoke() },
                    text = stringResource(Res.string.clear)
                )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = XyTheme.dimens.innerHorizontalPadding,
                    top = XyTheme.dimens.innerVerticalPadding / 2,
                    end = XyTheme.dimens.innerHorizontalPadding,
                    bottom = XyTheme.dimens.innerVerticalPadding,
                ),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            XyTextSub(
                text = describe,
                overflow = TextOverflow.Visible
            )
        }

    }
}

private data class JvmStorageChartSegment(
    val title: String,
    val sizeBytes: Float,
    val color: Color,
)

@Composable
private fun JvmStorageDonutChart(
    modifier: Modifier = Modifier,
    segments: List<JvmStorageChartSegment>,
) {
    var hoveredSegmentIndex by remember { mutableStateOf<Int?>(null) }
    val visibleSegments = segments.filter { it.sizeBytes > 0f }
    val chartData = visibleSegments
        .mapIndexed { index, segment ->
            Pie(
                label = segment.title,
                data = segment.sizeBytes.toDouble(),
                color = segment.color,
                selectedColor = segment.color,
                selected = hoveredSegmentIndex == index,
            )
        }
    val scaleAnimationSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow,
    )

    RoundedSurfaceColumn(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-36).dp)
                .padding(
                    start = XyTheme.dimens.innerHorizontalPadding,
                    top = XyTheme.dimens.innerVerticalPadding,
                    end = XyTheme.dimens.innerHorizontalPadding,
                ),
            contentAlignment = Alignment.Center,
        ) {
            val chartSize = if (maxWidth < 280.dp) maxWidth else 280.dp

            Box(
                modifier = Modifier.size(chartSize),
                contentAlignment = Alignment.Center,
            ) {
                if (chartData.isNotEmpty()) {
                    PieChart(
                        modifier = Modifier.fillMaxSize(),
                        data = chartData,
                        selectedScale = 1.08f,
                        spaceDegree = 0f,
                        selectedPaddingDegree = 2f,
                        spaceDegreeAnimEnterSpec = scaleAnimationSpec,
                        scaleAnimEnterSpec = scaleAnimationSpec,
                        colorAnimEnterSpec = tween(220),
                        colorAnimExitSpec = tween(180),
                        scaleAnimExitSpec = tween(180),
                        spaceDegreeAnimExitSpec = tween(180),
                        style = Pie.Style.Stroke(width = chartSize * 0.33f),
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = XyTheme.dimens.innerHorizontalPadding,
                    top = XyTheme.dimens.innerVerticalPadding,
                    end = XyTheme.dimens.innerHorizontalPadding,
                    bottom = XyTheme.dimens.innerVerticalPadding,
                ),
            horizontalArrangement = Arrangement.spacedBy(XyTheme.dimens.innerHorizontalPadding / 2),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            segments.forEach { segment ->
                JvmStorageDonutLegendItem(
                    modifier = Modifier.weight(1f),
                    segment = segment,
                )
            }
        }
    }
}

@Composable
private fun JvmStorageDonutLegendItem(
    modifier: Modifier = Modifier,
    segment: JvmStorageChartSegment,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Canvas(modifier = Modifier.size(10.dp)) {
            drawCircle(color = segment.color)
        }
        Spacer(modifier = Modifier.width(XyTheme.dimens.innerHorizontalPadding / 2))
        XyTextSubSmall(
            text = segment.title,
            maxLines = 1,
        )
    }
}

private fun String.toStorageBytes(): Float {
    val text = trim()
    if (text.isEmpty()) return 0f

    val numberText = text.takeWhile { it.isDigit() || it == '.' }
    val value = numberText.toFloatOrNull() ?: return 0f
    val unit = text.drop(numberText.length).trim().uppercase()
    val multiplier = when (unit) {
        "PB" -> 1024f * 1024f * 1024f * 1024f * 1024f
        "TB" -> 1024f * 1024f * 1024f * 1024f
        "GB" -> 1024f * 1024f * 1024f
        "MB" -> 1024f * 1024f
        "KB" -> 1024f
        else -> 1f
    }
    return value * multiplier
}



