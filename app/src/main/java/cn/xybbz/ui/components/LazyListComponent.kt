package cn.xybbz.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonDefaults.textButtonColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import cn.xybbz.R
import cn.xybbz.ui.theme.XyTheme
import cn.xybbz.ui.xy.XyItemText

/**
 * 下拉加载封装
 *
 * implementation "com.google.accompanist:accompanist-swiperefresh:xxx"
 * */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : Any> SwipeRefreshListComponent(
    modifier: Modifier = Modifier,
    lazyState: LazyListState = rememberLazyListState(),
    collectAsLazyPagingItems: LazyPagingItems<T>?,
    listContent: LazyListScope.(LazyPagingItems<T>) -> Unit,
) {

    //是否加载中
    val isLoading by remember {
        derivedStateOf {
            collectAsLazyPagingItems == null || (collectAsLazyPagingItems.loadState.refresh is LoadState.Loading
                    || (collectAsLazyPagingItems.loadState.mediator != null
                    && collectAsLazyPagingItems.loadState.mediator?.refresh is LoadState.Loading)
                    || collectAsLazyPagingItems.loadState.source.refresh is LoadState.Loading)
                    && collectAsLazyPagingItems.itemCount <= 0
        }
    }

    val ifNotData by remember {
        derivedStateOf {
            collectAsLazyPagingItems != null && collectAsLazyPagingItems.loadState.refresh is LoadState.NotLoading
                    && ((collectAsLazyPagingItems.loadState.mediator != null
                    && collectAsLazyPagingItems.loadState.mediator?.refresh is LoadState.NotLoading)
                    || collectAsLazyPagingItems.loadState.mediator == null)
                    && collectAsLazyPagingItems.loadState.source.refresh is LoadState.NotLoading
                    && collectAsLazyPagingItems.loadState.prepend is LoadState.NotLoading
                    && collectAsLazyPagingItems.loadState.append is LoadState.NotLoading
                    && collectAsLazyPagingItems.itemCount <= 0
        }
    }

    val state = rememberPullToRefreshState()
    val isRefreshing by remember {
        derivedStateOf {
            collectAsLazyPagingItems?.loadState?.refresh is LoadState.Loading
        }
    }


    PullToRefreshBox(
        state = state,
        isRefreshing = !ifNotData && !isLoading && isRefreshing,
        onRefresh = {
            collectAsLazyPagingItems?.refresh()
        }
    ) {
        LazyColumnNotComponent(modifier = modifier, state = lazyState) {
            collectAsLazyPagingItems?.let {
                listContent(collectAsLazyPagingItems)

            }
            lazyColumBottomComponent(
                onIsLoading = { isLoading },
                onIfNotData = { ifNotData }) { collectAsLazyPagingItems }
        }
    }
}


/**
 * 列表封装
 *
 * */
@Composable
fun <T : Any> LazyListComponent(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    collectAsLazyPagingItems: LazyPagingItems<T>?,
    listContent: LazyListScope.(LazyPagingItems<T>) -> Unit,
) {

    //是否加载中
    val isLoading by remember {
        derivedStateOf {
            collectAsLazyPagingItems == null || (collectAsLazyPagingItems.loadState.refresh is LoadState.Loading
                    || (collectAsLazyPagingItems.loadState.mediator != null
                    && collectAsLazyPagingItems.loadState.mediator?.refresh is LoadState.Loading)
                    || collectAsLazyPagingItems.loadState.source.refresh is LoadState.Loading)
                    && collectAsLazyPagingItems.itemCount <= 0
        }
    }

    val ifNotData by remember {
        derivedStateOf {
            collectAsLazyPagingItems != null && collectAsLazyPagingItems.loadState.refresh is LoadState.NotLoading
                    && ((collectAsLazyPagingItems.loadState.mediator != null
                    && collectAsLazyPagingItems.loadState.mediator?.refresh is LoadState.NotLoading)
                    || collectAsLazyPagingItems.loadState.mediator == null)
                    && collectAsLazyPagingItems.loadState.source.refresh is LoadState.NotLoading
                    && collectAsLazyPagingItems.loadState.prepend is LoadState.NotLoading
                    && collectAsLazyPagingItems.loadState.append is LoadState.NotLoading
                    && collectAsLazyPagingItems.itemCount <= 0
        }
    }

    LazyColumnNotComponent(modifier = modifier, state = state) {
        collectAsLazyPagingItems?.let {
            listContent(collectAsLazyPagingItems)
        }
        lazyColumBottomComponent(
            onIsLoading = { isLoading },
            onIfNotData = { ifNotData }) { collectAsLazyPagingItems }
    }
}

/**
 * lazyColum底部组件集合
 */
fun <T : Any> LazyListScope.lazyColumBottomComponent(
    retry: (() -> Unit)? = null,
    onIsLoading: () -> Boolean,
    onIfNotData: () -> Boolean,
    onHomeMusicPagingItems: () -> LazyPagingItems<T>?
) {

    item {

        val text = if (onIfNotData())
            "已经到底了"
        else if (onIsLoading())
            "加载中..."
        else if (onHomeMusicPagingItems()?.loadState?.append is LoadState.Loading)
            "加载中..."
        else if (onHomeMusicPagingItems()?.loadState?.append?.endOfPaginationReached == true)
            "已经到底了"
        else ""
        LazyLoadingAndStatus(
            text = text,
            ifLoading = onIsLoading() || onHomeMusicPagingItems()?.loadState?.append is LoadState.Loading
        )
    }

    when {
        onHomeMusicPagingItems()?.loadState?.append is LoadState.Error -> {
            //加载更多异常
            item(key = 8888) {
                ErrorMoreRetryItem {
                    if (retry == null)
                        onHomeMusicPagingItems()?.retry()
                    else
                        retry.invoke()
                }
            }

        }

        onHomeMusicPagingItems()?.loadState?.refresh is LoadState.Error -> {
            item(key = 7777) {
                if ((onHomeMusicPagingItems()?.itemCount ?: 0) <= 0) {
                    //刷新的时候，如果itemCount小于0，第一次加载异常

                    ErrorContent {
                        onHomeMusicPagingItems()?.retry()
                    }
                } else {
                    ErrorMoreRetryItem {
                        onHomeMusicPagingItems()?.retry()
                    }
                }
            }
        }
    }
}

/**
 * 底部加载更多失败处理
 * */
@Composable
fun ErrorMoreRetryItem(onErrorText: () -> String = { "请重试" }, retry: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        TextButton(
            onClick = { retry() },
            modifier = Modifier
                .padding(20.dp)
//                .width(80.dp)
                .height(30.dp),
            shape = RoundedCornerShape(6.dp),
            contentPadding = PaddingValues(3.dp),
            colors = textButtonColors(containerColor = Color.Gray),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 2.dp,
                pressedElevation = 4.dp,
            ),
        ) {
            Text(text = onErrorText(), color = Color.White)
        }
    }
}

/**
 * 页面加载失败处理
 * */
@Composable
fun ErrorContent(retry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 100.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            modifier = Modifier.padding(top = 80.dp),
            painter = painterResource(id = R.drawable.icon_error),
            contentDescription = null
        )
        Text(text = "请求失败，请检查网络", modifier = Modifier.padding(8.dp))
        TextButton(
            onClick = { retry() },
            modifier = Modifier
                .padding(20.dp)
                .width(80.dp)
                .height(30.dp),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(5.dp),
            colors = textButtonColors(containerColor = Color.Gray),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 2.dp,
                pressedElevation = 4.dp,
            )
            //colors = ButtonDefaults
        ) { Text(text = "重试", color = Color.White) }
    }
}

/**
 * 底部加载更多正在加载中...
 * */
@Composable
fun LoadingItem(modifier: Modifier = Modifier) {
    Box(
        modifier = Modifier
            .then(modifier)
            .height(80.dp)
            .fillMaxWidth()
            .padding(5.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(24.dp),
                color = Color.Gray,
                strokeWidth = 2.dp
            )
            Text(
                text = "加载中...",
                color = Color.Gray,
                modifier = Modifier
                    .padding(start = 20.dp),
                fontSize = 18.sp,
            )
        }
    }
}

@Composable
fun LazyLoadingAndStatus(text: String, ifLoading: Boolean) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(62.dp),
        contentAlignment = Alignment.Center
    ) {
        val lineWidth = this.maxWidth / 4
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            HorizontalDivider(modifier = Modifier.width(lineWidth))
            Spacer(modifier = Modifier.width(XyTheme.dimens.corner))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (ifLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(24.dp),
                        color = Color.Gray,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(XyTheme.dimens.corner))
                }
                XyItemText(text = text)
            }
            Spacer(modifier = Modifier.width(XyTheme.dimens.corner))
            HorizontalDivider(modifier = Modifier.width(lineWidth))
        }
    }
}