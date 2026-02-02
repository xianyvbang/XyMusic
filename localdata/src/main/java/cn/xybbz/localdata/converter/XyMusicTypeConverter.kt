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
import cn.xybbz.localdata.data.music.XyMusic
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class XyMusicTypeConverter {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .add(StringOrArrayAdapter())
        .build()

    private val adapter = moshi.adapter(XyMusic::class.java)

    @TypeConverter
    fun fromMusic(music: XyMusic?): String? {
        return music?.let { adapter.toJson(it) }
    }

    @TypeConverter
    fun toMusic(json: String?): XyMusic? {
        return json?.let { adapter.fromJson(it) }
    }
}
