package cn.xybbz.api.exception

import java.io.IOException

class UnauthorizedException(msg: String?) : IOException(msg) {
    var statusCode = 0
        private set
    var responsePhrase: String? = null
        private set

    constructor(msg: String?, statusCode: Int, responsePhrase: String?) : this(msg) {
        this.statusCode = statusCode
        this.responsePhrase = responsePhrase
    }
}