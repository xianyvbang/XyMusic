package cn.xybbz.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuOpen
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.xybbz.R
import cn.xybbz.common.enums.SortTypeEnum
import cn.xybbz.common.utils.DateUtil
import cn.xybbz.localdata.data.era.XyEraItem
import cn.xybbz.ui.ext.composeClick
import cn.xybbz.ui.popup.MenuItemDefaultData
import cn.xybbz.ui.popup.XyDropdownMenu
import cn.xybbz.ui.xy.ModalBottomSheetExtendComponent
import cn.xybbz.ui.xy.RoundedSurfaceColumnPadding
import cn.xybbz.ui.xy.XyItemTextIconCheckSelect
import cn.xybbz.ui.xy.XyItemTitle
import cn.xybbz.ui.xy.XyRow
import kotlinx.coroutines.launch


/**
 * 选择排序类型组件
 * @param [modifier] 修饰语
 * @param [ifDisplay] if 显示
 * @param [onSetDisplay] 在设置显示时
 * @param [onSortTypeClick] 在排序类型上单击
 * @param [onRefresh] 刷新时
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
    filterContent: @Composable () -> Unit
) {
    ModalBottomSheetExtendComponent(
        bottomSheetState = bottomSheetState,
        modifier = modifier,
        onIfDisplay = ifDisplay,
        onClose = onSetDisplay,
        titleText = stringResource(R.string.select_sort_method)
    ) {
        filterContent()
    }
}


/**
 * 选择排序类型组件
 * @param [modifier] 修饰语
 * @param [ifDisplay] if 显示
 * @param [onSetDisplay] 在设置显示时
 * @param [onSortTypeClick] 在排序类型上单击
 * @param [onRefresh] 刷新时
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectSortBottomSheetComponent(
    modifier: Modifier = Modifier,
    onIfYearFilter: () -> Boolean?,
    onIfSelectOneYear: () -> Boolean?,
    onIfStartEndYear: () -> Boolean?,
    onIfSort: () -> Boolean?,
    onIfFavoriteFilter: () -> Boolean?,
    onSortTypeClick: suspend (SortTypeEnum?) -> Unit,
    onSortType: () -> SortTypeEnum?,
    onFilterEraTypeList: () -> List<XyEraItem>,
    onFilterEraTypeClick: suspend (XyEraItem) -> Unit,
    onIfFavorite: () -> Boolean,
    setFavorite: suspend (Boolean) -> Unit,
    sortTypeList: List<SortTypeEnum> = SortTypeEnum.entries,
    onYearSet: () -> Set<Int>,
    onSelectYear: () -> Int?,
    onSetSelectYear: suspend (Int?) -> Unit,
    onSelectRangeYear: () -> List<Int>?,
    onSetSelectRangeYear: suspend (List<Int?>) -> Unit,
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
     * 打开年代筛选bottom
     */
    var ifOpenAgeFilterBottom by remember {
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
                imageVector = Icons.AutoMirrored.Rounded.MenuOpen,
                contentDescription = stringResource(R.string.open_sort_and_filter_menu)
            )
        }

        XyDropdownMenu(
            onIfShowMenu = { ifShowSortOrFilterMenu },
            onSetIfShowMenu = { ifShowSortOrFilterMenu = it },
            modifier = Modifier
                .width(200.dp),
            itemDataList = listOf(
                MenuItemDefaultData(
                    title = if (onIfFavorite()) stringResource(R.string.cancel_favorite_filter) else stringResource(
                        R.string.get_favorite_music
                    ),
                    trailingIcon = {
                        Icon(
                            imageVector = if (onIfFavorite()) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = if (onIfFavorite()) stringResource(R.string.can_cancel_favorite_filter) else stringResource(
                                R.string.can_get_favorite_music
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
                //年代筛选
                MenuItemDefaultData(
                    title = stringResource(R.string.era_filter),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.CalendarToday,
                            contentDescription = stringResource(R.string.era_filter),
                        )

                    },
                    onClick = composeClick {
                        ifShowSortOrFilterMenu = false
                        ifOpenAgeFilterBottom = true
                    },
                    ifItemShow = { onIfYearFilter() == true && onIfSelectOneYear.invoke() == false }),
                //单年筛选
                MenuItemDefaultData(
                    title = stringResource(R.string.year_filter),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.CalendarToday,
                            contentDescription = stringResource(R.string.year_filter),
                        )

                    },
                    onClick = composeClick {
                        ifShowSortOrFilterMenu = false
                        ifOpenYearFilterBottom = true
                    },
                    ifItemShow = { onIfYearFilter() == true && onIfSelectOneYear.invoke() == true }),
                //年范围筛选
                MenuItemDefaultData(
                    title = stringResource(R.string.year_filter),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.CalendarToday,
                            contentDescription = stringResource(R.string.year_filter),
                        )

                    },
                    onClick = composeClick {
                        ifShowSortOrFilterMenu = false
                        ifOpenYearRangeFilterBottom = true
                    },
                    ifItemShow = { onIfYearFilter() == true && onIfStartEndYear.invoke() == true }),
                //排序功能
                MenuItemDefaultData(
                    title = stringResource(R.string.sort_method),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Sort,
                            contentDescription = stringResource(R.string.sort_method),
                        )

                    },
                    onClick = composeClick {
                        ifShowSortOrFilterMenu = false
                        ifOpenSortBottom = true
                    },
                    ifItemShow = { onIfSort() == true })

            )
        )
    }

    AgeFilterComponent(
        ifDisplay = { ifOpenAgeFilterBottom },
        onSetDisplay = {
            ifOpenAgeFilterBottom = it
        },
        onFilterEraTypeList = onFilterEraTypeList,
        onFilterEraTypeClick = {
            selectEraId = it.id
            coroutineScope.launch {
                onFilterEraTypeClick(it)
            }
        },
        onFilterEraType = { selectEraId ?: 0 }
    )

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
            RoundedSurfaceColumnPadding(
                color = Color.Transparent
            ) {
                sortTypeList.forEach { item ->
                    XyItemTextIconCheckSelect(
                        text = stringResource(item.title),
                        icon = item.imageVector,
                        onIfSelected = { onSortType() == item }) {
                        coroutineScope.launch {
                            if (onSortType() == item) {
                                onSortTypeClick(null)
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

/**
 * 使用年代过滤
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AgeFilterComponent(
    modifier: Modifier = Modifier,
    bottomSheetState: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    ),
    ifDisplay: () -> Boolean,
    onSetDisplay: (Boolean) -> Unit,
    onFilterEraTypeList: () -> List<XyEraItem>,
    onFilterEraTypeClick: (XyEraItem) -> Unit,
    onFilterEraType: () -> Int,
) {
    val coroutineScope = rememberCoroutineScope()
    ModalBottomSheetExtendComponent(
        bottomSheetState = bottomSheetState,
        modifier = modifier,
        onIfDisplay = ifDisplay,
        onClose = onSetDisplay,
        titleText = stringResource(R.string.era_filter)
    ) {
        RoundedSurfaceColumnPadding(
            color = Color.Transparent
        ) {
            onFilterEraTypeList().forEach {
                XyItemTextIconCheckSelect(
                    onClick = {
                        coroutineScope
                            .launch {
                                onFilterEraTypeClick(it)
                                bottomSheetState.hide()
                            }
                            .invokeOnCompletion {
                                onSetDisplay(false)
                            }
                    },
                    text = it.title + stringResource(R.string.decade_suffix),
                    onIfSelected = { onFilterEraType() == it.id }
                )
            }
        }

    }
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
                Text(stringResource(R.string.cancel))
            }

            XyItemTitle(text = stringResource(R.string.year_selection), fontSize = 18.sp)

            TextButton(onClick = {
                coroutineScope.launch {
                    onSetSelectYear(year)
                    bottomSheetState.hide()
                }.invokeOnCompletion {
                    onSetDisplay(false)
                }
            }) {
                Text(stringResource(R.string.confirm))
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
                Text(stringResource(R.string.cancel))
            }
            XyItemTitle(text = stringResource(R.string.year_selection), fontSize = 18.sp)

            TextButton(onClick = {
                coroutineScope.launch {
                    onSetSelectRangeYear(yearList)
                    bottomSheetState.hide()
                }.invokeOnCompletion {
                    onSetDisplay(false)
                }

            }) {
                Text(stringResource(R.string.confirm))
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