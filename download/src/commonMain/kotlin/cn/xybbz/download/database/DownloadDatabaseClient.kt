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

package cn.xybbz.download.database

import androidx.room.ConstructedBy
import androidx.room.Database
import cn.xybbz.database.DatabaseClient
import cn.xybbz.download.database.dao.XyDownloadDao
import cn.xybbz.download.database.data.XyDownload


@Database(
    version = 1,
    entities = [XyDownload::class],
    exportSchema = true,
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class DownloadDatabaseClient : DatabaseClient() {
    // download module 当前只维护下载表，所以数据库接口也尽量保持最小。
    val downloadDao: XyDownloadDao by lazy { createXyDownloadDao() }

    abstract fun createXyDownloadDao(): XyDownloadDao
}
