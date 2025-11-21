package cn.xybbz.localdata.converter

import androidx.room.TypeConverter
import cn.xybbz.localdata.data.music.XyMusic
import com.squareup.moshi.Moshi
import kotlin.jvm.java

class XyMusicTypeConverter {

    private val moshi = Moshi.Builder().build()
    private val adapter = moshi.adapter(XyMusic::class.java)

    @TypeConverter
    fun fromBToJson(music: XyMusic): String {
        return adapter.toJson(music)
    }

    @TypeConverter
    fun fromJsonToB(json: String): XyMusic {
        return adapter.fromJson(json)!!
    }
}