package cn.xybbz.ui.components

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cn.xybbz.common.enums.SortTypeEnum
import cn.xybbz.localdata.data.era.XyEraItem
import cn.xybbz.ui.xy.ModalBottomSheetExtendComponent
import cn.xybbz.ui.xy.RoundedSurfaceColumnPadding
import cn.xybbz.ui.xy.RoundedSurfacePadding
import cn.xybbz.ui.xy.XyColumnNotPadding
import cn.xybbz.ui.xy.XyItemSelect
import cn.xybbz.ui.xy.XyItemSwitcherNotTextColor
import cn.xybbz.ui.xy.XyItemTextBigPadding
import cn.xybbz.ui.xy.XyItemTextIconCheckSelect
import cn.xybbz.ui.xy.XyItemTitlePadding
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
    onRefresh: () -> Unit,
    onFilterEraTypeList: () -> List<XyEraItem>,
    onFilterEraType: () -> Int,
    onFilterEraTypeClick: (XyEraItem) -> Unit,
    onIfFavorite: () -> Boolean,
    setFavorite: (Boolean) -> Unit,
    filterContent: @Composable () -> Unit
) {

    val coroutineScope = rememberCoroutineScope()
    ModalBottomSheetExtendComponent(
        bottomSheetState = bottomSheetState,
        modifier = modifier,
        onIfDisplay = ifDisplay,
        onClose = onSetDisplay,
        dragHandle = null
    ) {
        XyColumnNotPadding(
            horizontalAlignment = Alignment.Start
        ) {
            //todo 这里的筛选文字要根据播放列表的问题学一下UI样式
            XyItemTextBigPadding(text = "筛选")
            RoundedSurfaceColumnPadding(horizontalAlignment = Alignment.Start) {
                XyItemTitlePadding(text = "年代")
                FlowRow(
                    maxItemsInEachRow = 2,
                ) {
                    onFilterEraTypeList().forEach {
                        XyItemSelect(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                coroutineScope
                                    .launch {
                                        onFilterEraTypeClick(it)
                                        bottomSheetState.hide()
                                    }
                                    .invokeOnCompletion {
                                        onSetDisplay(false)
                                        onRefresh()
                                    }
                            },
                            text = it.title,
                            color = if (onFilterEraType() != it.id) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary
                        )

                    }
                }
            }
            RoundedSurfacePadding {


                XyItemSwitcherNotTextColor(state = onIfFavorite(), onChange = {
                    coroutineScope.launch {
                        setFavorite(it)
                        bottomSheetState.hide()
                    }.invokeOnCompletion {
                        onSetDisplay(false)
                        onRefresh()
                    }
                }, text = "收藏音乐")
            }
            XyItemTextBigPadding(text = "排序")
            filterContent()
        }
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
    ifDisplay: () -> Boolean,
    onSetDisplay: (Boolean) -> Unit,
    onSortTypeClick: (SortTypeEnum?) -> Unit,
    onSortType: () -> SortTypeEnum?,
    onRefresh: () -> Unit,
    onFilterEraTypeList: () -> List<XyEraItem>,
    onFilterEraType: () -> Int,
    onFilterEraTypeClick: (XyEraItem) -> Unit,
    onIfFavorite: () -> Boolean,
    setFavorite: (Boolean) -> Unit,
    sortTypeList: List<SortTypeEnum> = SortTypeEnum.entries
) {

    val coroutineScope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )

    SelectSortBottomSheet(
        modifier = modifier,
        bottomSheetState = bottomSheetState,
        ifDisplay = ifDisplay,
        onSetDisplay = onSetDisplay,
        onRefresh = onRefresh,
        onFilterEraTypeList = onFilterEraTypeList,
        onFilterEraType = onFilterEraType,
        onFilterEraTypeClick = onFilterEraTypeClick,
        onIfFavorite = onIfFavorite,
        setFavorite = setFavorite,
        filterContent = {
            RoundedSurfaceColumnPadding {
                sortTypeList.forEach { item ->
                    XyItemTextIconCheckSelect(
                        text = item.title,
                        icon = item.imageVector,
                        onIfSelected = { onSortType() == item }) {
                        coroutineScope.launch {
                            if (onSortType() == item){
                                onSortTypeClick(null)
                            }else {
                                onSortTypeClick(item)
                            }

                            bottomSheetState.hide()
                        }.invokeOnCompletion {
                            onSetDisplay(false)
                            onRefresh()
                        }
                    }
                }
            }
        })
}