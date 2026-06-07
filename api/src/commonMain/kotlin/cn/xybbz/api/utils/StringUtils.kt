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

package cn.xybbz.api.utils

import cn.xybbz.api.constants.ApiConstants

/**
 * 字符串通用工具方法。
 */
object StringUtils {

    /**
     * 为缺少协议头的地址补齐默认 HTTP 协议。
     *
     * @param value 需要补齐协议的地址。
     */
    fun withDefaultHttpScheme(value: String): String {
        return if (ApiConstants.URL_SCHEME_SEPARATOR in value) {
            value
        } else {
            "${ApiConstants.HTTP}$value"
        }
    }
}

/**
 * 为缺少协议头的地址补齐默认 HTTP 协议。
 */
fun String.withDefaultHttpScheme(): String {
    return StringUtils.withDefaultHttpScheme(this)
}
