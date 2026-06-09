package cn.xybbz.common.utils

import cn.xybbz.platform.ContextWrapper

/**
 * 分享音乐资源。
 *
 * 对于网络链接，平台实现通常会按文本/链接分享；
 * 对于本地路径，平台实现会尽量按本地文件分享或采用对应平台的降级方案。
 */
expect fun shareMusicResource(
    contextWrapper: ContextWrapper,
    resource: String?
)
