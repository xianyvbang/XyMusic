package cn.xybbz.api.exception

import java.io.IOException

/**
 * 服务异常
 * @author xybbz
 * @date 2024/03/16
 * @constructor 创建[ServiceException]
 */
class ServiceException() : IOException() {

    /**
     * 错误码
     */
    private var code: Int? = null

    /**
     * 错误提示
     */
    override var message: String? = null

    constructor(message: String, code: Int) : this(message) {
        this.message = message
        this.code = code
    }


    constructor(message: String) : this() {
        this.message = message
    }

    constructor(code: Int?, message: String?) : this() {
        this.message = message
        this.code = code
    }

    fun setMessage(message: String): ServiceException {
        this.message = message
        return this
    }
}