package cn.xybbz.download.internal

internal object DownloadLogger {
    fun d(tag: String, message: String) {
        println("D/$tag: $message")
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        println("E/$tag: $message")
        throwable?.printStackTrace()
    }
}
