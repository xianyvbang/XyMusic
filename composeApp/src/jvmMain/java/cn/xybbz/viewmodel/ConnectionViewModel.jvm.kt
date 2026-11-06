package cn.xybbz.viewmodel

import cn.xybbz.common.constants.Constants

actual val connectionUrlIndex: Int
    get() = Constants.MINUS_ONE_INT

/** JVM 端保持候选连接地址默认未选择。 */
actual val connectionDefaultUrlIndex: Int
    get() = Constants.MINUS_ONE_INT
