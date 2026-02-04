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

package cn.xybbz.api.constants

object ApiConstants {

    /**
     * The recommended value for the accept header. It prefers JSON followed by octet stream and finally
     * everything. The "any MIME type" (* / *) is required for some endpoints in the server.
     * application/octet-stream;
     */
    const val HEADER_ACCEPT: String = "application/json"
    const val DOWNLOAD_ACCEPT: String = "application/octet-stream"

    const val ACCEPT: String = "Accept"

    const val AUTHORIZATION = "Authorization"
    const val PROXY_AUTHORIZATION = "Proxy-Authorization"

    const val NAVIDROME_AUTHORIZATION = "X-Nd-Authorization"
    const val EMBY_AUTHORIZATION = "X-Emby-Token"
    const val PLEX_AUTHORIZATION = "X-Plex-Token"
    //region navidrome headers
    const val NAVIDROME_HEADER = "x-nd-client-unique-id"
    const val NAVIDROME_TOTAL_COUNT = "X-Total-Count"
    //endregion
    //region plex headers
    const val PLEX_CLIENT_IDENTIFIER = "X-Plex-Client-Identifier"
    const val PLEX_PRODUCT = "X-Plex-Product"
    const val PLEX_DEVICE = "X-Plex-Device"
    const val PLEX_VERSION = "X-Plex-Version"
    const val PLEX_PLATFORM = "X-Plex-Platform"
    const val PLEX_PROVIDES = "X-Plex-Provides"
    const val PLEX_DEVICE_NAME = "X-Plex-Device-Name"
    //endregion


    const val NAVIDROME_IMAGE_PREFIX_ALBUM = "al-"
    const val NAVIDROME_IMAGE_PREFIX_MUSIC = "mf-"
    const val NAVIDROME_IMAGE_PREFIX_PLAYLIST = "pl-"

    const val AUTHORIZATION_SCHEME: String = "MediaBrowser"
    const val EMBY_AUTHORIZATION_SCHEME: String = "Emby"

    /**
     * 未授权
     */
    const val UNAUTHORIZED = 401

    /**
     * okhttp超时时间
     */
    const val DEFAULT_TIMEOUT_MILLISECONDS = 10000L

    //请求是否为下载请求的标识
    const val HEADER_DOWNLOAD = "X-Download"
}