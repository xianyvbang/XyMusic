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
)

/**
 * 校验 Subsonic 业务状态。
 * Subsonic 协议的业务失败返回的是 HTTP 200 + status=failed，不会触发 HTTP 异常校验，
 * 因此在反序列化完成后统一校验，错误码 40（用户名或密码错误）抛未授权异常，其余抛服务异常。
 */
fun SubsonicResponse<*>.throwIfFailed() {
    val parent = subsonicResponse
    if (parent.status != Status.Failed) return
    val error = parent.error
    val code = error?.code
    val errorMessage = error?.message.orEmpty()
    if (code == 40) {
        // 错误码 40 表示用户名或密码错误，交由上层做重新登录处理
        throw UnauthorizedException(errorMessage, code, error?.message)
    }
    throw ServiceException(errorMessage, code ?: 0)
}