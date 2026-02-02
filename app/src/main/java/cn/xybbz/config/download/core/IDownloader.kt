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

package cn.xybbz.config.download.core


interface IDownloader: AutoCloseable {
    suspend fun initData(connectionId: Long)
    suspend fun enqueue(vararg requests: DownloadRequest)
    fun pause(vararg ids: Long)
    fun pauseAll()
    fun resume(vararg ids: Long)
    fun cancel(vararg ids: Long)
    fun cancelAll()
    fun delete(vararg ids: Long, deleteFile: Boolean = true)
    fun updateConfig(config: DownloaderConfig)
    fun addListener(listener: DownloadListener)
    fun removerListener(listener: DownloadListener)
}