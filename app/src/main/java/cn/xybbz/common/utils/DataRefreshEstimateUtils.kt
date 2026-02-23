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

package cn.xybbz.common.utils

import cn.xybbz.common.constants.Constants
import cn.xybbz.common.enums.HomeRefreshReason
import cn.xybbz.localdata.config.DatabaseClient
import cn.xybbz.localdata.data.remote.RemoteCurrent

/**
 * 数据间隔判断存储
 */
object DataRefreshEstimateUtils {


    /**
     * 判断是否可以刷新
     * 应该刷新
     * @param [reason] 原因
     * @param [db] DB
     * @param [key] 说明
     * @param [intervalMinutes] 间歇分钟
     * @return [Boolean]
     */
    suspend fun shouldRefresh(
        reason: HomeRefreshReason,
        db: DatabaseClient,
        key: String,
        intervalMinutes: Long = Constants.HOME_PAGE_TIME_FAILURE,
    ): Boolean {
        // 手动刷新：永远刷新
        if (reason == HomeRefreshReason.Manual) return true

        val remoteCurrent = db.remoteCurrentDao
            .remoteKeyById(key)

        val lastTime = remoteCurrent?.createTime ?: 0L
        val now = System.currentTimeMillis()
        return (now - lastTime) >= (intervalMinutes * 60_000)
    }

    /**
     * 更新刷新时间
     */
    suspend fun updateHomeRefreshTime(connectionId: Long?, db: DatabaseClient, key: String) {
        db.remoteCurrentDao.insertOrReplace(
            RemoteCurrent(
                id = key,
                connectionId = connectionId ?: Constants.MINUS_ONE_INT.toLong(),
                createTime = System.currentTimeMillis(),
                refresh = false
            )
        )
    }
}