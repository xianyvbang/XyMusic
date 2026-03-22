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

package cn.xybbz.localdata.converter

import androidx.room.TypeConverter
import cn.xybbz.localdata.common.LocalConstants

class StringListTypeConverter {

    @TypeConverter
    fun listToString(list: List<String>?): String? {
        return list?.joinToString(LocalConstants.ARTIST_DELIMITER)
    }

    @TypeConverter
    fun stringToList(value: String?): List<String>? {
        return value?.split(LocalConstants.ARTIST_DELIMITER)
    }
}
