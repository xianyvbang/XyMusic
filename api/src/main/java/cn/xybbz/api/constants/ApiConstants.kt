package cn.xybbz.api.constants

object ApiConstants {

    /**
     * The recommended value for the accept header. It prefers JSON followed by octet stream and finally
     * everything. The "any MIME type" (* / *) is required for some endpoints in the server.
     * application/octet-stream;
     */
    const val HEADER_ACCEPT: String = "application/json,q=0.9, */*;q=0.8"

    const val ACCEPT: String = "Accept"

    const val AUTHORIZATION = "Authorization"

    const val NAVIDROME_AUTHORIZATION = "X-Nd-Authorization"
    const val EMBY_AUTHORIZATION = "X-Emby-Token"
    const val NAVIDROME_HEADER = "x-nd-client-unique-id"

    const val NAVIDROME_TOTAL_COUNT = "X-Total-Count"

    const val NAVIDROME_IMAGE_PREFIX_ALBUM = "al-"
    const val NAVIDROME_IMAGE_PREFIX_MUSIC = "mf-"
    const val NAVIDROME_IMAGE_PREFIX_PLAYLIST = "pl-"

    const val AUTHORIZATION_SCHEME: String = "MediaBrowser"
    const val EMBY_AUTHORIZATION_SCHEME: String = "Emby"

    const val HTTP_OK = 200
    /**
     * 未授权
     */
    const val UNAUTHORIZED = 401
    /**
     * 访问受限，授权过期
     */
    const val FORBIDDEN = 403

    /**
     * 系统内部错误
     */
    const val ERROR = 500

    const val LOGIN_TIMEOUT = "登陆超时,请先进行登陆"
//    const val ERROR_MESSAGE = "数据获取错误"

    /**
     * okhttp超时时间
     */
    const val DEFAULT_TIMEOUT_MILLISECONDS = 10000L

    /**
     * http请求前缀
     */
    const val HTTP_PREFIX = "http://"
}