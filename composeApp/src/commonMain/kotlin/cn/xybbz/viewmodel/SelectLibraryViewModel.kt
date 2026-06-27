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

package cn.xybbz.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.xybbz.api.client.DataSourceManager
import cn.xybbz.common.constants.Constants
import cn.xybbz.localdata.config.LocalDatabaseClient
import cn.xybbz.localdata.data.library.XyLibrary
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import xymusic.composeapp.generated.resources.Res
import xymusic.composeapp.generated.resources.all_media_libraries

@KoinViewModel
class SelectLibraryViewModel(
    @InjectedParam private val connectionId: Long,
    @InjectedParam private val thisLibraryId: List<String>?,
    private val db: LocalDatabaseClient,
    private val dataSourceManager: DataSourceManager
) : ViewModel() {

    /**
     * 全部媒体库占位 ID。
     */
    private val allLibraryId = Constants.MINUS_ONE_INT.toString()

    /**
     * 进入页面时的媒体库选择，用于桌面端即时保存后的撤销。
     */
    private val initialLibraryIds = normalizedLibraryIds(thisLibraryId)

    /**
     * 最近一次已保存的媒体库选择，用于撤销本次页面内的临时改动。
     */
    private var savedLibraryIds by mutableStateOf(initialLibraryIds)

    //媒体库
    val libraryList = mutableStateListOf<XyLibrary>()


    //当前媒体库id
    val libraryIds = mutableStateSetOf<String>().apply {
        addAll(savedLibraryIds)
    }

    /**
     * 当前页面选择是否和已保存选择不同。
     */
    val hasPendingLibraryChanges: Boolean
        get() = libraryIds.toSet() != savedLibraryIds

    /**
     * 当前选择是否已经偏离进入页面时的选择，用于控制桌面端撤销按钮。
     */
    val hasInitialLibraryChanges: Boolean
        get() = libraryIds.toSet() != initialLibraryIds

    init {
        getLibraryList(resetSelection = true)
    }

    /**
     * 获得媒体库
     */
    private fun getLibraryList(resetSelection: Boolean) {
        viewModelScope.launch {
            if (resetSelection) {
                applyLibraryIds(savedLibraryIds)
            }
            val libraryData = db.libraryDao.selectListByDataSourceType()
            libraryList.clear()
            if (dataSourceManager.dataSourceType?.ifAllMediaLibrary == true) {
                libraryList.add(
                    XyLibrary(
                        id = allLibraryId,
                        name = getString(Res.string.all_media_libraries),
                        connectionId = connectionId,
                        collectionType = ""
                    )
                )
            }

            if (libraryData.isNotEmpty()) {
                libraryList.addAll(libraryData)
            }
        }
    }

    /**
     * 重新读取本地媒体库列表，保留当前页面内的临时选择。
     */
    fun refreshLibraryList() {
        getLibraryList(resetSelection = false)
    }

    /**
     * 设置媒体库id
     *
     * @param data 媒体库 ID。
     * @param saveImmediately 是否立即写入当前连接，默认选择后即时保存。
     */
    fun updateLibraryId(data: String, saveImmediately: Boolean = true) {
        if (dataSourceManager.dataSourceType?.ifMultiMediaLibrary == true) {
            if (libraryIds.contains(data)) {
                libraryIds.remove(data)
                if (libraryIds.isEmpty()) {
                    libraryIds.add(allLibraryId)
                }
            } else {
                libraryIds.add(data)
                if (data == allLibraryId) {
                    libraryIds.clear()
                    libraryIds.add(allLibraryId)
                } else {
                    libraryIds.remove(allLibraryId)
                }
            }
        } else {
            if (libraryIds.contains(data)) {
                libraryIds.clear()
                libraryIds.add(allLibraryId)
            } else {
                libraryIds.clear()
                libraryIds.add(data)
            }
        }

        if (saveImmediately) {
            saveLibraryIds()
        }
    }

    /**
     * 保存当前页面内选择到连接配置。
     */
    fun saveLibraryIds() {
        val selectedLibraryIds = libraryIds.toSet()
        var persistedLibraryIds: Set<String>? = selectedLibraryIds
        viewModelScope.launch {
            if (persistedLibraryIds?.contains(allLibraryId) == true) {
                persistedLibraryIds = null
            }
            //更新媒体库
            dataSourceManager.updateLibraryId(
                libraryIds = persistedLibraryIds?.toList(),
                connectionId = connectionId
            )
            savedLibraryIds = selectedLibraryIds
        }
    }

    /**
     * 撤销当前页面内未保存的选择，恢复到最近一次已保存状态。
     */
    fun resetLibraryIds() {
        applyLibraryIds(savedLibraryIds)
    }

    /**
     * 撤销本页面内已经即时保存的选择，并恢复进入页面时的媒体库范围。
     */
    fun restoreInitialLibraryIds() {
        applyLibraryIds(initialLibraryIds)
        saveLibraryIds()
    }

    /**
     * 将传入的媒体库 ID 写入页面状态。
     *
     * @param ids 需要展示为选中的媒体库 ID 集合。
     */
    private fun applyLibraryIds(ids: Set<String>) {
        libraryIds.clear()
        libraryIds.addAll(ids)
    }

    /**
     * 将持久化字段转换成页面可展示的媒体库 ID 集合。
     *
     * @param ids 数据库中保存的媒体库 ID 列表，空值表示全部媒体库。
     */
    private fun normalizedLibraryIds(ids: List<String>?): Set<String> {
        return ids?.toSet()?.takeIf { it.isNotEmpty() } ?: setOf(allLibraryId)
    }
}
