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

package cn.xybbz.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cn.xybbz.common.enums.SortTypeEnum
import cn.xybbz.common.utils.DateUtil
import cn.xybbz.ui.ext.composeClick
import cn.xybbz.ui.popup.MenuItemDefaultData
import cn.xybbz.ui.popup.XyDropdownMenu
import cn.xybbz.ui.xy.LazyColumnBottomSheetComponent
import cn.xybbz.ui.xy.ModalBottomSheetExtendComponent
import cn.xybbz.ui.xy.XyItemIconSelect
import cn.xybbz.ui.xy.XyRow
import cn.xybbz.ui.xy.XyText
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import xymusic_kmp.composeapp.generated.resources.Res
import xymusic_kmp.composeapp.generated.resources.calendar_today_24px
import xymusic_kmp.composeapp.generated.resources.can_cancel_favorite_filter
import xymusic_kmp.composeapp.generated.resources.can_get_favorite_music
import xymusic_kmp.composeapp.generated.resources.cancel
import xymusic_kmp.composeapp.generated.resources.cancel_favorite_filter
import xymusic_kmp.composeapp.generated.resources.clear_filters
import xymusic_kmp.composeapp.generated.resources.close_24px
import xymusic_kmp.composeapp.generated.resources.confirm
import xymusic_kmp.composeapp.generated.resources.favorite_24px
import xymusic_kmp.composeapp.generated.resources.favorite_border_24px
import xymusic_kmp.composeapp.generated.resources.get_favorite_music
import xymusic_kmp.composeapp.generated.resources.menu_open_24px
import xymusic_kmp.composeapp.generated.resources.open_sort_and_filter_menu
import xymusic_kmp.composeapp.generated.resources.select_sort_method
import xymusic_kmp.composeapp.generated.resources.sort_24px
import xymusic_kmp.composeapp.generated.resources.sort_method
import xymusic_kmp.composeapp.generated.resources.year_filter
import xymusic_kmp.composeapp.generated.resources.year_selection
import cn.xybbz.ui.xy.XyIconButton as IconButton


/**
 * 选择排序类型组件
 * @param [modifier] 修饰语
 * @param [ifDisplay] if 显示
 * @param [onSetDisplay] 在设置显示时
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SelectSortBottomSheet(
    modifier: Modifier = Modifier,
    bottomSheetState: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    ),
    ifDisplay: () -> Boolean,
    onSetDisplay: (Boolean) -> Unit,
    filterContent: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheetExtendComponent(
        bottomSheetState = bottomSheetState,
        modifier = modifier,
        onIfDisplay = ifDisplay,
        onClose = onSetDisplay,
        dragHandle = null,
        titleText = stringResource(Res.string.select_sort_method)
    ) {
        filterContent()
    }
}


/**
 * 选择排序类型组件
 * @param [modifier] 修饰语
 * @param [onSortTypeClick] 在排序类型上单击
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectSortBottomSheetComponent(
    modifier: Modifier = Modifier,
    onIfSelectOneYear: () -> Boolean?,
    onIfStartEndYear: () -> Boolean?,
    onIfSort: () -> Boolean?,
    onIfFavoriteFilter: () -> Boolean?,
    onSortTypeClick: suspend (SortTypeEnum?) -> Unit,
    onSortType: () -> SortTypeEnum?,
    onDefaultSortType: () -> SortTypeEnum?,
    onIfFavorite: () -> Boolean,
    setFavorite: suspend (Boolean) -> Unit,
    sortTypeList: List<SortTypeEnum> = SortTypeEnum.entries,
    onYearSet: () -> Set<Int>,
    onSelectYear: () -> Int?,
    onSetSelectYear: suspend (Int?) -> Unit,
    onSelectRangeYear: () -> List<Int>?,
    onSetSelectRangeYear: suspend (List<Int?>) -> Unit,
    onEnabledClearClick: () -> Boolean,
    onClearFilterOrShort: suspend () -> Unit,
) {

    val coroutineScope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )

    var selectEraId by remember {
        mutableStateOf<Int?>(null)
    }

    /**
     * 打开菜单页面
     */
    var ifShowSortOrFilterMenu by remember {
        mutableStateOf(false)
    }

    /**
     * 打开年份筛选bottom
     */
    var ifOpenYearFilterBottom by remember {
        mutableStateOf(false)
    }

    /**
     * 打开年份范围筛选bottom
     */
    var ifOpenYearRangeFilterBottom by remember {
        mutableStateOf(false)
    }

    /**
     * 打开排序功能bottom
     */
    var ifOpenSortBottom by remember {
        mutableStateOf(false)
    }


    Box {
        IconButton(onClick = {
            ifShowSortOrFilterMenu = true
        }) {
            Icon(
                painter = painterResource(Res.drawable.menu_open_24px),
                contentDescription = stringResource(Res.string.open_sort_and_filter_menu)
            )
        }

        XyDropdownMenu(
            onIfShowMenu = { ifShowSortOrFilterMenu },
            onSetIfShowMenu = { ifShowSortOrFilterMenu = it },
            modifier = Modifier
                .width(250.dp),
            itemDataList = listOf(
                MenuItemDefaultData(
                    title = if (onIfFavorite()) stringResource(Res.string.cancel_favorite_filter) else stringResource(
                        Res.string.get_favorite_music
                    ),
                    trailingIcon = {
                        Icon(
                            painter = painterResource(if (onIfFavorite()) Res.drawable.favorite_24px else Res.drawable.favorite_border_24px),
                            contentDescription = if (onIfFavorite()) stringResource(Res.string.can_cancel_favorite_filter) else stringResource(
                                Res.string.can_get_favorite_music
                            ),
                            tint = if (onIfFavorite()) Color.Red else MaterialTheme.colorScheme.onSurface
                        )

                    },
                    onClick = composeClick {
                        ifShowSortOrFilterMenu = false
                        coroutineScope.launch {
                            setFavorite(!onIfFavorite())
                        }
                    }, ifItemShow = { onIfFavoriteFilter() == true }),
                //单年筛选
                MenuItemDefaultData(
                    title = stringResource(Res.string.year_filter),
                    trailingIcon = {
                        Icon(
                            painter = painterResource(Res.drawable.calendar_today_24px),
                            contentDescription = stringResource(Res.string.year_filter),
                        )

                    },
                    onClick = composeClick {
                        ifShowSortOrFilterMenu = false
                        ifOpenYearFilterBottom = true
                    },
                    ifItemShow = { onIfSelectOneYear.invoke() == true }),
                //年范围筛选
                MenuItemDefaultData(
                    title = stringResource(Res.string.year_filter),
                    trailingIcon = {
                        Icon(
                            painter = painterResource(Res.drawable.calendar_today_24px),
                            contentDescription = stringResource(Res.string.year_filter),
                        )

                    },
                    onClick = composeClick {
                        ifShowSortOrFilterMenu = false
                        ifOpenYearRangeFilterBottom = true
                    },
                    ifItemShow = { onIfStartEndYear.invoke() == true }),
                //排序功能
                MenuItemDefaultData(
                    title = stringResource(Res.string.sort_method),
                    trailingIcon = {
                        Icon(
                            painter =painterResource(Res.drawable.sort_24px),
                            contentDescription = stringResource(Res.string.sort_method),
                        )

                    },
                    onClick = composeClick {
                        ifShowSortOrFilterMenu = false
                        ifOpenSortBottom = true
                    },
                    ifItemShow = { onIfSort() == true }),
                MenuItemDefaultData(
                    title = stringResource(Res.string.clear_filters),
                    enabled = onEnabledClearClick(),
                    trailingIcon = {
                        Icon(
                            painter = painterResource(Res.drawable.close_24px),
                            contentDescription = stringResource(Res.string.clear_filters),
                        )

                    },
                    onClick = composeClick() {
                        ifShowSortOrFilterMenu = false
                        coroutineScope.launch {
                            onClearFilterOrShort()
                        }
                    },
                    ifItemShow = { true })

            )
        )
    }

    YearFilterComponent(
        ifDisplay = { ifOpenYearFilterBottom },
        onSetDisplay = { ifOpenYearFilterBottom = it },
        onYearSet = onYearSet,
        onSelectYear = onSelectYear,
        onSetSelectYear = onSetSelectYear
    )

    YearRangeFilterComponent(
        ifDisplay = { ifOpenYearRangeFilterBottom },
        onSetDisplay = { ifOpenYearRangeFilterBottom = it },
        onYearSet = { listOf(onYearSet(), onYearSet()) },
        onSelectRangeYear = onSelectRangeYear,
        onSetSelectRangeYear = onSetSelectRangeYear
    )


    SelectSortBottomSheet(
        modifier = modifier,
        bottomSheetState = bottomSheetState,
        ifDisplay = { ifOpenSortBottom },
        onSetDisplay = { ifOpenSortBottom = it },
        filterContent = {
            LazyColumnBottomSheetComponent(vertical = 0.dp) {
                items(sortTypeList) { item ->
                    XyItemIconSelect(
                        text = stringResource(item.title),
                        painter = item.painter,
                        onIfSelected = { onSortType() == item }) {
                        coroutineScope.launch {
                            if (onSortType() == item) {
                                onSortTypeClick(onDefaultSortType())
                            } else {
                                onSortTypeClick(item)
                            }

                            bottomSheetState.hide()
                        }.invokeOnCompletion {
                            ifShowSortOrFilterMenu = false
                        }
                    }
                }
            }
        })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun YearFilterComponent(
    modifier: Modifier = Modifier,
    bottomSheetState: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    ),
    ifDisplay: () -> Boolean,
    onSetDisplay: (Boolean) -> Unit,
    onYearSet: () -> Set<Int>,
    onSelectYear: () -> Int?,
    onSetSelectYear: suspend (Int?) -> Unit
) {
    ModalBottomSheetExtendComponent(
        bottomSheetState = bottomSheetState,
        modifier = modifier,
        onIfDisplay = ifDisplay,
        onClose = onSetDisplay,
        dragHandle = null
    ) {

        val coroutineScope = rememberCoroutineScope()

        var year by remember {
            mutableIntStateOf(onSelectYear() ?: onYearSet().first())
        }

        XyRow {
            TextButton(onClick = {
                coroutineScope.launch {
                    bottomSheetState.hide()
                }.invokeOnCompletion {
                    onSetDisplay(false)
                }
            }) {
                Text(stringResource(Res.string.cancel))
            }

            XyText(text = stringResource(Res.string.year_selection))

            TextButton(onClick = {
                coroutineScope.launch {
                    onSetSelectYear(year)
                    bottomSheetState.hide()
                }.invokeOnCompletion {
                    onSetDisplay(false)
                }
            }) {
                Text(stringResource(Res.string.confirm))
            }
        }
        Picker(
            items = onYearSet(),
            backgroundColor = Color.Transparent,
            selectedItem = year,
            onValueChange = {
                year = it
            })
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun YearRangeFilterComponent(
    modifier: Modifier = Modifier,
    bottomSheetState: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    ),
    ifDisplay: () -> Boolean,
    onSetDisplay: (Boolean) -> Unit,
    onYearSet: () -> List<Set<Int>>,
    onSelectRangeYear: () -> List<Int>?,
    onSetSelectRangeYear: suspend (List<Int?>) -> Unit
) {

    var yearList by remember {
        mutableStateOf(onSelectRangeYear() ?: listOf(DateUtil.thisYear(), DateUtil.thisYear()))
    }

    val coroutineScope = rememberCoroutineScope()

    ModalBottomSheetExtendComponent(
        bottomSheetState = bottomSheetState,
        modifier = modifier,
        onIfDisplay = ifDisplay,
        onClose = onSetDisplay,
        dragHandle = null
    ) {
        XyRow {
            TextButton(onClick = {
                coroutineScope.launch {
                    bottomSheetState.hide()
                }.invokeOnCompletion {
                    onSetDisplay(false)
                }
            }) {
                Text(stringResource(Res.string.cancel))
            }
            XyText(text = stringResource(Res.string.year_selection))

            TextButton(onClick = {
                coroutineScope.launch {
                    onSetSelectRangeYear(yearList)
                    bottomSheetState.hide()
                }.invokeOnCompletion {
                    onSetDisplay(false)
                }

            }) {
                Text(stringResource(Res.string.confirm))
            }
        }
        MultiPicker(
            items = onYearSet(),
            selectedItems = yearList,
            backgroundColor = Color.Transparent,
            onValueChange = {
                yearList = it
            })

    }
}

