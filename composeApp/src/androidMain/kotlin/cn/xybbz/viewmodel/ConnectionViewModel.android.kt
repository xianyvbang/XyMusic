package cn.xybbz.viewmodel

actual val connectionUrlIndex: Int
    get() = ConnectionViewModel.CHOOSE_INDEX

/** Android 端默认选中第一个候选连接地址。 */
actual val connectionDefaultUrlIndex: Int
    get() = 0
