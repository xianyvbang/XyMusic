package cn.xybbz.page

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.RemoteMediator
import cn.xybbz.common.constants.Constants

/**
 * 默认Pager配置
 */
@OptIn(ExperimentalPagingApi::class)
fun <Key : Any, Value : Any> defaultPager(
    pageSize: Int = Constants.UI_LIST_PAGE,
    initialLoadSize: Int = Constants.UI_INIT_LIST_PAGE,
    prefetchDistance: Int = Constants.UI_PREFETCH_DISTANCE,
    remoteMediator: RemoteMediator<Key, Value>?,
    pagingSourceFactory: () -> PagingSource<Key, Value>
): Pager<Key, Value> {
    return Pager(
        PagingConfig(
            pageSize = pageSize,  // 每一页个数
            initialLoadSize = initialLoadSize,
            prefetchDistance = prefetchDistance,
        ), remoteMediator = remoteMediator
    ) {
        pagingSourceFactory()
    }
}

/**
 * 大列表Pager配置
 */
@OptIn(ExperimentalPagingApi::class)
fun <Key : Any, Value : Any> bigPager(
    remoteMediator: RemoteMediator<Key, Value>?,
    pagingSourceFactory: () -> PagingSource<Key, Value>
): Pager<Key, Value> {
    return Pager(
        PagingConfig(
            pageSize = Constants.PAGE_SIZE_ALL,  // 每一页个数
            prefetchDistance = 10,
        ), remoteMediator = remoteMediator
    ) {
        pagingSourceFactory()
    }
}

/**
 * 默认Pager配置
 */
fun <Key : Any, Value : Any> defaultLocalPager(
    pagingSourceFactory: () -> PagingSource<Key, Value>
): Pager<Key, Value> {
    return Pager(
        PagingConfig(
            pageSize = Constants.UI_LIST_PAGE,  // 每一页个数
            initialLoadSize = Constants.UI_INIT_LIST_PAGE,
            prefetchDistance = Constants.UI_PREFETCH_DISTANCE,
        )
    ) {
        pagingSourceFactory()
    }
}