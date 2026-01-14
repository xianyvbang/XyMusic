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

package cn.xybbz.page.parent

import androidx.paging.PagingSource
import androidx.paging.PagingState
import cn.xybbz.api.client.IDataSourceParentServer
import cn.xybbz.localdata.data.artist.XyArtist

class ResemblanceArtistRemoteMediator(
    private val artistId: String,
    private val datasourceServer: IDataSourceParentServer
) : PagingSource<Int, XyArtist>() {
    override fun getRefreshKey(state: PagingState<Int, XyArtist>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, XyArtist> {
        try {
            // Start refresh at page 1 if undefined.
            val loadKey = params.key ?: 0
            val pageSize = params.loadSize
            val response = datasourceServer.getSimilarArtistsRemotely(
                artistId,
                startIndex = 0,
                pageSize
            )
            return LoadResult.Page(
                data = response.items ?: listOf(),
                prevKey = null, // Only paging forward.
                nextKey = null
            )
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }

}