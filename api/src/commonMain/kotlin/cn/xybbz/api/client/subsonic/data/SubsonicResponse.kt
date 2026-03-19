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

package cn.xybbz.api.client.subsonic.data

import android.util.Log
import cn.xybbz.api.enums.subsonic.Status
import cn.xybbz.api.exception.ServiceException
import cn.xybbz.api.exception.UnauthorizedException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * GetArtistsResponse，A subsonic-response element with a nested artists element on success.
 */
@Serializable
data class SubsonicResponse<T : SubsonicParentResponse>(
    @SerialName(value = "subsonic-response")
    val subsonicResponse: T
){
    init {
        hasOkStatus()
    }

   fun hasOkStatus(){
        val subsonicResponse = this.subsonicResponse
        if (subsonicResponse.status == Status.Failed) {
            Log.e("error","接口报错响应:${subsonicResponse}")
            val error = subsonicResponse.error
            val code = error?.code
            if (code == 40) {
                throw UnauthorizedException(
                    msg = "${error.message}",
                    statusCode = code,
                    responsePhrase = error.message
                )
            } else {
                throw ServiceException(
                    message = error?.message ?: "",
                    code = code
                )
            }
        }
    }
}