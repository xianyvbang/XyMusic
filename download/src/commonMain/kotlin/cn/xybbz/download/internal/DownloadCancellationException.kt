package cn.xybbz.download.internal

internal class DownloadCancellationException(
    override val message: String,
) : RuntimeException(message)
