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

package cn.xybbz.config.media

import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.Metadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.extractor.metadata.id3.BinaryFrame
import androidx.media3.extractor.metadata.id3.CommentFrame
import androidx.media3.extractor.metadata.id3.TextInformationFrame
import androidx.media3.extractor.metadata.vorbis.VorbisComment
import cn.xybbz.common.enums.LrcDataType
import cn.xybbz.common.utils.CoroutineScopeUtils
import cn.xybbz.common.utils.LrcUtils
import cn.xybbz.config.lrc.LrcServer
import java.nio.charset.Charset

class MediaServer(
    private val lrcServer: LrcServer
) {
    private val DEFAULT_TAG = "MediaServer"
    val scope = CoroutineScopeUtils.getIo("MediaServer")

    @OptIn(UnstableApi::class)
    fun printMetadata(metadata: Metadata, prefix: String) {
        for (i in 0..<metadata.length()) {
            val entry = metadata.get(i)
            logd(prefix + entry)
            when (entry) {
                is TextInformationFrame -> {
                }

                is CommentFrame -> {
                }

                is BinaryFrame -> {
                    val readMp3Lyrics = readMp3Lyrics(entry)
                    androidx.media3.common.util.Log.d("Lyrics", "USLT Lyrics found: $readMp3Lyrics")
                    if (!readMp3Lyrics.isNullOrBlank()) {
                        lrcServer.createLrcList(LrcUtils.parseLrc(readMp3Lyrics), LrcDataType.FILE)
                    }
                }

                is VorbisComment -> {
                    // flac
                    if (entry.key.equals("LYRICS", true)) {
                        androidx.media3.common.util.Log.d(
                            "Lyrics",
                            "FLAC Lyrics found: ${entry.value}"
                        )
                        lrcServer.createLrcList(LrcUtils.parseLrc(entry.value), LrcDataType.FILE)
                    }
                }
            }
        }
        lrcServer.getMusicLyricList()
    }

    fun readMp3Lyrics(entry: BinaryFrame): String? {
        if (entry.id == "USLT") {
            val data = entry.data
            if (data.size < 4) return null

            val encoding = data[0].toInt() and 0xFF
            val lyrics: String = when (encoding) {
                0 -> String(data, 4, data.size - 4, Charset.forName("ISO-8859-1"))
                1 -> String(data, 4, data.size - 4, Charset.forName("UTF-16"))
                2 -> String(data, 4, data.size - 4, Charset.forName("UTF-16BE"))
                3 -> String(data, 4, data.size - 4, Charset.forName("UTF-8"))
                else -> String(data, 4, data.size - 4, Charset.forName("ISO-8859-1"))
            }

            return lyrics
        }

        return null
    }

    fun logd(msg: String) {
        Log.d(DEFAULT_TAG, msg)
    }
}