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

package cn.xybbz.localdata.dao.lrc

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import cn.xybbz.localdata.data.lrc.XyLrcConfig

@Dao
interface XyLrcConfigDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(xyLrcConfig: XyLrcConfig):Long

    @Update
    suspend fun update(xyLrcConfig: XyLrcConfig)

    @Query("select * from xy_lrc_config where itemId = :itemId and connectionId = (select connectionId from xy_settings)")
    suspend fun getLrcConfig(itemId: String): XyLrcConfig?
}